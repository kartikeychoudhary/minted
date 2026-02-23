package com.minted.api.llm.dto;

import com.minted.api.llm.entity.LlmModel;

public record LlmModelResponse(
        Long id,
        String name,
        String provider,
        String modelKey,
        String description,
        Boolean isActive,
        Boolean isDefault
) {
    public static LlmModelResponse from(LlmModel model) {
        return new LlmModelResponse(
                model.getId(),
                model.getName(),
                model.getProvider(),
                model.getModelKey(),
                model.getDescription(),
                model.getIsActive(),
                model.getIsDefault()
        );
    }
}
