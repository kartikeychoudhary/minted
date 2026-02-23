package com.minted.api.budget.dto;

import com.minted.api.budget.entity.Budget;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetResponse(
        Long id,
        String name,
        BigDecimal amount,
        Integer month,
        Integer year,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getName(),
                budget.getAmount(),
                budget.getMonth(),
                budget.getYear(),
                budget.getCategory() != null ? budget.getCategory().getId() : null,
                budget.getCategory() != null ? budget.getCategory().getName() : null,
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}
