package com.minted.api.bulkimport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.account.entity.Account;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.bulkimport.dto.BulkImportResponse;
import com.minted.api.bulkimport.entity.BulkImport;
import com.minted.api.bulkimport.enums.ImportStatus;
import com.minted.api.bulkimport.enums.ImportType;
import com.minted.api.bulkimport.repository.BulkImportRepository;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.job.repository.JobExecutionRepository;
import com.minted.api.job.repository.JobScheduleConfigRepository;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
