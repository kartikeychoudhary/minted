package com.minted.api.transaction.repository;

import com.minted.api.account.entity.Account;
import com.minted.api.account.entity.AccountType;
import com.minted.api.transaction.entity.Transaction;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired TransactionRepository transactionRepository;

    private User user1;
    private User user2;
    private Account account;
    private TransactionCategory category;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        AccountType at = em.persist(buildAccountType(user1));
        account = em.persist(buildAccount(user1, at));
        category = em.persist(buildCategory(user1));
        em.flush();
    }

    // ── findByUserId ──────────────────────────────────────────────────────────

    @Test
    void findByUserId_returnsOnlyOwnTransactions() {
        Account acct2 = em.persist(buildAccount(user2, em.persist(buildAccountType(user2))));
        TransactionCategory cat2 = em.persist(buildCategory(user2));
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(100), TransactionType.EXPENSE, LocalDate.now()));
        em.persist(buildTx(user2, acct2, cat2, BigDecimal.valueOf(200), TransactionType.INCOME, LocalDate.now()));
        em.flush();

        List<Transaction> result = transactionRepository.findByUserId(user1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("100");
    }

    // ── findByIdAndUserId ─────────────────────────────────────────────────────

    @Test
    void findByIdAndUserId_correctOwner_returnsTransaction() {
        Transaction tx = em.persist(buildTx(user1, account, category, BigDecimal.valueOf(50), TransactionType.EXPENSE, LocalDate.now()));
        em.flush();

        Optional<Transaction> result = transactionRepository.findByIdAndUserId(tx.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualByComparingTo("50");
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {
        Transaction tx = em.persist(buildTx(user1, account, category, BigDecimal.valueOf(50), TransactionType.EXPENSE, LocalDate.now()));
        em.flush();

        Optional<Transaction> result = transactionRepository.findByIdAndUserId(tx.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    // ── findByUserIdAndTransactionDateBetween ─────────────────────────────────

    @Test
    void findByUserIdAndTransactionDateBetween_returnsInRange() {
        LocalDate today = LocalDate.now();
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(100), TransactionType.EXPENSE, today));
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(200), TransactionType.EXPENSE, today.minusDays(40)));
        em.flush();

        List<Transaction> result = transactionRepository.findByUserIdAndTransactionDateBetween(
                user1.getId(), today.minusDays(30), today);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("100");
    }

    // ── findByUserIdAndType ───────────────────────────────────────────────────

    @Test
    void findByUserIdAndType_filtersByType() {
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(100), TransactionType.EXPENSE, LocalDate.now()));
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(500), TransactionType.INCOME, LocalDate.now()));
        em.flush();

        List<Transaction> expenses = transactionRepository.findByUserIdAndType(user1.getId(), TransactionType.EXPENSE);

        assertThat(expenses).hasSize(1);
        assertThat(expenses.get(0).getType()).isEqualTo(TransactionType.EXPENSE);
    }

    // ── findByUserIdAndAccountId ──────────────────────────────────────────────

    @Test
    void findByUserIdAndAccountId_filtersByAccount() {
        AccountType at2 = em.persist(buildAccountType(user1));
        Account account2 = em.persist(buildAccount(user1, at2));
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(100), TransactionType.EXPENSE, LocalDate.now()));
        em.persist(buildTx(user1, account2, category, BigDecimal.valueOf(200), TransactionType.EXPENSE, LocalDate.now()));
        em.flush();

        List<Transaction> result = transactionRepository.findByUserIdAndAccountId(user1.getId(), account.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccount().getId()).isEqualTo(account.getId());
    }

    // ── existsByTransactionDateAndAmountAndDescriptionAndAccountIdAndUserId ───

    @Test
    void existsByDuplicateFields_trueWhenExists() {
        LocalDate date = LocalDate.now();
        Transaction tx = buildTx(user1, account, category, BigDecimal.valueOf(100), TransactionType.EXPENSE, date);
        tx.setDescription("Coffee");
        em.persist(tx);
        em.flush();

        boolean exists = transactionRepository.existsByTransactionDateAndAmountAndDescriptionAndAccountIdAndUserId(
                date, BigDecimal.valueOf(100), "Coffee", account.getId(), user1.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByDuplicateFields_falseWhenNotExists() {
        boolean exists = transactionRepository.existsByTransactionDateAndAmountAndDescriptionAndAccountIdAndUserId(
                LocalDate.now(), BigDecimal.valueOf(999), "NoSuchTx", account.getId(), user1.getId());

        assertThat(exists).isFalse();
    }

    // ── findByUserIdAndDateRangeOrderByDateDesc (named query) ─────────────────

    @Test
    void findByUserIdAndDateRangeOrderByDateDesc_orderedDescending() {
        LocalDate today = LocalDate.now();
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(100), TransactionType.EXPENSE, today.minusDays(2)));
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(200), TransactionType.EXPENSE, today));
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(300), TransactionType.EXPENSE, today.minusDays(1)));
        em.flush();

        List<Transaction> result = transactionRepository.findByUserIdAndDateRangeOrderByDateDesc(
                user1.getId(), today.minusDays(5), today);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("200"); // newest first
        assertThat(result.get(2).getAmount()).isEqualByComparingTo("100"); // oldest last
    }

    // ── sumAmountByUserIdAndTypeAndDateBetween (named query) ──────────────────

    @Test
    void sumAmountByUserIdAndTypeAndDateBetween_sumsCorrectly() {
        LocalDate today = LocalDate.now();
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(100), TransactionType.EXPENSE, today));
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(50), TransactionType.EXPENSE, today.minusDays(5)));
        em.persist(buildTx(user1, account, category, BigDecimal.valueOf(500), TransactionType.INCOME, today)); // different type
        em.flush();

        BigDecimal sum = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                user1.getId(), TransactionType.EXPENSE, today.minusDays(30), today);

        assertThat(sum).isEqualByComparingTo("150");
    }

    // helpers

    private User buildUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("hashed");
        u.setIsActive(true);
        u.setForcePasswordChange(false);
        u.setRole("USER");
        return u;
    }

    private AccountType buildAccountType(User user) {
        AccountType at = new AccountType();
        at.setName("Bank");
        at.setUser(user);
        at.setIsActive(true);
        at.setIsDefault(false);
        return at;
    }

    private Account buildAccount(User user, AccountType type) {
        Account a = new Account();
        a.setName("Savings");
        a.setUser(user);
        a.setAccountType(type);
        a.setIsActive(true);
        a.setBalance(BigDecimal.ZERO);
        a.setCurrency("INR");
        return a;
    }

    private TransactionCategory buildCategory(User user) {
        TransactionCategory c = new TransactionCategory();
        c.setName("Food");
        c.setType(TransactionType.EXPENSE);
        c.setIsActive(true);
        c.setIsDefault(false);
        c.setUser(user);
        return c;
    }

    private Transaction buildTx(User user, Account acct, TransactionCategory cat,
                                 BigDecimal amount, TransactionType type, LocalDate date) {
        Transaction t = new Transaction();
        t.setUser(user);
        t.setAccount(acct);
        t.setCategory(cat);
        t.setAmount(amount);
        t.setType(type);
        t.setTransactionDate(date);
        t.setExcludeFromAnalysis(false);
        return t;
    }
}
