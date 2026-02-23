package com.minted.api.dto;

public record LlmConfigRequest(
        String apiKey,
        Long modelId
) {}
