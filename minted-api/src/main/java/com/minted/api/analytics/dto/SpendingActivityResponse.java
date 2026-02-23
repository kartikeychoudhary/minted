package com.minted.api.analytics.dto;

import java.math.BigDecimal;

public record SpendingActivityResponse(
        String date,
        String dayLabel,
        BigDecimal amount
) {}
