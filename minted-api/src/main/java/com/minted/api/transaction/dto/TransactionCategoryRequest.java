package com.minted.api.transaction.dto;

import com.minted.api.transaction.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransactionCategoryRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Type is required")
        TransactionType type,

        @Size(max = 50, message = "Icon must not exceed 50 characters")
        String icon,

        @Size(max = 7, message = "Color must not exceed 7 characters")
        String color,

        Long parentId
) {}
