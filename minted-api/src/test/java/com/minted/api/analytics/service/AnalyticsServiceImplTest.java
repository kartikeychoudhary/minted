package com.minted.api.analytics.service;

import com.minted.api.analytics.dto.AnalyticsSummaryResponse;
import com.minted.api.analytics.dto.BudgetSummaryResponse;
import com.minted.api.analytics.dto.CategoryWiseResponse;
import com.minted.api.analytics.dto.SpendingActivityResponse;
import com.minted.api.analytics.dto.TotalBalanceResponse;
import com.minted.api.analytics.dto.TrendResponse;
import com.minted.api.account.entity.Account;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.budget.entity.Budget;
import com.minted.api.budget.repository.BudgetRepository;
import com.minted.api.dashboard.repository.DashboardCardRepository;
import com.minted.api.dashboardconfig.service.DashboardConfigService;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private DashboardCardRepository dashboardCardRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private DashboardConfigService dashboardConfigService;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    // ── getSummary ────────────────────────────────────────────────────────────

    @Test
    void getSummary_noExcludedCategories_returnsCalculatedValues() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        when(dashboardConfigService.getExcludedCategoryIds(1L)).thenReturn(List.of());
        when(transactionRepository.sumAmountFiltered(eq(1L), eq(TransactionType.INCOME), eq(start), eq(end), anyList(), isNull()))
                .thenReturn(BigDecimal.valueOf(5000));
        when(transactionRepository.sumAmountFiltered(eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end), anyList(), isNull()))
                .thenReturn(BigDecimal.valueOf(2000));
        when(transactionRepository.countFiltered(eq(1L), eq(start), eq(end), anyList(), isNull()))
                .thenReturn(10L);

        AnalyticsSummaryResponse result = analyticsService.getSummary(1L, start, end, null);

        assertThat(result.totalIncome()).isEqualByComparingTo("5000");
        assertThat(result.totalExpense()).isEqualByComparingTo("2000");
        assertThat(result.netBalance()).isEqualByComparingTo("3000");
        assertThat(result.transactionCount()).isEqualTo(10L);
    }

    @Test
    void getSummary_nullAmounts_treatedAsZero() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        when(dashboardConfigService.getExcludedCategoryIds(1L)).thenReturn(List.of());
        when(transactionRepository.sumAmountFiltered(eq(1L), eq(TransactionType.INCOME), eq(start), eq(end), anyList(), isNull()))
                .thenReturn(null);
        when(transactionRepository.sumAmountFiltered(eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end), anyList(), isNull()))
                .thenReturn(null);
        when(transactionRepository.countFiltered(eq(1L), eq(start), eq(end), anyList(), isNull()))
                .thenReturn(0L);

        AnalyticsSummaryResponse result = analyticsService.getSummary(1L, start, end, null);

        assertThat(result.totalIncome()).isEqualByComparingTo("0");
        assertThat(result.totalExpense()).isEqualByComparingTo("0");
        assertThat(result.netBalance()).isEqualByComparingTo("0");
    }

    // ── getCategoryWise ───────────────────────────────────────────────────────

    @Test
    void getCategoryWise_returnsFormattedList() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        // Object[]: categoryId, categoryName, amount, count, icon, color
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{1L, "Food", BigDecimal.valueOf(1000), 5L, "pi-food", "#ff0000"});

        when(dashboardConfigService.getExcludedCategoryIds(1L)).thenReturn(List.of());
        when(transactionRepository.sumAmountGroupedByCategoryFiltered(eq(1L), eq(TransactionType.EXPENSE), eq(start), eq(end), anyList(), isNull()))
                .thenReturn(rows);

        List<CategoryWiseResponse> result = analyticsService.getCategoryWise(1L, start, end, TransactionType.EXPENSE, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).categoryName()).isEqualTo("Food");
        assertThat(result.get(0).totalAmount()).isEqualByComparingTo("1000");
        assertThat(result.get(0).percentage()).isEqualTo(100.0);
    }

    @Test
    void getCategoryWise_emptyResult_returnsEmptyList() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 31);

        when(dashboardConfigService.getExcludedCategoryIds(1L)).thenReturn(List.of());
        when(transactionRepository.sumAmountGroupedByCategoryFiltered(any(), any(), any(), any(), anyList(), any()))
                .thenReturn(List.of());

        List<CategoryWiseResponse> result = analyticsService.getCategoryWise(1L, start, end, TransactionType.EXPENSE, null);

        assertThat(result).isEmpty();
    }

    // ── getTrend ──────────────────────────────────────────────────────────────

    @Test
    void getTrend_noData_returnsZeroFilledMonths() {
        when(dashboardConfigService.getExcludedCategoryIds(1L)).thenReturn(List.of());
        when(transactionRepository.sumAmountGroupedByMonthFiltered(any(), any(), any(), any(), anyList(), any()))
                .thenReturn(List.of());

        List<TrendResponse> result = analyticsService.getTrend(1L, 3, null);

        assertThat(result).hasSize(3);
        result.forEach(r -> {
            assertThat(r.income()).isEqualByComparingTo("0");
            assertThat(r.expense()).isEqualByComparingTo("0");
            assertThat(r.net()).isEqualByComparingTo("0");
        });
    }

    // ── getSpendingActivity ───────────────────────────────────────────────────

    @Test
    void getSpendingActivity_noData_returnsZeroForEachDay() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 1, 3);

        when(dashboardConfigService.getExcludedCategoryIds(1L)).thenReturn(List.of());
        when(transactionRepository.sumExpenseGroupedByDate(eq(1L), eq(start), eq(end)))
                .thenReturn(List.of());

        List<SpendingActivityResponse> result = analyticsService.getSpendingActivity(1L, start, end);

        assertThat(result).hasSize(3); // Jan 1, 2, 3
        result.forEach(r -> assertThat(r.amount()).isEqualByComparingTo("0"));
    }

    // ── getTotalBalance ───────────────────────────────────────────────────────

    @Test
    void getTotalBalance_sumsActiveAccountBalances() {
        Account a1 = new Account();
        a1.setBalance(BigDecimal.valueOf(10000));
        Account a2 = new Account();
        a2.setBalance(BigDecimal.valueOf(5000));

        when(accountRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(List.of(a1, a2));
        when(dashboardConfigService.getExcludedCategoryIds(1L)).thenReturn(List.of());
        when(transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(eq(1L), any(TransactionType.class), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        TotalBalanceResponse result = analyticsService.getTotalBalance(1L);

        assertThat(result.totalBalance()).isEqualByComparingTo("15000");
    }

    // ── getBudgetSummary ──────────────────────────────────────────────────────

    @Test
    void getBudgetSummary_noBudgets_returnsEmptyList() {
        when(budgetRepository.findByUserIdAndMonthAndYear(eq(1L), anyInt(), anyInt()))
                .thenReturn(List.of());

        List<BudgetSummaryResponse> result = analyticsService.getBudgetSummary(1L);

        assertThat(result).isEmpty();
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getBudgetSummary_withBudget_calculatesUtilization() {
        Budget budget = new Budget();
        budget.setId(1L);
        budget.setName("Monthly Food");
        budget.setAmount(BigDecimal.valueOf(5000));
        budget.setCategory(null); // tracks all expenses

        when(budgetRepository.findByUserIdAndMonthAndYear(eq(1L), anyInt(), anyInt()))
                .thenReturn(List.of(budget));
        when(dashboardConfigService.getExcludedCategoryIds(1L)).thenReturn(List.of());
        // Object[]: categoryId, categoryName, amount, count, icon, color
        List<Object[]> catRows = new ArrayList<>();
        catRows.add(new Object[]{1L, "Food", BigDecimal.valueOf(2500), 10L, null, null});
        when(transactionRepository.sumAmountGroupedByCategory(eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(catRows);

        List<BudgetSummaryResponse> result = analyticsService.getBudgetSummary(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).budgetedAmount()).isEqualByComparingTo("5000");
        assertThat(result.get(0).spentAmount()).isEqualByComparingTo("2500");
        assertThat(result.get(0).utilizationPercent()).isEqualTo(50.0);
    }
}
