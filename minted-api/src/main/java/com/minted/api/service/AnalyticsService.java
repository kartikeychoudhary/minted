package com.minted.api.service;

import com.minted.api.dto.AnalyticsSummaryResponse;
import com.minted.api.dto.CategoryWiseResponse;
import com.minted.api.dto.ChartDataResponse;
import com.minted.api.dto.TrendResponse;
import com.minted.api.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    AnalyticsSummaryResponse getSummary(Long userId, LocalDate startDate, LocalDate endDate);

    List<CategoryWiseResponse> getCategoryWise(Long userId, LocalDate startDate, LocalDate endDate, TransactionType type);

    List<TrendResponse> getTrend(Long userId, int months);

    ChartDataResponse getCardData(Long userId, Long cardId, LocalDate startDate, LocalDate endDate);
}
