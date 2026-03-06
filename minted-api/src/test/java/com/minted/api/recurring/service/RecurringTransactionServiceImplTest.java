package com.minted.api.recurring.service;

import com.minted.api.account.entity.Account;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.recurring.dto.RecurringSummaryResponse;
import com.minted.api.recurring.dto.RecurringTransactionRequest;
import com.minted.api.recurring.dto.RecurringTransactionResponse;
import com.minted.api.recurring.entity.RecurringTransaction;
import com.minted.api.recurring.enums.RecurringFrequency;
import com.minted.api.recurring.enums.RecurringStatus;
import com.minted.api.recurring.repository.RecurringTransactionRepository;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceImplTest {

    @Mock private RecurringTransactionRepository recurringRepo;
    @Mock private UserRepository userRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionCategoryRepository categoryRepository;

    @InjectMocks
    private RecurringTransactionServiceImpl recurringService;

    // ── getAllByUserId ─────────────────────────────────────────────────────────

    @Test
    void getAllByUserId_returnsList() {
        RecurringTransaction r = buildRecurring(1L, "Rent", RecurringStatus.ACTIVE);
        when(recurringRepo.findByUserId(1L)).thenReturn(List.of(r));

        List<RecurringTransactionResponse> result = recurringService.getAllByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Rent");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsResponse() {
        RecurringTransaction r = buildRecurring(1L, "Rent", RecurringStatus.ACTIVE);
        when(recurringRepo.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(r));

        RecurringTransactionResponse response = recurringService.getById(1L, 1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(recurringRepo.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recurringService.getById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_success() {
        User user = buildUser(1L);
        Account account = buildAccount(1L);
        TransactionCategory category = buildCategory(1L);
        RecurringTransaction saved = buildRecurring(10L, "Rent", RecurringStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(recurringRepo.save(any(RecurringTransaction.class))).thenReturn(saved);

        RecurringTransactionRequest request = new RecurringTransactionRequest(
                "Rent", BigDecimal.valueOf(1000), "EXPENSE", 1L, 1L,
                "MONTHLY", 1, LocalDate.now(), null);

        RecurringTransactionResponse response = recurringService.create(request, 1L);

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void create_userNotFound_throwsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recurringService.create(
                new RecurringTransactionRequest("Rent", BigDecimal.ONE, "EXPENSE", 1L, 1L,
                        "MONTHLY", 1, LocalDate.now(), null), 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_success() {
        RecurringTransaction r = buildRecurring(1L, "Rent", RecurringStatus.ACTIVE);
        when(recurringRepo.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(r));

        recurringService.delete(1L, 1L);

        verify(recurringRepo).delete(r);
    }

    @Test
    void delete_notFound_throwsResourceNotFound() {
        when(recurringRepo.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recurringService.delete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── toggleStatus ──────────────────────────────────────────────────────────

    @Test
    void toggleStatus_activeBecomespaused() {
        RecurringTransaction r = buildRecurring(1L, "Rent", RecurringStatus.ACTIVE);
        when(recurringRepo.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(r));
        when(recurringRepo.save(r)).thenReturn(r);

        recurringService.toggleStatus(1L, 1L);

        assertThat(r.getStatus()).isEqualTo(RecurringStatus.PAUSED);
    }

    @Test
    void toggleStatus_pausedBecomesActive() {
        RecurringTransaction r = buildRecurring(1L, "Rent", RecurringStatus.PAUSED);
        when(recurringRepo.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(r));
        when(recurringRepo.save(r)).thenReturn(r);

        recurringService.toggleStatus(1L, 1L);

        assertThat(r.getStatus()).isEqualTo(RecurringStatus.ACTIVE);
    }

    // ── getSummary ────────────────────────────────────────────────────────────

    @Test
    void getSummary_returnsCalculatedValues() {
        when(recurringRepo.sumAmountByUserIdAndStatusAndType(1L, RecurringStatus.ACTIVE, TransactionType.EXPENSE))
                .thenReturn(BigDecimal.valueOf(2000));
        when(recurringRepo.sumAmountByUserIdAndStatusAndType(1L, RecurringStatus.ACTIVE, TransactionType.INCOME))
                .thenReturn(BigDecimal.valueOf(5000));
        when(recurringRepo.countByUserIdAndStatus(1L, RecurringStatus.ACTIVE)).thenReturn(3L);
        when(recurringRepo.countByUserIdAndStatus(1L, RecurringStatus.PAUSED)).thenReturn(1L);

        RecurringSummaryResponse summary = recurringService.getSummary(1L);

        assertThat(summary.estimatedMonthlyExpenses()).isEqualByComparingTo("2000");
        assertThat(summary.estimatedMonthlyIncome()).isEqualByComparingTo("5000");
        assertThat(summary.scheduledNetFlux()).isEqualByComparingTo("3000");
        assertThat(summary.activeCount()).isEqualTo(3L);
    }

    // ── calculateNextExecutionDate ────────────────────────────────────────────

    @Test
    void calculateNextExecutionDate_startInFuture_usesStartMonth() {
        LocalDate futureStart = LocalDate.now().plusMonths(2).withDayOfMonth(1);
        LocalDate result = recurringService.calculateNextExecutionDate(futureStart, 15);

        assertThat(result.getDayOfMonth()).isEqualTo(15);
        assertThat(result).isAfterOrEqualTo(futureStart);
    }

    // helpers

    private RecurringTransaction buildRecurring(Long id, String name, RecurringStatus status) {
        RecurringTransaction r = new RecurringTransaction();
        r.setId(id);
        r.setName(name);
        r.setAmount(BigDecimal.valueOf(1000));
        r.setType(TransactionType.EXPENSE);
        r.setCategory(buildCategory(1L));
        r.setAccount(buildAccount(1L));
        r.setFrequency(RecurringFrequency.MONTHLY);
        r.setDayOfMonth(1);
        r.setStartDate(LocalDate.now());
        r.setStatus(status);
        return r;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("alice");
        return u;
    }

    private Account buildAccount(Long id) {
        Account a = new Account();
        a.setId(id);
        a.setName("Savings");
        return a;
    }

    private TransactionCategory buildCategory(Long id) {
        TransactionCategory c = new TransactionCategory();
        c.setId(id);
        c.setName("Rent");
        return c;
    }
}
