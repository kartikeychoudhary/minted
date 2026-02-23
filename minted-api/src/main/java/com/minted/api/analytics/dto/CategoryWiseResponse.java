package com.minted.api.analytics.dto;

import java.math.BigDecimal;

public record CategoryWiseResponse(
        Long categoryId,
        String categoryName,
        String icon,
        String color,
        BigDecimal totalAmount,
        Long transactionCount,
        Double percentage
) {}
