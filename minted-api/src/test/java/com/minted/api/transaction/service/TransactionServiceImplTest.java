package com.minted.api.transaction.service;

import com.minted.api.account.entity.Account;
import com.minted.api.account.entity.AccountType;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.split.repository.SplitTransactionRepository;
import com.minted.api.transaction.dto.TransactionRequest;
import com.minted.api.transaction.entity.Transaction;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionCategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private SplitTransactionRepository splitTransactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    // ── create — validation ───────────────────────────────────────────────────

    @Test
    void create_typeCategoryMismatch_throwsBadRequest() {
        stubUserAndAccountAndCategory(TransactionType.INCOME); // category is INCOME

        TransactionRequest request = buildRequest(TransactionType.EXPENSE, 1L, null);

        assertThatThrownBy(() -> transactionService.create(request, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must match category type");
    }

    @Test
    void create_transferWithNoToAccount_throwsBadRequest() {
        stubUserAndAccountAndCategory(TransactionType.TRANSFER);

        TransactionRequest request = buildRequest(TransactionType.TRANSFER, 1L, null); // toAccountId null

        assertThatThrownBy(() -> transactionService.create(request, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("To account is required");
    }

    @Test
    void create_transferSameAccount_throwsBadRequest() {
        stubUserAndAccountAndCategory(TransactionType.TRANSFER);
        // toAccount same as from account (both id=1)
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(buildAccount(1L, BigDecimal.ZERO)));

        TransactionRequest request = buildTransferRequest(1L, 1L);

        assertThatThrownBy(() -> transactionService.create(request, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be the same");
    }

    // ── create — balance updates ──────────────────────────────────────────────

    @Test
    void create_income_addsToBalance() {
        Account account = buildAccount(1L, BigDecimal.valueOf(100));
        User user = buildUser(1L);
        TransactionCategory category = buildCategory(1L, TransactionType.INCOME);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));

        Transaction saved = buildTransaction(1L, TransactionType.INCOME, BigDecimal.valueOf(50), account, null);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
        when(accountRepository.save(account)).thenReturn(account);

        transactionService.create(buildRequest(TransactionType.INCOME, 1L, null), 1L);

        assertThat(account.getBalance()).isEqualByComparingTo("150");
    }

    @Test
    void create_expense_subtractsFromBalance() {
        Account account = buildAccount(1L, BigDecimal.valueOf(200));
        User user = buildUser(1L);
        TransactionCategory category = buildCategory(1L, TransactionType.EXPENSE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));

        Transaction saved = buildTransaction(1L, TransactionType.EXPENSE, BigDecimal.valueOf(80), account, null);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
        when(accountRepository.save(account)).thenReturn(account);

        transactionService.create(buildRequest(TransactionType.EXPENSE, 1L, null), 1L);

        // buildRequest uses amount=50, so 200 - 50 = 150
        assertThat(account.getBalance()).isEqualByComparingTo("150");
    }

    @Test
    void create_transfer_updatesSourceAndDestination() {
        Account from = buildAccount(1L, BigDecimal.valueOf(500));
        Account to = buildAccount(2L, BigDecimal.valueOf(100));
        User user = buildUser(1L);
        TransactionCategory category = buildCategory(1L, TransactionType.TRANSFER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(from));
        when(accountRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(to));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));

        Transaction saved = buildTransaction(1L, TransactionType.TRANSFER, BigDecimal.valueOf(200), from, to);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
        when(accountRepository.save(any(Account.class))).thenReturn(from);

        transactionService.create(buildTransferRequest(1L, 2L), 1L);

        assertThat(from.getBalance()).isEqualByComparingTo("300");
        assertThat(to.getBalance()).isEqualByComparingTo("300");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_reversesBalanceAndDeletesTransaction() {
        Account account = buildAccount(1L, BigDecimal.valueOf(300));
        Transaction tx = buildTransaction(10L, TransactionType.EXPENSE, BigDecimal.valueOf(50), account, null);

        when(transactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(tx));
        when(accountRepository.save(account)).thenReturn(account);

        transactionService.delete(10L, 1L);

        // Expense reversal: add back 50 → 350
        assertThat(account.getBalance()).isEqualByComparingTo("350");
        verify(transactionRepository).delete(tx);
    }

    @Test
    void delete_notFound_throwsResourceNotFound() {
        when(transactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.delete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── bulkDelete ───────────────────────────────────────────────────────────

    @Test
    void bulkDelete_emptyList_throwsBadRequest() {
        assertThatThrownBy(() -> transactionService.bulkDelete(Collections.emptyList(), 1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void bulkDelete_deletesAll() {
        Account account = buildAccount(1L, BigDecimal.valueOf(100));
        Transaction tx1 = buildTransaction(1L, TransactionType.INCOME, BigDecimal.valueOf(50), account, null);
        Transaction tx2 = buildTransaction(2L, TransactionType.INCOME, BigDecimal.valueOf(30), account, null);

        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(tx1));
        when(transactionRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(tx2));
        when(accountRepository.save(account)).thenReturn(account);

        transactionService.bulkDelete(List.of(1L, 2L), 1L);

        verify(transactionRepository).delete(tx1);
        verify(transactionRepository).delete(tx2);
    }

    // ── bulkUpdateCategory ────────────────────────────────────────────────────

    @Test
    void bulkUpdateCategory_emptyList_throwsBadRequest() {
        assertThatThrownBy(() -> transactionService.bulkUpdateCategory(Collections.emptyList(), 5L, 1L))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void bulkUpdateCategory_updatesAllTransactions() {
        TransactionCategory newCategory = buildCategory(5L, TransactionType.EXPENSE);
        Account account = buildAccount(1L, BigDecimal.ZERO);
        Transaction tx = buildTransaction(1L, TransactionType.EXPENSE, BigDecimal.TEN, account, null);

        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(newCategory));
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(tx)).thenReturn(tx);

        transactionService.bulkUpdateCategory(List.of(1L), 5L, 1L);

        assertThat(tx.getCategory()).isEqualTo(newCategory);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_reversesOldBalanceThenAppliesNew() {
        Account account = buildAccount(1L, BigDecimal.valueOf(200));
        TransactionCategory oldCategory = buildCategory(1L, TransactionType.EXPENSE);
        Transaction tx = buildTransaction(10L, TransactionType.EXPENSE, BigDecimal.valueOf(50), account, null);
        tx.setCategory(oldCategory);

        TransactionCategory newCategory = buildCategory(2L, TransactionType.EXPENSE);
        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(80), TransactionType.EXPENSE, "desc", null,
                LocalDate.now(), 1L, null, 2L, false, null, false);

        when(transactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(tx));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(newCategory));
        when(transactionRepository.save(tx)).thenReturn(tx);
        when(accountRepository.save(account)).thenReturn(account);
        when(splitTransactionRepository.findSourceTransactionIdsByUserId(1L)).thenReturn(Collections.emptyList());

        transactionService.update(10L, request, 1L);

        // Old expense reversed: 200 + 50 = 250; new expense applied: 250 - 80 = 170
        assertThat(account.getBalance()).isEqualByComparingTo("170");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void stubUserAndAccountAndCategory(TransactionType categoryType) {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L)));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(buildAccount(1L, BigDecimal.ZERO)));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(buildCategory(1L, categoryType)));
    }

    private TransactionRequest buildRequest(TransactionType type, Long accountId, Long toAccountId) {
        return new TransactionRequest(
                BigDecimal.valueOf(50), type, "desc", null,
                LocalDate.now(), accountId, toAccountId, 1L, false, null, false);
    }

    private TransactionRequest buildTransferRequest(Long fromId, Long toId) {
        return new TransactionRequest(
                BigDecimal.valueOf(200), TransactionType.TRANSFER, "transfer", null,
                LocalDate.now(), fromId, toId, 1L, false, null, false);
    }

    private Account buildAccount(Long id, BigDecimal balance) {
        Account a = new Account();
        a.setId(id);
        a.setBalance(balance);
        a.setName("Account-" + id);
        AccountType at = new AccountType();
        at.setId(10L);
        at.setName("Bank");
        a.setAccountType(at);
        return a;
    }

    private TransactionCategory buildCategory(Long id, TransactionType type) {
        TransactionCategory c = new TransactionCategory();
        c.setId(id);
        c.setName("Category-" + id);
        c.setType(type);
        return c;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("alice");
        return u;
    }

    private Transaction buildTransaction(Long id, TransactionType type, BigDecimal amount,
                                          Account account, Account toAccount) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setType(type);
        t.setAmount(amount);
        t.setAccount(account);
        t.setToAccount(toAccount);
        t.setTransactionDate(LocalDate.now());
        t.setIsRecurring(false);
        t.setExcludeFromAnalysis(false);
        // TransactionResponse.from() reads category fields — provide a minimal one
        TransactionCategory cat = buildCategory(1L, type);
        t.setCategory(cat);
        return t;
    }
}
