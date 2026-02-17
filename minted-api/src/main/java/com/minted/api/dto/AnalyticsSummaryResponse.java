package com.minted.api.dto;

import java.math.BigDecimal;

public record AnalyticsSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        Long transactionCount
) {}
