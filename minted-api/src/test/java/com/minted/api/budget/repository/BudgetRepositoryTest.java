package com.minted.api.budget.repository;

import com.minted.api.budget.entity.Budget;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BudgetRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired BudgetRepository budgetRepository;

    private User user1;
    private User user2;
    private TransactionCategory category;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        category = em.persist(buildCategory("Food", user1));
        em.flush();
    }

    @Test
    void findByUserId_returnsAllBudgetsForUser() {
        em.persist(buildBudget("Food Budget", user1, 3, 2026, category));
        em.persist(buildBudget("Total", user1, 4, 2026, null));
        em.persist(buildBudget("Other User", user2, 3, 2026, null));
        em.flush();

        List<Budget> result = budgetRepository.findByUserId(user1.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void findByIdAndUserId_ownerFound() {
        Budget b = em.persist(buildBudget("Food", user1, 3, 2026, null));
        em.flush();

        Optional<Budget> result = budgetRepository.findByIdAndUserId(b.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Food");
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {
        Budget b = em.persist(buildBudget("Food", user1, 3, 2026, null));
        em.flush();

        Optional<Budget> result = budgetRepository.findByIdAndUserId(b.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdAndMonthAndYear_filtersByMonthYear() {
        em.persist(buildBudget("March Food", user1, 3, 2026, category));
        em.persist(buildBudget("April Food", user1, 4, 2026, category));
        em.flush();

        List<Budget> result = budgetRepository.findByUserIdAndMonthAndYear(user1.getId(), 3, 2026);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("March Food");
    }

    @Test
    void existsByUserIdAndMonthAndYearAndCategoryId_exists_returnsTrue() {
        em.persist(buildBudget("Food", user1, 3, 2026, category));
        em.flush();

        boolean exists = budgetRepository.existsByUserIdAndMonthAndYearAndCategoryId(
                user1.getId(), 3, 2026, category.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndMonthAndYearAndCategoryId_notExists_returnsFalse() {
        boolean exists = budgetRepository.existsByUserIdAndMonthAndYearAndCategoryId(
                user1.getId(), 3, 2026, category.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void findByUserIdAndYear_returnsOrderedByMonth() {
        em.persist(buildBudget("April", user1, 4, 2026, null));
        em.persist(buildBudget("Feb", user1, 2, 2026, null));
        em.persist(buildBudget("March", user1, 3, 2026, null));
        em.persist(buildBudget("Other Year", user1, 1, 2025, null));
        em.flush();

        List<Budget> result = budgetRepository.findByUserIdAndYear(user1.getId(), 2026);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getMonth()).isEqualTo(2);
        assertThat(result.get(1).getMonth()).isEqualTo(3);
        assertThat(result.get(2).getMonth()).isEqualTo(4);
    }

    @Test
    void findByUserIdAndYearAndMonthBetween_returnsInRange() {
        em.persist(buildBudget("Jan", user1, 1, 2026, null));
        em.persist(buildBudget("Feb", user1, 2, 2026, null));
        em.persist(buildBudget("March", user1, 3, 2026, null));
        em.persist(buildBudget("June", user1, 6, 2026, null));
        em.flush();

        List<Budget> result = budgetRepository.findByUserIdAndYearAndMonthBetween(
                user1.getId(), 2026, 2, 4);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMonth()).isEqualTo(2);
        assertThat(result.get(1).getMonth()).isEqualTo(3);
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

    private TransactionCategory buildCategory(String name, User user) {
        TransactionCategory c = new TransactionCategory();
        c.setName(name);
        c.setType(TransactionType.EXPENSE);
        c.setIsActive(true);
        c.setIsDefault(false);
        c.setUser(user);
        return c;
    }

    private Budget buildBudget(String name, User user, int month, int year, TransactionCategory category) {
        Budget b = new Budget();
        b.setName(name);
        b.setUser(user);
        b.setMonth(month);
        b.setYear(year);
        b.setAmount(BigDecimal.valueOf(500));
        b.setCategory(category);
        return b;
    }
}
