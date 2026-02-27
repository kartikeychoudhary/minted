package com.minted.api.analytics.dto;

import java.math.BigDecimal;

public record BudgetSummaryResponse(
        Long budgetId,
        String budgetName,
        String categoryName,
        BigDecimal budgetedAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        double utilizationPercent
) {}
