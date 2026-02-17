package com.minted.api.dto;

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
