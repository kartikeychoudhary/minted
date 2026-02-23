package com.minted.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.dto.*;
import com.minted.api.entity.*;
import com.minted.api.enums.*;
import com.minted.api.exception.BadRequestException;
import com.minted.api.exception.ForbiddenException;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.*;
import com.minted.api.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardStatementServiceImpl implements CreditCardStatementService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    private final CreditCardStatementRepository statementRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final JobScheduleConfigRepository scheduleConfigRepository;
    private final StatementParserService statementParserService;
    private final LlmService llmService;
    private final LlmConfigService llmConfigService;
    private final MerchantMappingService merchantMappingService;
    private final SystemSettingService systemSettingService;
    private final NotificationHelper notificationHelper;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    @Transactional
    public StatementResponse uploadAndExtract(MultipartFile file, Long accountId, String pdfPassword, Long userId) {
        checkFeatureEnabled();

        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File is required.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new BadRequestException("Only PDF files are accepted.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum of 20MB.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        // Create statement record
        CreditCardStatement statement = new CreditCardStatement();
        statement.setUser(user);
        statement.setAccount(account);
        statement.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "statement.pdf");
        statement.setFileSize(file.getSize());
        statement.setStatus(StatementStatus.UPLOADED);
        statement.setCurrentStep(1);
        if (pdfPassword != null && !pdfPassword.isBlank()) {
            statement.setPdfPasswordHint(pdfPassword.length() > 20 ? pdfPassword.substring(0, 20) : pdfPassword);
        }

        statement = statementRepository.save(statement);

        // Extract text
        try {
            String extractedText = statementParserService.extractText(file.getBytes(), pdfPassword);
            statement.setExtractedText(extractedText);
            statement.setStatus(StatementStatus.TEXT_EXTRACTED);
            statement.setCurrentStep(2);
            statement = statementRepository.save(statement);

            notificationHelper.notify(userId, NotificationType.INFO,
                    "Statement Uploaded",
                    "Text extracted from " + statement.getFileName() + ". Review the text and send to AI for parsing.");

        } catch (BadRequestException e) {
            statement.setStatus(StatementStatus.FAILED);
            statement.setErrorMessage(e.getMessage());
            statementRepository.save(statement);
            throw e;
        } catch (IOException e) {
            statement.setStatus(StatementStatus.FAILED);
            statement.setErrorMessage("Failed to extract text: " + e.getMessage());
            statementRepository.save(statement);
            throw new BadRequestException("Failed to extract text from PDF: " + e.getMessage());
        }

        return StatementResponse.from(statement);
    }

    @Override
    @Transactional
    public StatementResponse triggerLlmParse(Long statementId, Long userId) {
        checkFeatureEnabled();

        CreditCardStatement statement = statementRepository.findByIdAndUserId(statementId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found with id: " + statementId));

        if (statement.getStatus() != StatementStatus.TEXT_EXTRACTED) {
            throw new BadRequestException("Statement is not ready for parsing. Current status: " + statement.getStatus());
        }

        // Resolve LLM config
        LlmConfigService.EffectiveLlmConfig effectiveConfig = llmConfigService.getEffectiveConfig(userId);

        // Create JobExecution
        JobExecution execution = new JobExecution();
        execution.setJobName("CREDIT_CARD_STATEMENT_PARSER");
        execution.setStatus(JobStatus.RUNNING);
        execution.setTriggerType(JobTriggerType.MANUAL);
        execution.setStartTime(LocalDateTime.now());
        execution.setTotalSteps(2);
        execution = jobExecutionRepository.save(execution);

        statement.setJobExecution(execution);
        statement = statementRepository.save(statement);

        // Trigger async processing after commit
        final Long stmtId = statement.getId();
        final String apiKey = effectiveConfig.apiKey();
        final String modelKey = effectiveConfig.modelKey();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> {
                    try {
                        processLlmParseAsync(stmtId, userId, apiKey, modelKey);
                    } catch (Exception e) {
                        log.error("Async LLM parse failed for statement {}: {}", stmtId, e.getMessage(), e);
                    }
                });
            }
        });

        return StatementResponse.from(statement);
    }

    public void processLlmParseAsync(Long statementId, Long userId, String apiKey, String modelKey) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            CreditCardStatement statement = statementRepository.findById(statementId)
                    .orElseThrow(() -> new ResourceNotFoundException("Statement not found: " + statementId));
            JobExecution execution = statement.getJobExecution();

            try {
                // Step 1: LLM Parse
                JobStepExecution step1 = createStep(execution, "LLM Parse", 1);
                List<MerchantCategoryMapping> mappings = merchantMappingService.getRawMappings(userId);
                List<ParsedTransactionRow> rows = llmService.parseStatement(
                        statement.getExtractedText(), userId, statementId, mappings, apiKey, modelKey);
                completeStep(step1, null);
                execution.setCompletedSteps(1);
                jobExecutionRepository.save(execution);

                // Pre-pass: apply merchant mapping rules
                rows = applyMerchantMappings(rows, mappings);

                // Step 2: Duplicate Detection
                JobStepExecution step2 = createStep(execution, "Duplicate Detection", 2);
                int duplicateCount = 0;
                for (ParsedTransactionRow row : rows) {
                    if (isDuplicate(row, statement.getAccount().getId())) {
                        row.setIsDuplicate(true);
                        row.setDuplicateReason("Similar transaction found within ±1 day");
                        duplicateCount++;
                    }
                }
                completeStep(step2, null);
                execution.setCompletedSteps(2);

                // Save results
                statement.setLlmResponseJson(objectMapper.writeValueAsString(rows));
                statement.setParsedCount(rows.size());
                statement.setDuplicateCount(duplicateCount);
                statement.setStatus(StatementStatus.LLM_PARSED);
                statement.setCurrentStep(3);
                statementRepository.save(statement);

                execution.setStatus(JobStatus.COMPLETED);
                execution.setEndTime(LocalDateTime.now());
                jobExecutionRepository.save(execution);

                notificationHelper.notify(userId, NotificationType.SUCCESS,
                        "AI Parsing Complete",
                        rows.size() + " transactions found (" + duplicateCount + " possible duplicates). Review and import.");

            } catch (Exception e) {
                log.error("LLM parse failed for statement {}: {}", statementId, e.getMessage(), e);
                execution.setStatus(JobStatus.FAILED);
                execution.setEndTime(LocalDateTime.now());
                execution.setErrorMessage(e.getMessage());
                jobExecutionRepository.save(execution);

                statement.setStatus(StatementStatus.FAILED);
                statement.setErrorMessage("AI parsing failed: " + e.getMessage());
                statementRepository.save(statement);

                notificationHelper.notify(userId, NotificationType.ERROR,
                        "AI Parsing Failed",
                        "Failed to parse statement: " + e.getMessage());
            }
        });
    }

    @Override
    @Transactional
    public void confirmImport(ConfirmStatementRequest request, Long userId) {
        CreditCardStatement statement = statementRepository.findByIdAndUserId(request.statementId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found with id: " + request.statementId()));

        if (statement.getStatus() != StatementStatus.LLM_PARSED) {
            throw new BadRequestException("Statement is not ready for import. Current status: " + statement.getStatus());
        }

        List<ParsedTransactionRow> rows;
        try {
            rows = objectMapper.readValue(statement.getLlmResponseJson(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to parse stored transaction data.");
        }

        // Filter duplicates if requested
        if (request.skipDuplicates()) {
            rows = rows.stream()
                    .filter(r -> !r.getIsDuplicate())
                    .collect(Collectors.toList());
        }

        User user = statement.getUser();
        Account account = accountRepository.findById(statement.getAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        int importedCount = 0;
        for (ParsedTransactionRow row : rows) {
            try {
                TransactionType type = TransactionType.valueOf(row.getType());

                // Resolve category
                TransactionCategory category = null;
                if (row.getMatchedCategoryId() != null) {
                    category = categoryRepository.findByIdAndUserId(row.getMatchedCategoryId(), userId).orElse(null);
                }
                if (category == null && row.getCategoryName() != null) {
                    category = categoryRepository.findByNameIgnoreCaseAndUserIdAndType(
                            row.getCategoryName(), userId, type).orElse(null);
                }
                if (category == null) {
                    // Fall back to first category of matching type
                    List<TransactionCategory> cats = categoryRepository.findByUserIdAndTypeAndIsActiveTrue(userId, type);
                    if (!cats.isEmpty()) {
                        category = cats.get(0);
                    } else {
                        log.warn("No category found for row: {}, skipping", row.getDescription());
                        continue;
                    }
                }

                Transaction transaction = new Transaction();
                transaction.setAmount(row.getAmount());
                transaction.setType(type);
                transaction.setDescription(row.getDescription());
                transaction.setNotes(row.getNotes() != null && !row.getNotes().isBlank() ? row.getNotes() : null);
                transaction.setTransactionDate(LocalDate.parse(row.getTransactionDate()));
                transaction.setAccount(account);
                transaction.setCategory(category);
                transaction.setUser(user);
                transaction.setIsRecurring(false);
                transaction.setTags(row.getTags() != null && !row.getTags().isBlank() ? row.getTags() : null);

                transactionRepository.save(transaction);

                // Update account balance
                switch (type) {
                    case INCOME -> account.setBalance(account.getBalance().add(row.getAmount()));
                    case EXPENSE -> account.setBalance(account.getBalance().subtract(row.getAmount()));
                    default -> {} // TRANSFER not expected from credit card statements
                }

                importedCount++;
            } catch (Exception e) {
                log.warn("Failed to import row '{}': {}", row.getDescription(), e.getMessage());
            }
        }

        accountRepository.save(account);

        statement.setImportedCount(importedCount);
        statement.setStatus(StatementStatus.COMPLETED);
        statement.setCurrentStep(4);
        statementRepository.save(statement);

        notificationHelper.notify(userId, NotificationType.SUCCESS,
                "Import Complete",
                importedCount + " transactions added to " + account.getName() + ".");
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatementResponse> getUserStatements(Long userId) {
        return statementRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(StatementResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StatementResponse getStatementById(Long statementId, Long userId) {
        CreditCardStatement statement = statementRepository.findByIdAndUserId(statementId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found with id: " + statementId));
        return StatementResponse.from(statement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParsedTransactionRow> getParsedRows(Long statementId, Long userId) {
        CreditCardStatement statement = statementRepository.findByIdAndUserId(statementId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Statement not found with id: " + statementId));

        if (statement.getLlmResponseJson() == null || statement.getLlmResponseJson().isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(statement.getLlmResponseJson(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to parse stored transaction data.");
        }
    }

    // --- Private helpers ---

    private void checkFeatureEnabled() {
        try {
            boolean enabled = "true".equalsIgnoreCase(systemSettingService.getValue("CREDIT_CARD_PARSER_ENABLED"));
            if (!enabled) {
                throw new ForbiddenException("Credit Card Statement Parser is currently disabled by the administrator.");
            }
        } catch (ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            // If setting doesn't exist, allow the feature
            log.warn("Could not check CREDIT_CARD_PARSER_ENABLED setting: {}", e.getMessage());
        }
    }

    private List<ParsedTransactionRow> applyMerchantMappings(List<ParsedTransactionRow> rows,
                                                              List<MerchantCategoryMapping> mappings) {
        if (mappings == null || mappings.isEmpty()) return rows;

        for (ParsedTransactionRow row : rows) {
            for (MerchantCategoryMapping mapping : mappings) {
                boolean matches = mapping.getSnippetList().stream()
                        .anyMatch(snippet ->
                                row.getDescription() != null &&
                                        row.getDescription().toUpperCase().contains(snippet.toUpperCase()));
                if (matches) {
                    row.setCategoryName(mapping.getCategory().getName());
                    row.setMatchedCategoryId(mapping.getCategory().getId());
                    row.setMappedByRule(true);
                    break; // first match wins
                }
            }
        }
        return rows;
    }

    private boolean isDuplicate(ParsedTransactionRow row, Long accountId) {
        try {
            LocalDate date = LocalDate.parse(row.getTransactionDate());
            String descSnippet = row.getDescription() != null && row.getDescription().length() > 10
                    ? row.getDescription().substring(0, 10)
                    : row.getDescription();

            return transactionRepository
                    .existsByAccountIdAndAmountAndTransactionDateBetweenAndDescriptionContainingIgnoreCase(
                            accountId,
                            row.getAmount(),
                            date.minusDays(1),
                            date.plusDays(1),
                            descSnippet != null ? descSnippet : "");
        } catch (Exception e) {
            log.warn("Duplicate check failed for row '{}': {}", row.getDescription(), e.getMessage());
            return false;
        }
    }

    private JobStepExecution createStep(JobExecution jobExecution, String name, int order) {
        JobStepExecution step = new JobStepExecution();
        step.setJobExecution(jobExecution);
        step.setStepName(name);
        step.setStepOrder(order);
        step.setStatus(JobStepStatus.RUNNING);
        step.setStartTime(LocalDateTime.now());
        jobExecution.getSteps().add(step);
        return step;
    }

    private void completeStep(JobStepExecution step, String contextJson) {
        step.setStatus(JobStepStatus.COMPLETED);
        step.setEndTime(LocalDateTime.now());
        if (contextJson != null) {
            step.setContextJson(contextJson);
        }
    }
}
