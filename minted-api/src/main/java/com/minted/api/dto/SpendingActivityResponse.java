package com.minted.api.dto;

import java.math.BigDecimal;

public record SpendingActivityResponse(
        String date,
        String dayLabel,
        BigDecimal amount
) {}
