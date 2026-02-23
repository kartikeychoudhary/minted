package com.minted.api.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AccountRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Account type ID is required")
        Long accountTypeId,

        BigDecimal balance,

        @Size(max = 3, message = "Currency must not exceed 3 characters")
        String currency,

        @Size(max = 7, message = "Color must not exceed 7 characters")
        String color,

        @Size(max = 50, message = "Icon must not exceed 50 characters")
        String icon
) {}
