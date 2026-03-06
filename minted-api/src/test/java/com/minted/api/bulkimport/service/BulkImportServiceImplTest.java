package com.minted.api.bulkimport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.account.entity.Account;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.bulkimport.dto.BulkImportResponse;
import com.minted.api.bulkimport.dto.CsvUploadResponse;
import com.minted.api.bulkimport.entity.BulkImport;
import com.minted.api.bulkimport.enums.ImportStatus;
import com.minted.api.bulkimport.enums.ImportType;
import com.minted.api.bulkimport.repository.BulkImportRepository;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.job.dto.JobExecutionResponse;
import com.minted.api.job.entity.JobExecution;
import com.minted.api.job.enums.JobStatus;
import com.minted.api.job.enums.JobTriggerType;
import com.minted.api.job.repository.JobExecutionRepository;
import com.minted.api.job.repository.JobScheduleConfigRepository;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkImportServiceImplTest {

    @Mock private BulkImportRepository bulkImportRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionCategoryRepository categoryRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private JobExecutionRepository jobExecutionRepository;
    @Mock private JobScheduleConfigRepository scheduleConfigRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private PlatformTransactionManager transactionManager;

    @InjectMocks
    private BulkImportServiceImpl bulkImportService;

    private User user;
    private Account account;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("alice");

        account = new Account();
        account.setId(1L);
        account.setName("Savings");
        account.setBalance(BigDecimal.valueOf(5000));
    }

    // ── getCsvTemplate ────────────────────────────────────────────────────────

    @Test
    void getCsvTemplate_returnsNonEmptyBytes() {
        byte[] template = bulkImportService.getCsvTemplate();

        assertThat(template).isNotEmpty();
        assertThat(new String(template)).contains("date,amount,type");
    }

    // ── getUserImports ────────────────────────────────────────────────────────

    @Test
    void getUserImports_returnsList() {
        BulkImport bi = buildImport(1L);
        when(bulkImportRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(bi));

        List<BulkImportResponse> result = bulkImportService.getUserImports(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
    }

    // ── getImportById ─────────────────────────────────────────────────────────

    @Test
    void getImportById_found_returnsResponse() {
        BulkImport bi = buildImport(1L);
        when(bulkImportRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bi));

        BulkImportResponse response = bulkImportService.getImportById(1L, 1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("VALIDATED");
    }

    @Test
    void getImportById_notFound_throwsResourceNotFound() {
        when(bulkImportRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bulkImportService.getImportById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getImportJobDetails ───────────────────────────────────────────────────

    @Test
    void getImportJobDetails_noJobExecution_throwsResourceNotFound() {
        BulkImport bi = buildImport(1L);
        bi.setJobExecution(null);
        when(bulkImportRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bi));

        assertThatThrownBy(() -> bulkImportService.getImportJobDetails(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getImportJobDetails_withJobExecution_returnsResponse() {
        BulkImport bi = buildImport(1L);
        JobExecution execution = new JobExecution();
        execution.setId(10L);
        execution.setJobName("BULK_IMPORT_PROCESSOR");
        execution.setStatus(JobStatus.COMPLETED);
        execution.setTriggerType(JobTriggerType.MANUAL);
        execution.setTotalSteps(4);
        execution.setCompletedSteps(4);
        bi.setJobExecution(execution);
        when(bulkImportRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(bi));

        JobExecutionResponse result = bulkImportService.getImportJobDetails(1L, 1L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.jobName()).isEqualTo("BULK_IMPORT_PROCESSOR");
        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.steps()).isEmpty();
    }

    // ── uploadAndValidate ─────────────────────────────────────────────────────

    @Test
    void uploadAndValidate_emptyCsvContent_throwsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "import.csv", "text/csv",
                "date,amount,type,description,categoryName\n".getBytes(StandardCharsets.UTF_8));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> bulkImportService.uploadAndValidate(file, 1L, 1L))
                .hasMessageContaining("empty");
    }

    @Test
    void uploadAndValidate_validCsv_returnsPreview() throws Exception {
        String csv = "date,amount,type,description,categoryName,notes,tags\n" +
                     "2026-01-15,1500.00,EXPENSE,Grocery shopping,Groceries,,\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "import.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        TransactionCategory category = new TransactionCategory();
        category.setId(5L);
        category.setName("Groceries");
        category.setType(TransactionType.EXPENSE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByNameIgnoreCaseAndUserIdAndType("Groceries", 1L, TransactionType.EXPENSE))
                .thenReturn(Optional.of(category));
        when(transactionRepository.existsByTransactionDateAndAmountAndDescriptionAndAccountIdAndUserId(
                eq(LocalDate.of(2026, 1, 15)), eq(new BigDecimal("1500.00")),
                eq("Grocery shopping"), eq(1L), eq(1L)))
                .thenReturn(false);
        when(bulkImportRepository.save(any())).thenAnswer(inv -> {
            BulkImport bi = inv.getArgument(0);
            bi.setId(99L);
            return bi;
        });

        CsvUploadResponse result = bulkImportService.uploadAndValidate(file, 1L, 1L);

        assertThat(result.importId()).isEqualTo(99L);
        assertThat(result.totalRows()).isEqualTo(1);
        assertThat(result.validRows()).isEqualTo(1);
        assertThat(result.errorRows()).isEqualTo(0);
        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0).status()).isEqualTo("VALID");
    }

    @Test
    void uploadAndValidate_invalidRowMissingCategory_marksRowError() throws Exception {
        String csv = "date,amount,type,description,categoryName,notes,tags\n" +
                     "2026-01-15,1500.00,EXPENSE,Grocery shopping,Unknown,,\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "import.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByNameIgnoreCaseAndUserIdAndType("Unknown", 1L, TransactionType.EXPENSE))
                .thenReturn(Optional.empty());
        when(bulkImportRepository.save(any())).thenAnswer(inv -> {
            BulkImport bi = inv.getArgument(0);
            bi.setId(100L);
            return bi;
        });

        CsvUploadResponse result = bulkImportService.uploadAndValidate(file, 1L, 1L);

        assertThat(result.errorRows()).isEqualTo(1);
        assertThat(result.rows().get(0).status()).isEqualTo("ERROR");
    }

    // helpers

    private BulkImport buildImport(Long id) {
        Account account = new Account();
        account.setId(1L);
        account.setName("Savings");

        BulkImport bi = new BulkImport();
        bi.setId(id);
        bi.setAccount(account);
        bi.setImportType(ImportType.CSV);
        bi.setFileName("import.csv");
        bi.setFileSize(1024L);
        bi.setTotalRows(10);
        bi.setValidRows(9);
        bi.setDuplicateRows(0);
        bi.setErrorRows(1);
        bi.setImportedRows(0);
        bi.setStatus(ImportStatus.VALIDATED);
        bi.setSkipDuplicates(false);
        return bi;
    }
}
