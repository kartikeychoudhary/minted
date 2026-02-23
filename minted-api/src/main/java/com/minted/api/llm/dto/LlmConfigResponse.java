package com.minted.api.llm.dto;

import com.minted.api.llm.entity.LlmConfiguration;

import java.util.List;

public record LlmConfigResponse(
        Long id,
        String provider,
        boolean hasApiKey,
        LlmModelResponse selectedModel,
        List<MerchantMappingResponse> merchantMappings
) {
    public static LlmConfigResponse from(LlmConfiguration config, List<MerchantMappingResponse> mappings) {
        return new LlmConfigResponse(
                config.getId(),
                config.getProvider(),
                config.getApiKey() != null && !config.getApiKey().isBlank(),
                config.getModel() != null ? LlmModelResponse.from(config.getModel()) : null,
                mappings
        );
    }
}
