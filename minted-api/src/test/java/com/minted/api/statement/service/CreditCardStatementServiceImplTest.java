package com.minted.api.statement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.account.entity.Account;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.admin.service.SystemSettingService;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ForbiddenException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.job.repository.JobExecutionRepository;
import com.minted.api.job.repository.JobScheduleConfigRepository;
import com.minted.api.llm.service.LlmConfigService;
import com.minted.api.llm.service.LlmService;
import com.minted.api.llm.service.MerchantMappingService;
import com.minted.api.notification.enums.NotificationType;
import com.minted.api.notification.service.NotificationHelper;
import com.minted.api.statement.dto.ConfirmStatementRequest;
import com.minted.api.statement.dto.ParsedTransactionRow;
import com.minted.api.statement.dto.StatementResponse;
import com.minted.api.statement.entity.CreditCardStatement;
import com.minted.api.statement.enums.StatementStatus;
import com.minted.api.statement.repository.CreditCardStatementRepository;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.transaction.service.TransactionCategoryService;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditCardStatementServiceImplTest {

    @Mock CreditCardStatementRepository statementRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock TransactionCategoryRepository categoryRepository;
    @Mock AccountRepository accountRepository;
    @Mock UserRepository userRepository;
    @Mock JobExecutionRepository jobExecutionRepository;
    @Mock JobScheduleConfigRepository scheduleConfigRepository;
    @Mock StatementParserService statementParserService;
    @Mock LlmService llmService;
    @Mock LlmConfigService llmConfigService;
    @Mock MerchantMappingService merchantMappingService;
    @Mock TransactionCategoryService transactionCategoryService;
    @Mock SystemSettingService systemSettingService;
    @Mock NotificationHelper notificationHelper;
    @Spy  ObjectMapper objectMapper = new ObjectMapper();
    @Mock PlatformTransactionManager transactionManager;

    @InjectMocks CreditCardStatementServiceImpl statementService;

    private User user;
    private Account account;
    private CreditCardStatement statement;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("alice");

        account = new Account();
        account.setId(1L);
        account.setName("Credit Card");
        account.setBalance(BigDecimal.valueOf(1000));

        statement = new CreditCardStatement();
        statement.setId(1L);
        statement.setUser(user);
        statement.setAccount(account);
        statement.setFileName("statement.pdf");
        statement.setFileSize(10240L);
        statement.setFileType("PDF");
        statement.setStatus(StatementStatus.TEXT_EXTRACTED);
        statement.setCurrentStep(2);
    }

    // ── getUserStatements ─────────────────────────────────────────────────────

    @Test
    void getUserStatements_returnsMappedList() {
        when(statementRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(statement));

        List<StatementResponse> result = statementService.getUserStatements(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).fileName()).isEqualTo("statement.pdf");
        assertThat(result.get(0).accountName()).isEqualTo("Credit Card");
    }

    // ── getStatementById ──────────────────────────────────────────────────────

    @Test
    void getStatementById_found_returnsResponse() {
        when(statementRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(statement));

        StatementResponse result = statementService.getStatementById(1L, 1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(StatementStatus.TEXT_EXTRACTED);
    }

    @Test
    void getStatementById_notFound_throwsException() {
        when(statementRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statementService.getStatementById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getParsedRows ─────────────────────────────────────────────────────────

    @Test
    void getParsedRows_noLlmResponse_returnsEmptyList() {
        statement.setLlmResponseJson(null);
        when(statementRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(statement));

        List<ParsedTransactionRow> result = statementService.getParsedRows(1L, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getParsedRows_withParsedJson_returnsRows() throws Exception {
        String json = "[{\"amount\":100.00,\"type\":\"EXPENSE\",\"description\":\"Coffee\"," +
                "\"transactionDate\":\"2025-01-15\"}]";
        statement.setLlmResponseJson(json);
        when(statementRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(statement));

        List<ParsedTransactionRow> result = statementService.getParsedRows(1L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Coffee");
    }

    // ── deleteStatement ───────────────────────────────────────────────────────

    @Test
    void deleteStatement_deletesAndNotifies() {
        when(statementRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(statement));

        statementService.deleteStatement(1L, 1L);

        verify(statementRepository).delete(statement);
        verify(notificationHelper).notify(eq(1L), eq(NotificationType.INFO), anyString(), anyString());
    }

    @Test
    void deleteStatement_notFound_throwsException() {
        when(statementRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statementService.deleteStatement(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── uploadAndExtract ──────────────────────────────────────────────────────

    @Test
    void uploadAndExtract_featureDisabled_throwsForbidden() {
        when(systemSettingService.getValue("CREDIT_CARD_PARSER_ENABLED")).thenReturn("false");
        MockMultipartFile file = new MockMultipartFile(
                "file", "statement.csv", "text/csv", "content".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> statementService.uploadAndExtract(file, 1L, null, 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void uploadAndExtract_unsupportedFileType_throwsBadRequest() {
        when(systemSettingService.getValue("CREDIT_CARD_PARSER_ENABLED")).thenReturn("true");
        MockMultipartFile file = new MockMultipartFile(
                "file", "statement.xlsx", "application/octet-stream",
                "content".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> statementService.uploadAndExtract(file, 1L, null, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    void uploadAndExtract_csvFile_extractsTextAndSavesStatement() {
        String csvContent = "date,amount,description\n2025-01-15,100.00,Coffee\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "statement.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        when(systemSettingService.getValue("CREDIT_CARD_PARSER_ENABLED")).thenReturn("true");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(statementRepository.save(any())).thenAnswer(inv -> {
            CreditCardStatement s = inv.getArgument(0);
            s.setId(1L);
            return s;
        });

        StatementResponse result = statementService.uploadAndExtract(file, 1L, null, 1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(StatementStatus.TEXT_EXTRACTED);
        assertThat(result.fileName()).isEqualTo("statement.csv");
        verify(statementRepository, times(2)).save(any()); // once UPLOADED, once TEXT_EXTRACTED
        verify(notificationHelper).notify(eq(1L), eq(NotificationType.INFO), anyString(), anyString());
    }

    // ── confirmImport ─────────────────────────────────────────────────────────

    @Test
    void confirmImport_wrongStatus_throwsBadRequest() {
        statement.setStatus(StatementStatus.UPLOADED); // not LLM_PARSED
        ConfirmStatementRequest request = new ConfirmStatementRequest(1L, false, null);
        when(statementRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(statement));

        assertThatThrownBy(() -> statementService.confirmImport(request, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not ready for import");
    }

    @Test
    void confirmImport_withModifiedRows_importsTransactions() {
        statement.setStatus(StatementStatus.LLM_PARSED);

        TransactionCategory category = new TransactionCategory();
        category.setId(5L);
        category.setName("Food");
        category.setType(TransactionType.EXPENSE);

        ParsedTransactionRow row = new ParsedTransactionRow(
                null, BigDecimal.valueOf(100), "EXPENSE", "Coffee",
                "2025-01-15", "Food", 5L, null, null, false, null, false);

        ConfirmStatementRequest request = new ConfirmStatementRequest(1L, false, List.of(row));

        when(statementRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(statement));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(category));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        statementService.confirmImport(request, 1L);

        verify(transactionRepository).save(any());
        verify(accountRepository).save(account);
        verify(statementRepository, atLeastOnce()).save(statement);
        verify(notificationHelper).notify(eq(1L), eq(NotificationType.SUCCESS), anyString(), anyString());
    }
}
