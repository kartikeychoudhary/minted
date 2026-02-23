package com.minted.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record DefaultAccountTypeRequest(
        @NotBlank(message = "Account type name is required")
        String name
) {
}
