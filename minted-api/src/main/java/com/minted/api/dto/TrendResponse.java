package com.minted.api.dto;

import java.math.BigDecimal;

public record TrendResponse(
        String month,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {}
