package com.minted.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DefaultCategoryRequest(
        @NotBlank(message = "Category name is required")
        String name,
        String icon,
        @NotBlank(message = "Type is required (INCOME or EXPENSE)")
        String type
) {
}
