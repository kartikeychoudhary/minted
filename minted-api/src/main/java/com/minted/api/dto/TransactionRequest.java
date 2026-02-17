package com.minted.api.dto;

import com.minted.api.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        BigDecimal amount,

        @NotNull(message = "Type is required")
        TransactionType type,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        String notes,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,

        @NotNull(message = "Account ID is required")
        Long accountId,

        Long toAccountId,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        Boolean isRecurring,

        @Size(max = 500, message = "Tags must not exceed 500 characters")
        String tags
) {}
