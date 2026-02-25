package com.minted.api.transaction.dto;

import com.minted.api.transaction.entity.Transaction;
import com.minted.api.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        TransactionType type,
        String description,
        String notes,
        LocalDate transactionDate,
        Long accountId,
        String accountName,
        Long toAccountId,
        String toAccountName,
        Long categoryId,
        String categoryName,
        String categoryIcon,
        String categoryColor,
        Boolean isRecurring,
        String tags,
        Boolean isSplit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TransactionResponse from(Transaction transaction, boolean isSplit) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getDescription(),
                transaction.getNotes(),
                transaction.getTransactionDate(),
                transaction.getAccount().getId(),
                transaction.getAccount().getName(),
                transaction.getToAccount() != null ? transaction.getToAccount().getId() : null,
                transaction.getToAccount() != null ? transaction.getToAccount().getName() : null,
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getCategory().getIcon(),
                transaction.getCategory().getColor(),
                transaction.getIsRecurring(),
                transaction.getTags(),
                isSplit,
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
