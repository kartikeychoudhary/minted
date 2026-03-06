package com.minted.api.transaction.repository;

import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionCategoryRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired TransactionCategoryRepository categoryRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        em.flush();
    }

    @Test
    void findByUserIdAndIsActiveTrue_returnsOnlyActive() {
        TransactionCategory active = em.persist(buildCategory("Food", TransactionType.EXPENSE, user1, true));
        TransactionCategory inactive = em.persist(buildCategory("OldCat", TransactionType.EXPENSE, user1, false));
        em.flush();

        List<TransactionCategory> result = categoryRepository.findByUserIdAndIsActiveTrue(user1.getId());

        assertThat(result).containsExactly(active);
        assertThat(result).doesNotContain(inactive);
    }

    @Test
    void findByIdAndUserId_correctOwner_found() {
        TransactionCategory cat = em.persist(buildCategory("Food", TransactionType.EXPENSE, user1, true));
        em.flush();

        Optional<TransactionCategory> result = categoryRepository.findByIdAndUserId(cat.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Food");
    }

    @Test
    void findByIdAndUserId_wrongOwner_empty() {
        TransactionCategory cat = em.persist(buildCategory("Food", TransactionType.EXPENSE, user1, true));
        em.flush();

        Optional<TransactionCategory> result = categoryRepository.findByIdAndUserId(cat.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdAndType_filtersCorrectly() {
        em.persist(buildCategory("Salary", TransactionType.INCOME, user1, true));
        em.persist(buildCategory("Food", TransactionType.EXPENSE, user1, true));
        em.flush();

        List<TransactionCategory> incomeResult = categoryRepository.findByUserIdAndTypeAndIsActiveTrue(user1.getId(), TransactionType.INCOME);
        List<TransactionCategory> expenseResult = categoryRepository.findByUserIdAndTypeAndIsActiveTrue(user1.getId(), TransactionType.EXPENSE);

        assertThat(incomeResult).hasSize(1).allMatch(c -> c.getType() == TransactionType.INCOME);
        assertThat(expenseResult).hasSize(1).allMatch(c -> c.getType() == TransactionType.EXPENSE);
    }

    @Test
    void existsByNameAndTypeAndUserId_existingCombo_true() {
        em.persist(buildCategory("Food", TransactionType.EXPENSE, user1, true));
        em.flush();

        assertThat(categoryRepository.existsByNameAndTypeAndUserId("Food", TransactionType.EXPENSE, user1.getId())).isTrue();
        assertThat(categoryRepository.existsByNameAndTypeAndUserId("Food", TransactionType.INCOME, user1.getId())).isFalse();
        assertThat(categoryRepository.existsByNameAndTypeAndUserId("Food", TransactionType.EXPENSE, user2.getId())).isFalse();
    }

    @Test
    void findByNameIgnoreCaseAndUserIdAndType_caseInsensitive() {
        em.persist(buildCategory("Food & Dining", TransactionType.EXPENSE, user1, true));
        em.flush();

        Optional<TransactionCategory> result = categoryRepository
                .findByNameIgnoreCaseAndUserIdAndType("food & dining", user1.getId(), TransactionType.EXPENSE);

        assertThat(result).isPresent();
    }

    private User buildUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("hashed");
        u.setIsActive(true);
        u.setForcePasswordChange(false);
        u.setRole("USER");
        return u;
    }

    private TransactionCategory buildCategory(String name, TransactionType type, User user, boolean active) {
        TransactionCategory c = new TransactionCategory();
        c.setName(name);
        c.setType(type);
        c.setUser(user);
        c.setIsActive(active);
        c.setIsDefault(false);
        return c;
    }
}
