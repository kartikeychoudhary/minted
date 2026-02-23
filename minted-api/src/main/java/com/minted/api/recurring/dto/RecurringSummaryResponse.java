package com.minted.api.recurring.dto;

import java.math.BigDecimal;

public record RecurringSummaryResponse(
    BigDecimal estimatedMonthlyExpenses,
    BigDecimal estimatedMonthlyIncome,
    BigDecimal scheduledNetFlux,
    Long activeCount,
    Long pausedCount
) {}
