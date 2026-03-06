package com.minted.api.budget.service;

import com.minted.api.budget.dto.BudgetRequest;
import com.minted.api.budget.dto.BudgetResponse;
import com.minted.api.budget.entity.Budget;
import com.minted.api.budget.repository.BudgetRepository;
import com.minted.api.common.exception.DuplicateResourceException;
import com.minted.api.common.exception.ResourceNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private TransactionCategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_duplicateBudget_throwsDuplicateResourceException() {
        BudgetRequest request = new BudgetRequest("Food", BigDecimal.valueOf(500), 3, 2026, 1L);
        when(budgetRepository.existsByUserIdAndMonthAndYearAndCategoryId(1L, 3, 2026, 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> budgetService.create(request, 1L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void create_withCategory_success() {
        BudgetRequest request = new BudgetRequest("Food", BigDecimal.valueOf(500), 3, 2026, 1L);
        User user = buildUser(1L);
        TransactionCategory category = buildCategory(1L);
        Budget saved = buildBudget(10L, "Food", 3, 2026, category);

        when(budgetRepository.existsByUserIdAndMonthAndYearAndCategoryId(1L, 3, 2026, 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));
        when(budgetRepository.save(any(Budget.class))).thenReturn(saved);

        BudgetResponse response = budgetService.create(request, 1L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.categoryId()).isEqualTo(1L);
    }

    @Test
    void create_withoutCategory_success() {
        BudgetRequest request = new BudgetRequest("All Expenses", BigDecimal.valueOf(1000), 3, 2026, null);
        User user = buildUser(1L);
        Budget saved = buildBudget(11L, "All Expenses", 3, 2026, null);

        when(budgetRepository.existsByUserIdAndMonthAndYearAndCategoryId(1L, 3, 2026, null)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(budgetRepository.save(any(Budget.class))).thenReturn(saved);

        BudgetResponse response = budgetService.create(request, 1L);

        assertThat(response.categoryId()).isNull();
        verify(categoryRepository, never()).findByIdAndUserId(anyLong(), anyLong());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_changingToExistingMonthYearCategory_throwsDuplicate() {
        Budget existing = buildBudget(1L, "Food", 3, 2026, buildCategory(1L));
        BudgetRequest request = new BudgetRequest("Food", BigDecimal.valueOf(500), 4, 2026, 1L); // month changed

        when(budgetRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(budgetRepository.existsByUserIdAndMonthAndYearAndCategoryId(1L, 4, 2026, 1L)).thenReturn(true);

        assertThatThrownBy(() -> budgetService.update(1L, request, 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void update_noChangeToCombination_skipsDuplicateCheck() {
        TransactionCategory category = buildCategory(1L);
        Budget existing = buildBudget(1L, "Food", 3, 2026, category);
        // same month/year/category — no combination change
        BudgetRequest request = new BudgetRequest("Food Updated", BigDecimal.valueOf(600), 3, 2026, 1L);

        when(budgetRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(category));
        when(budgetRepository.save(existing)).thenReturn(existing);

        budgetService.update(1L, request, 1L);

        verify(budgetRepository, never()).existsByUserIdAndMonthAndYearAndCategoryId(anyLong(), anyInt(), anyInt(), any());
    }

    @Test
    void update_notFound_throwsResourceNotFound() {
        when(budgetRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.update(99L, new BudgetRequest("x", BigDecimal.ONE, 1, 2026, null), 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_success() {
        Budget budget = buildBudget(1L, "Food", 3, 2026, null);
        when(budgetRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(budget));

        budgetService.delete(1L, 1L);

        verify(budgetRepository).delete(budget);
    }

    @Test
    void delete_notFound_throwsResourceNotFound() {
        when(budgetRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.delete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(budgetRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetService.getById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_found_returnsResponse() {
        Budget budget = buildBudget(1L, "Food", 3, 2026, null);
        when(budgetRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(budget));

        BudgetResponse response = budgetService.getById(1L, 1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Food");
    }

    // ── getAllByUserId ─────────────────────────────────────────────────────────

    @Test
    void getAllByUserId_returnsList() {
        Budget b = buildBudget(1L, "Food", 3, 2026, null);
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(b));

        List<BudgetResponse> result = budgetService.getAllByUserId(1L);

        assertThat(result).hasSize(1);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Budget buildBudget(Long id, String name, int month, int year, TransactionCategory category) {
        Budget b = new Budget();
        b.setId(id);
        b.setName(name);
        b.setAmount(BigDecimal.valueOf(500));
        b.setMonth(month);
        b.setYear(year);
        b.setCategory(category);
        return b;
    }

    private TransactionCategory buildCategory(Long id) {
        TransactionCategory c = new TransactionCategory();
        c.setId(id);
        c.setName("Food & Dining");
        c.setType(TransactionType.EXPENSE);
        return c;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("alice");
        return u;
    }
}
