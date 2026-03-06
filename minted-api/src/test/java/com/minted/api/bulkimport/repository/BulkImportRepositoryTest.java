package com.minted.api.bulkimport.repository;

import com.minted.api.account.entity.Account;
import com.minted.api.account.entity.AccountType;
import com.minted.api.bulkimport.entity.BulkImport;
import com.minted.api.bulkimport.enums.ImportStatus;
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
class BulkImportRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired BulkImportRepository bulkImportRepository;

    private User user1;
    private User user2;
    private Account account;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        AccountType at = em.persist(buildAccountType("Bank", user1));
        account = em.persist(buildAccount("Savings", user1, at));
        em.flush();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsUserImports() {
        em.persist(buildImport("import1.csv", user1, ImportStatus.COMPLETED));
        em.persist(buildImport("import2.csv", user1, ImportStatus.PENDING));
        em.persist(buildImport("other.csv", user2, ImportStatus.COMPLETED));
        em.flush();

        List<BulkImport> result = bulkImportRepository.findByUserIdOrderByCreatedAtDesc(user1.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void findByIdAndUserId_ownerFound() {
        BulkImport bi = em.persist(buildImport("import.csv", user1, ImportStatus.COMPLETED));
        em.flush();

        Optional<BulkImport> result = bulkImportRepository.findByIdAndUserId(bi.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getFileName()).isEqualTo("import.csv");
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {
        BulkImport bi = em.persist(buildImport("import.csv", user1, ImportStatus.COMPLETED));
        em.flush();

        Optional<BulkImport> result = bulkImportRepository.findByIdAndUserId(bi.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByStatus_returnsMatchingImports() {
        em.persist(buildImport("a.csv", user1, ImportStatus.PENDING));
        em.persist(buildImport("b.csv", user1, ImportStatus.COMPLETED));
        em.persist(buildImport("c.csv", user2, ImportStatus.PENDING));
        em.flush();

        List<BulkImport> result = bulkImportRepository.findByStatus(ImportStatus.PENDING);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(bi -> bi.getStatus() == ImportStatus.PENDING);
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

    private BulkImport buildImport(String fileName, User user, ImportStatus status) {
        BulkImport bi = new BulkImport();
        bi.setUser(user);
        bi.setAccount(account);
        bi.setFileName(fileName);
        bi.setStatus(status);
        return bi;
    }
}
