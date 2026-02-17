package com.minted.api.dto;

import com.minted.api.entity.RecurringTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecurringTransactionResponse(
    Long id,
    String name,
    BigDecimal amount,
    String type,
    Long categoryId,
    String categoryName,
    String categoryIcon,
    String categoryColor,
    Long accountId,
    String accountName,
    String frequency,
    Integer dayOfMonth,
    LocalDate startDate,
    LocalDate endDate,
    String status,
    LocalDate nextExecutionDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static RecurringTransactionResponse from(RecurringTransaction entity) {
        return new RecurringTransactionResponse(
            entity.getId(),
            entity.getName(),
            entity.getAmount(),
            entity.getType().name(),
            entity.getCategory().getId(),
            entity.getCategory().getName(),
            entity.getCategory().getIcon(),
            entity.getCategory().getColor(),
            entity.getAccount().getId(),
            entity.getAccount().getName(),
            entity.getFrequency().name(),
            entity.getDayOfMonth(),
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getStatus().name(),
            entity.getNextExecutionDate(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
