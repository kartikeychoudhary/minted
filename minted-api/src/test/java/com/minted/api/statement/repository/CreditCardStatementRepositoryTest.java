package com.minted.api.statement.repository;

import com.minted.api.account.entity.Account;
import com.minted.api.account.entity.AccountType;
import com.minted.api.statement.entity.CreditCardStatement;
import com.minted.api.statement.enums.StatementStatus;
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
class CreditCardStatementRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired CreditCardStatementRepository statementRepository;

    private User user1;
    private User user2;
    private Account account;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        AccountType at = em.persist(buildAccountType("Credit Card", user1));
        account = em.persist(buildAccount("Visa", user1, at));
        em.flush();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsUserStatements() {
        em.persist(buildStatement("jan.pdf", user1));
        em.persist(buildStatement("feb.pdf", user1));
        em.persist(buildStatement("other.pdf", user2));
        em.flush();

        List<CreditCardStatement> result = statementRepository.findByUserIdOrderByCreatedAtDesc(user1.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void findByIdAndUserId_ownerFound() {
        CreditCardStatement stmt = em.persist(buildStatement("jan.pdf", user1));
        em.flush();

        Optional<CreditCardStatement> result = statementRepository.findByIdAndUserId(stmt.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getFileName()).isEqualTo("jan.pdf");
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {
        CreditCardStatement stmt = em.persist(buildStatement("jan.pdf", user1));
        em.flush();

        Optional<CreditCardStatement> result = statementRepository.findByIdAndUserId(stmt.getId(), user2.getId());

        assertThat(result).isEmpty();
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

    private AccountType buildAccountType(String name, User user) {
        AccountType at = new AccountType();
        at.setName(name);
        at.setUser(user);
        at.setIsActive(true);
        at.setIsDefault(false);
        return at;
    }

    private Account buildAccount(String name, User user, AccountType type) {
        Account a = new Account();
        a.setName(name);
        a.setUser(user);
        a.setAccountType(type);
        a.setIsActive(true);
        a.setBalance(BigDecimal.ZERO);
        a.setCurrency("INR");
        return a;
    }

    private CreditCardStatement buildStatement(String fileName, User user) {
        CreditCardStatement s = new CreditCardStatement();
        s.setUser(user);
        s.setAccount(account);
        s.setFileName(fileName);
        s.setStatus(StatementStatus.UPLOADED);
        s.setFileType("PDF");
        return s;
    }
}
