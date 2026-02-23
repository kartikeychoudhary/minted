package com.minted.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MerchantMappingRequest(
        @NotBlank(message = "Snippets are required")
        @Size(max = 500, message = "Snippets must not exceed 500 characters")
        String snippets,

        @NotNull(message = "Category ID is required")
        Long categoryId
) {}
