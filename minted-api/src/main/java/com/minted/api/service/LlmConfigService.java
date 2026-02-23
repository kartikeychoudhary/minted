package com.minted.api.service;

import com.minted.api.dto.LlmConfigRequest;
import com.minted.api.dto.LlmConfigResponse;

public interface LlmConfigService {

    LlmConfigResponse getConfig(Long userId);

    LlmConfigResponse saveConfig(LlmConfigRequest request, Long userId);

    EffectiveLlmConfig getEffectiveConfig(Long userId);

    record EffectiveLlmConfig(String apiKey, String modelKey) {}
}
