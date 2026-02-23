package com.minted.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSettingRequest(
        @NotBlank(message = "Value is required")
        String value
) {
}
