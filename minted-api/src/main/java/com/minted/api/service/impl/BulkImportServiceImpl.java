package com.minted.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.dto.*;
import com.minted.api.entity.*;
import com.minted.api.enums.*;
import com.minted.api.exception.BadRequestException;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.*;
import com.minted.api.service.BulkImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkImportServiceImpl implements BulkImportService {

    private static final int MAX_ROWS = 5000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String CSV_TEMPLATE = "date,amount,type,description,categoryName,notes,tags\n" +
            "2026-01-15,1500.00,EXPENSE,Grocery shopping,Groceries,Weekly groceries,food;weekly\n" +
            "2026-01-16,50000.00,INCOME,Monthly salary,Salary,January salary,salary;monthly\n";

    private final BulkImportRepository bulkImportRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final JobScheduleConfigRepository scheduleConfigRepository;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Override
    public byte[] getCsvTemplate() {
        return CSV_TEMPLATE.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional
    public CsvUploadResponse uploadAndValidate(MultipartFile file, Long accountId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + accountId));

        String csvContent;
        try {
            csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BadRequestException("Failed to read uploaded file: " + e.getMessage());
        }

        List<String[]> parsedRows = parseCsv(csvContent);

        if (parsedRows.isEmpty()) {
            throw new BadRequestException("CSV file is empty or has no data rows");
        }
        if (parsedRows.size() > MAX_ROWS) {
            throw new BadRequestException("CSV file exceeds maximum of " + MAX_ROWS + " rows. Found: " + parsedRows.size());
        }

        List<CsvRowPreview> previews = new ArrayList<>();
        int validCount = 0;
        int errorCount = 0;
        int duplicateCount = 0;

        for (int i = 0; i < parsedRows.size(); i++) {
            String[] fields = parsedRows.get(i);
            CsvRowPreview preview = validateRow(fields, i + 1, userId, accountId);
            previews.add(preview);

            switch (preview.status()) {
                case "VALID" -> validCount++;
                case "ERROR" -> errorCount++;
                case "DUPLICATE" -> duplicateCount++;
            }
        }

        // Save BulkImport entity
        BulkImport bulkImport = new BulkImport();
        bulkImport.setUser(user);
        bulkImport.setAccount(account);
        bulkImport.setImportType(ImportType.CSV);
        bulkImport.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "import.csv");
        bulkImport.setFileSize(file.getSize());
        bulkImport.setTotalRows(parsedRows.size());
        bulkImport.setValidRows(validCount);
        bulkImport.setErrorRows(errorCount);
        bulkImport.setDuplicateRows(duplicateCount);
        bulkImport.setStatus(ImportStatus.VALIDATED);
        bulkImport.setCsvData(csvContent);

        try {
            bulkImport.setValidationResult(objectMapper.writeValueAsString(previews));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize validation result", e);
        }

        bulkImport = bulkImportRepository.save(bulkImport);

        return new CsvUploadResponse(
                bulkImport.getId(),
                parsedRows.size(),
                validCount,
                errorCount,
                duplicateCount,
                previews
        );
    }

    @Override
    @Transactional
    public BulkImportResponse confirmImport(BulkImportConfirmRequest request, Long userId) {
        BulkImport bulkImport = bulkImportRepository.findByIdAndUserId(request.importId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Import not found with id: " + request.importId()));

        if (bulkImport.getStatus() != ImportStatus.VALIDATED) {
            throw new BadRequestException("Import is not in VALIDATED status. Current status: " + bulkImport.getStatus());
        }

        bulkImport.setStatus(ImportStatus.IMPORTING);
        bulkImport.setSkipDuplicates(request.skipDuplicates());

        // Create JobExecution record
        JobScheduleConfig config = scheduleConfigRepository.findByJobName("BULK_IMPORT_PROCESSOR").orElse(null);
        JobExecution execution = new JobExecution();
        execution.setJobName("BULK_IMPORT_PROCESSOR");
        execution.setScheduleConfig(config);
        execution.setStatus(JobStatus.RUNNING);
        execution.setTriggerType(JobTriggerType.MANUAL);
        execution.setStartTime(java.time.LocalDateTime.now());
        execution.setTotalSteps(4);
        execution = jobExecutionRepository.save(execution);

        bulkImport.setJobExecution(execution);
        bulkImport = bulkImportRepository.save(bulkImport);

        // Trigger async processing AFTER transaction commits
        // (must wait for commit so processImportAsync can load job_execution_id from DB)
        final Long bulkImportId = bulkImport.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    try {
                        processImportAsync(bulkImportId);
                    } catch (Exception e) {
                        log.error("Async import processing failed for import ID: {}", bulkImportId, e);
                    }
                });
            }
        });

        return BulkImportResponse.from(bulkImport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BulkImportResponse> getUserImports(Long userId) {
        return bulkImportRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(BulkImportResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BulkImportResponse getImportById(Long importId, Long userId) {
        BulkImport bulkImport = bulkImportRepository.findByIdAndUserId(importId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Import not found with id: " + importId));
        return BulkImportResponse.from(bulkImport);
    }

    @Override
    @Transactional(readOnly = true)
    public JobExecutionResponse getImportJobDetails(Long importId, Long userId) {
        BulkImport bulkImport = bulkImportRepository.findByIdAndUserId(importId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Import not found with id: " + importId));

        if (bulkImport.getJobExecution() == null) {
            throw new ResourceNotFoundException("No job execution found for import: " + importId);
        }

        JobExecution execution = bulkImport.getJobExecution();
        List<JobStepExecutionResponse> stepResponses = execution.getSteps().stream()
                .map(step -> new JobStepExecutionResponse(
                        step.getId(),
                        step.getStepName(),
                        step.getStepOrder(),
                        step.getStatus().name(),
                        step.getContextJson(),
                        step.getErrorMessage(),
                        step.getStartTime(),
                        step.getEndTime()
                ))
                .collect(Collectors.toList());

        return new JobExecutionResponse(
                execution.getId(),
                execution.getJobName(),
                execution.getStatus().name(),
                execution.getTriggerType().name(),
                execution.getStartTime(),
                execution.getEndTime(),
                execution.getErrorMessage(),
                execution.getTotalSteps(),
                execution.getCompletedSteps(),
                stepResponses
        );
    }

    // ---- CSV Parsing ----

    private List<String[]> parseCsv(String csvContent) {
        List<String[]> rows = new ArrayList<>();
        String[] lines = csvContent.split("\n");

        if (lines.length <= 1) {
            return rows; // Only header or empty
        }

        // Skip header line
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            rows.add(parseCsvLine(line));
        }
        return rows;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());

        return fields.toArray(new String[0]);
    }

    private CsvRowPreview validateRow(String[] fields, int rowNumber, Long userId, Long accountId) {
        // Expected columns: date, amount, type, description, categoryName, notes, tags
        if (fields.length < 5) {
            return new CsvRowPreview(rowNumber,
                    fields.length > 0 ? fields[0] : "",
                    fields.length > 1 ? fields[1] : "",
                    fields.length > 2 ? fields[2] : "",
                    fields.length > 3 ? fields[3] : "",
                    fields.length > 4 ? fields[4] : "",
                    "", "", "ERROR",
                    "Row has fewer than 5 required columns (date, amount, type, description, categoryName)",
                    null, false);
        }

        String dateStr = fields[0].trim();
        String amountStr = fields[1].trim();
        String typeStr = fields[2].trim().toUpperCase();
        String description = fields[3].trim();
        String categoryName = fields[4].trim();
        String notes = fields.length > 5 ? fields[5].trim() : "";
        String tags = fields.length > 6 ? fields[6].trim() : "";

        List<String> errors = new ArrayList<>();

        // Validate date
        LocalDate parsedDate = null;
        try {
            parsedDate = LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            errors.add("Invalid date format. Expected yyyy-MM-dd");
        }

        // Validate amount
        BigDecimal parsedAmount = null;
        try {
            parsedAmount = new BigDecimal(amountStr);
            if (parsedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("Amount must be greater than 0");
            }
        } catch (NumberFormatException e) {
            errors.add("Invalid amount: " + amountStr);
        }

        // Validate type
        TransactionType parsedType = null;
        try {
            parsedType = TransactionType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            errors.add("Invalid type. Must be INCOME, EXPENSE, or TRANSFER");
        }

        // Validate description
        if (description.isEmpty()) {
            errors.add("Description is required");
        } else if (description.length() > 500) {
            errors.add("Description exceeds 500 characters");
        }

        // Validate category
        Long matchedCategoryId = null;
        if (categoryName.isEmpty()) {
            errors.add("Category name is required");
        } else if (parsedType != null) {
            Optional<TransactionCategory> category = categoryRepository
                    .findByNameIgnoreCaseAndUserIdAndType(categoryName, userId, parsedType);
            if (category.isPresent()) {
                matchedCategoryId = category.get().getId();
            } else {
                errors.add("Category '" + categoryName + "' not found for type " + parsedType);
            }
        }

        if (!errors.isEmpty()) {
            return new CsvRowPreview(rowNumber, dateStr, amountStr, typeStr, description,
                    categoryName, notes, tags, "ERROR",
                    String.join("; ", errors), matchedCategoryId, false);
        }

        // Check for duplicates
        boolean isDuplicate = transactionRepository
                .existsByTransactionDateAndAmountAndDescriptionAndAccountIdAndUserId(
                        parsedDate, parsedAmount, description, accountId, userId);

        String status = isDuplicate ? "DUPLICATE" : "VALID";
        return new CsvRowPreview(rowNumber, dateStr, amountStr, typeStr, description,
                categoryName, notes, tags, status,
                null, matchedCategoryId, isDuplicate);
    }

    // ---- Async Processing ----

    /**
     * Runs inside a programmatic transaction via TransactionTemplate.
     * Cannot rely on @Transactional here because this method is called via
     * CompletableFuture.runAsync() (self-invocation bypasses Spring AOP proxy).
     */
    public void processImportAsync(Long bulkImportId) {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            BulkImport bulkImport = bulkImportRepository.findById(bulkImportId)
                    .orElseThrow(() -> new ResourceNotFoundException("Import not found: " + bulkImportId));
            JobExecution execution = bulkImport.getJobExecution();

            try {
                // Step 1: Re-validate CSV
                List<CsvRowPreview> revalidated = executeStep1Revalidate(execution, bulkImport);
                execution.setCompletedSteps(1);
                jobExecutionRepository.save(execution);

                // Step 2: Check Duplicates
                List<CsvRowPreview> toImport = executeStep2CheckDuplicates(execution, bulkImport, revalidated);
                execution.setCompletedSteps(2);
                jobExecutionRepository.save(execution);

                // Step 3: Insert Transactions
                int[] results = executeStep3InsertTransactions(execution, bulkImport, toImport);
                execution.setCompletedSteps(3);
                jobExecutionRepository.save(execution);

                // Step 4: Summary
                executeStep4Summary(execution, bulkImport, results[0], results[1],
                        bulkImport.getSkipDuplicates() ? bulkImport.getDuplicateRows() : 0);
                execution.setCompletedSteps(4);

                execution.setStatus(JobStatus.COMPLETED);
                execution.setEndTime(java.time.LocalDateTime.now());
                jobExecutionRepository.save(execution);

                bulkImport.setStatus(ImportStatus.COMPLETED);
                bulkImportRepository.save(bulkImport);

            } catch (Exception e) {
                log.error("Import processing failed for import ID: {}", bulkImportId, e);
                execution.setStatus(JobStatus.FAILED);
                execution.setEndTime(java.time.LocalDateTime.now());
                execution.setErrorMessage(e.getMessage());
                jobExecutionRepository.save(execution);

                bulkImport.setStatus(ImportStatus.FAILED);
                bulkImport.setErrorMessage(e.getMessage());
                bulkImportRepository.save(bulkImport);
            }
        });
    }

    private List<CsvRowPreview> executeStep1Revalidate(JobExecution execution, BulkImport bulkImport) throws Exception {
        JobStepExecution step = createStep(execution, "Re-validate CSV Data", 1);

        try {
            List<String[]> parsedRows = parseCsv(bulkImport.getCsvData());
            List<CsvRowPreview> previews = new ArrayList<>();
            int validCount = 0;
            int errorCount = 0;

            for (int i = 0; i < parsedRows.size(); i++) {
                CsvRowPreview preview = validateRow(parsedRows.get(i), i + 1,
                        bulkImport.getUser().getId(), bulkImport.getAccount().getId());
                previews.add(preview);
                if ("ERROR".equals(preview.status())) errorCount++;
                else validCount++;
            }

            java.util.Map<String, Object> context = new java.util.HashMap<>();
            context.put("totalRows", parsedRows.size());
            context.put("validRows", validCount);
            context.put("errorRows", errorCount);
            completeStep(step, context);

            return previews;
        } catch (Exception e) {
            failStep(step, e);
            throw e;
        }
    }

    private List<CsvRowPreview> executeStep2CheckDuplicates(JobExecution execution, BulkImport bulkImport,
                                                             List<CsvRowPreview> rows) throws Exception {
        JobStepExecution step = createStep(execution, "Check Duplicates", 2);

        try {
            int duplicatesFound = 0;
            int skipped = 0;
            List<CsvRowPreview> toImport = new ArrayList<>();

            for (CsvRowPreview row : rows) {
                if ("ERROR".equals(row.status())) continue;

                if (row.isDuplicate()) {
                    duplicatesFound++;
                    if (bulkImport.getSkipDuplicates()) {
                        skipped++;
                        continue;
                    }
                }
                toImport.add(row);
            }

            java.util.Map<String, Object> context = new java.util.HashMap<>();
            context.put("duplicatesFound", duplicatesFound);
            context.put("skipped", skipped);
            context.put("rowsToImport", toImport.size());
            completeStep(step, context);

            return toImport;
        } catch (Exception e) {
            failStep(step, e);
            throw e;
        }
    }

    private int[] executeStep3InsertTransactions(JobExecution execution, BulkImport bulkImport,
                                                  List<CsvRowPreview> rows) throws Exception {
        JobStepExecution step = createStep(execution, "Insert Transactions", 3);

        int imported = 0;
        int failed = 0;
        List<Integer> failedRows = new ArrayList<>();

        try {
            User user = bulkImport.getUser();
            Account account = accountRepository.findById(bulkImport.getAccount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

            for (CsvRowPreview row : rows) {
                try {
                    LocalDate date = LocalDate.parse(row.date(), DATE_FORMAT);
                    BigDecimal amount = new BigDecimal(row.amount());
                    TransactionType type = TransactionType.valueOf(row.type());

                    TransactionCategory category = categoryRepository.findById(row.matchedCategoryId())
                            .orElseThrow(() -> new RuntimeException("Category not found: " + row.matchedCategoryId()));

                    if (!type.equals(category.getType())) {
                        throw new RuntimeException("Transaction type " + type + " does not match category type " + category.getType());
                    }

                    Transaction transaction = new Transaction();
                    transaction.setAmount(amount);
                    transaction.setType(type);
                    transaction.setDescription(row.description());
                    transaction.setNotes(row.notes() != null && !row.notes().isEmpty() ? row.notes() : null);
                    transaction.setTransactionDate(date);
                    transaction.setAccount(account);
                    transaction.setCategory(category);
                    transaction.setUser(user);
                    transaction.setIsRecurring(false);
                    transaction.setTags(row.tags() != null && !row.tags().isEmpty() ? row.tags() : null);

                    transactionRepository.save(transaction);

                    // Update account balance
                    switch (type) {
                        case INCOME -> {
                            account.setBalance(account.getBalance().add(amount));
                            accountRepository.save(account);
                        }
                        case EXPENSE -> {
                            account.setBalance(account.getBalance().subtract(amount));
                            accountRepository.save(account);
                        }
                        case TRANSFER -> {
                            // For bulk import, transfers only debit the source account
                            account.setBalance(account.getBalance().subtract(amount));
                            accountRepository.save(account);
                        }
                    }

                    imported++;
                } catch (Exception ex) {
                    log.error("Failed to import row {}: {}", row.rowNumber(), ex.getMessage());
                    failed++;
                    failedRows.add(row.rowNumber());
                }
            }

            bulkImport.setImportedRows(imported);
            bulkImport.setErrorRows(bulkImport.getErrorRows() + failed);
            bulkImportRepository.save(bulkImport);

            java.util.Map<String, Object> context = new java.util.HashMap<>();
            context.put("imported", imported);
            context.put("failed", failed);
            if (!failedRows.isEmpty()) {
                context.put("failedRows", failedRows);
            }
            completeStep(step, context);

            return new int[]{imported, failed};
        } catch (Exception e) {
            failStep(step, e);
            throw e;
        }
    }

    private void executeStep4Summary(JobExecution execution, BulkImport bulkImport,
                                      int totalImported, int totalFailed, int totalSkipped) throws Exception {
        JobStepExecution step = createStep(execution, "Summary", 4);

        try {
            java.util.Map<String, Object> context = new java.util.HashMap<>();
            context.put("totalImported", totalImported);
            context.put("totalFailed", totalFailed);
            context.put("totalSkipped", totalSkipped);
            context.put("fileName", bulkImport.getFileName());
            context.put("accountName", bulkImport.getAccount().getName());
            completeStep(step, context);
        } catch (Exception e) {
            failStep(step, e);
            throw e;
        }
    }

    // ---- Step Helpers (same pattern as RecurringTransactionJob) ----

    private JobStepExecution createStep(JobExecution jobExecution, String name, int order) {
        JobStepExecution step = new JobStepExecution();
        step.setJobExecution(jobExecution);
        step.setStepName(name);
        step.setStepOrder(order);
        step.setStatus(JobStepStatus.RUNNING);
        step.setStartTime(java.time.LocalDateTime.now());
        jobExecution.getSteps().add(step);
        return step;
    }

    private void completeStep(JobStepExecution step, java.util.Map<String, Object> context) throws JsonProcessingException {
        step.setStatus(JobStepStatus.COMPLETED);
        step.setEndTime(java.time.LocalDateTime.now());
        if (context != null) {
            step.setContextJson(objectMapper.writeValueAsString(context));
        }
    }

    private void failStep(JobStepExecution step, Exception e) {
        step.setStatus(JobStepStatus.FAILED);
        step.setEndTime(java.time.LocalDateTime.now());
        step.setErrorMessage(e.getMessage());
    }
}
