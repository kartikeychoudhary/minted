package com.minted.api.llm.service;

import com.minted.api.llm.dto.LlmConfigRequest;
import com.minted.api.llm.dto.LlmConfigResponse;

public interface LlmConfigService {

    LlmConfigResponse getConfig(Long userId);

    LlmConfigResponse saveConfig(LlmConfigRequest request, Long userId);

    EffectiveLlmConfig getEffectiveConfig(Long userId);

    record EffectiveLlmConfig(String apiKey, String modelKey) {}
}
