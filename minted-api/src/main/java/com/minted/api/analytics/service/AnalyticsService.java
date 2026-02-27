package com.minted.api.analytics.service;

import com.minted.api.analytics.dto.AnalyticsSummaryResponse;
import com.minted.api.analytics.dto.BudgetSummaryResponse;
import com.minted.api.analytics.dto.CategoryWiseResponse;
import com.minted.api.dashboard.dto.ChartDataResponse;
import com.minted.api.analytics.dto.SpendingActivityResponse;
import com.minted.api.analytics.dto.TotalBalanceResponse;
import com.minted.api.analytics.dto.TrendResponse;
import com.minted.api.transaction.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    AnalyticsSummaryResponse getSummary(Long userId, LocalDate startDate, LocalDate endDate);

    List<CategoryWiseResponse> getCategoryWise(Long userId, LocalDate startDate, LocalDate endDate, TransactionType type);

    List<TrendResponse> getTrend(Long userId, int months);

    ChartDataResponse getCardData(Long userId, Long cardId, LocalDate startDate, LocalDate endDate);

    List<SpendingActivityResponse> getSpendingActivity(Long userId, LocalDate startDate, LocalDate endDate);

    TotalBalanceResponse getTotalBalance(Long userId);

    List<BudgetSummaryResponse> getBudgetSummary(Long userId);
}
