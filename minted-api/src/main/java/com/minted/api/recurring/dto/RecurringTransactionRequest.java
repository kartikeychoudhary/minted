package com.minted.api.recurring.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionRequest(
    @NotBlank(message = "Transaction name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    String name,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,

    @NotNull(message = "Type is required")
    String type,

    @NotNull(message = "Category is required")
    Long categoryId,

    @NotNull(message = "Account is required")
    Long accountId,

    String frequency,

    @Min(value = 1, message = "Day of month must be between 1 and 31")
    @Max(value = 31, message = "Day of month must be between 1 and 31")
    Integer dayOfMonth,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    LocalDate endDate
) {}
