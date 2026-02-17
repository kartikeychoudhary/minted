package com.minted.api.dto;

import java.math.BigDecimal;

public record TotalBalanceResponse(
        BigDecimal totalBalance,
        BigDecimal previousMonthBalance,
        BigDecimal incomeChangePercent,
        BigDecimal expenseChangePercent
) {}
