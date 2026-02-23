package com.minted.api.llm.dto;

public record LlmConfigRequest(
        String apiKey,
        Long modelId
) {}
