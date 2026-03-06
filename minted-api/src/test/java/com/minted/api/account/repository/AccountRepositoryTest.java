package com.minted.api.account.repository;

import com.minted.api.account.entity.Account;
import com.minted.api.account.entity.AccountType;
import com.minted.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired AccountRepository accountRepository;

    private User user1;
    private User user2;
    private AccountType accountType;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        accountType = em.persist(buildAccountType(user1, "Bank"));
        em.flush();
    }

    @Test
    void findByUserIdAndIsActiveTrue_returnsOnlyActiveAccounts() {
        Account active = em.persist(buildAccount("Savings", user1, accountType, true));
        Account inactive = em.persist(buildAccount("OldAccount", user1, accountType, false));
        em.flush();

        List<Account> result = accountRepository.findByUserIdAndIsActiveTrue(user1.getId());

        assertThat(result).containsExactly(active);
        assertThat(result).doesNotContain(inactive);
    }

    @Test
    void findByIdAndUserId_ownerFound_returnsAccount() {
        Account account = em.persist(buildAccount("Savings", user1, accountType, true));
        em.flush();

        Optional<Account> result = accountRepository.findByIdAndUserId(account.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Savings");
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {
        Account account = em.persist(buildAccount("Savings", user1, accountType, true));
        em.flush();

        Optional<Account> result = accountRepository.findByIdAndUserId(account.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void existsByNameAndUserId_existingName_returnsTrue() {
        em.persist(buildAccount("Savings", user1, accountType, true));
        em.flush();

        assertThat(accountRepository.existsByNameAndUserId("Savings", user1.getId())).isTrue();
        assertThat(accountRepository.existsByNameAndUserId("Savings", user2.getId())).isFalse();
    }

    @Test
    void findByNameAndUserIdAndIsActiveFalse_softDeleted_found() {
        Account deleted = em.persist(buildAccount("OldSavings", user1, accountType, false));
        em.flush();

        Optional<Account> result = accountRepository.findByNameAndUserIdAndIsActiveFalse("OldSavings", user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(deleted.getId());
    }

    @Test
    void findActiveAccountsByUserIdOrderByBalanceDesc_orderedByBalance() {
        Account a1 = buildAccount("Low", user1, accountType, true); a1.setBalance(BigDecimal.valueOf(100));
        Account a2 = buildAccount("High", user1, accountType, true); a2.setBalance(BigDecimal.valueOf(500));
        Account a3 = buildAccount("Mid", user1, accountType, true); a3.setBalance(BigDecimal.valueOf(300));
        em.persist(a1); em.persist(a2); em.persist(a3);
        em.flush();

        List<Account> result = accountRepository.findActiveAccountsByUserIdOrderByBalanceDesc(user1.getId());

        assertThat(result.get(0).getName()).isEqualTo("High");
        assertThat(result.get(1).getName()).isEqualTo("Mid");
        assertThat(result.get(2).getName()).isEqualTo("Low");
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

    private AccountType buildAccountType(User user, String name) {
        AccountType at = new AccountType();
        at.setName(name);
        at.setUser(user);
        at.setIsActive(true);
        at.setIsDefault(false);
        return at;
    }

    private Account buildAccount(String name, User user, AccountType type, boolean active) {
        Account a = new Account();
        a.setName(name);
        a.setUser(user);
        a.setAccountType(type);
        a.setIsActive(active);
        a.setBalance(BigDecimal.ZERO);
        a.setCurrency("INR");
        return a;
    }
}
