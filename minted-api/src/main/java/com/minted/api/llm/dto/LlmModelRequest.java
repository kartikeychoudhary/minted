package com.minted.api.llm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LlmModelRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Size(max = 50, message = "Provider must not exceed 50 characters")
        String provider,

        @NotBlank(message = "Model key is required")
        @Size(max = 200, message = "Model key must not exceed 200 characters")
        String modelKey,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        Boolean isActive,
        Boolean isDefault
) {}
