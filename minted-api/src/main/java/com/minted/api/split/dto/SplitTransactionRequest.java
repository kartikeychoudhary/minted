package com.minted.api.split.dto;

import com.minted.api.split.enums.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SplitTransactionRequest(
        Long sourceTransactionId,

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String categoryName,

        @NotNull(message = "Total amount is required")
        BigDecimal totalAmount,

        @NotNull(message = "Split type is required")
        SplitType splitType,

        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,

        @NotEmpty(message = "At least one share is required")
        @Valid
        List<SplitShareRequest> shares
) {}
