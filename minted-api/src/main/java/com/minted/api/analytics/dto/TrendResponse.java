package com.minted.api.analytics.dto;

import java.math.BigDecimal;

public record TrendResponse(
        String month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {}
