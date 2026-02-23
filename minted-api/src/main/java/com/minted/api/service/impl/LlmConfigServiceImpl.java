package com.minted.api.service.impl;

import com.minted.api.dto.LlmConfigRequest;
import com.minted.api.dto.LlmConfigResponse;
import com.minted.api.dto.MerchantMappingResponse;
import com.minted.api.entity.LlmConfiguration;
import com.minted.api.entity.LlmModel;
import com.minted.api.entity.User;
import com.minted.api.exception.BadRequestException;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.LlmConfigurationRepository;
import com.minted.api.repository.LlmModelRepository;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.LlmConfigService;
import com.minted.api.service.MerchantMappingService;
import com.minted.api.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmConfigServiceImpl implements LlmConfigService {

    private final LlmConfigurationRepository configRepository;
    private final LlmModelRepository modelRepository;
    private final UserRepository userRepository;
    private final MerchantMappingService merchantMappingService;
    private final SystemSettingService systemSettingService;

    @Override
    @Transactional(readOnly = true)
    public LlmConfigResponse getConfig(Long userId) {
        List<MerchantMappingResponse> mappings = merchantMappingService.getMappings(userId);

        LlmConfiguration config = configRepository.findByUserId(userId).orElse(null);
        if (config == null) {
            return new LlmConfigResponse(null, "GEMINI", false, null, mappings);
        }

        return LlmConfigResponse.from(config, mappings);
    }

    @Override
    @Transactional
    public LlmConfigResponse saveConfig(LlmConfigRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LlmConfiguration config = configRepository.findByUserId(userId)
                .orElseGet(() -> {
                    LlmConfiguration newConfig = new LlmConfiguration();
                    newConfig.setUser(user);
                    newConfig.setProvider("GEMINI");
                    return newConfig;
                });

        if (request.apiKey() != null && !request.apiKey().isBlank()) {
            config.setApiKey(request.apiKey());
        }

        if (request.modelId() != null) {
            LlmModel model = modelRepository.findById(request.modelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Model not found with id: " + request.modelId()));
            config.setModel(model);
        }

        config = configRepository.save(config);

        List<MerchantMappingResponse> mappings = merchantMappingService.getMappings(userId);
        return LlmConfigResponse.from(config, mappings);
    }

    @Override
    @Transactional(readOnly = true)
    public EffectiveLlmConfig getEffectiveConfig(Long userId) {
        // 1. Check user's own config
        LlmConfiguration userConfig = configRepository.findByUserId(userId).orElse(null);

        if (userConfig != null && userConfig.getApiKey() != null && !userConfig.getApiKey().isBlank()) {
            String modelKey = resolveModelKey(userConfig);
            return new EffectiveLlmConfig(userConfig.getApiKey(), modelKey);
        }

        // 2. Check if admin key is shared
        boolean adminKeyShared;
        try {
            adminKeyShared = "true".equalsIgnoreCase(systemSettingService.getValue("ADMIN_LLM_KEY_SHARED"));
        } catch (Exception e) {
            adminKeyShared = false;
        }

        if (adminKeyShared) {
            // Find admin user and their config
            User admin = userRepository.findAll().stream()
                    .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                    .findFirst()
                    .orElse(null);

            if (admin != null) {
                LlmConfiguration adminConfig = configRepository.findByUserId(admin.getId()).orElse(null);
                if (adminConfig != null && adminConfig.getApiKey() != null && !adminConfig.getApiKey().isBlank()) {
                    // Use admin key, but prefer user's model selection if available
                    String modelKey;
                    if (userConfig != null && userConfig.getModel() != null) {
                        modelKey = userConfig.getModel().getModelKey();
                    } else {
                        modelKey = resolveModelKey(adminConfig);
                    }
                    return new EffectiveLlmConfig(adminConfig.getApiKey(), modelKey);
                }
            }
        }

        // 3. No config available
        throw new BadRequestException("No LLM key configured. Please add your Gemini API key in Settings.");
    }

    private String resolveModelKey(LlmConfiguration config) {
        if (config.getModel() != null) {
            return config.getModel().getModelKey();
        }
        // Fall back to default model
        return modelRepository.findByIsActiveTrueOrderByIsDefaultDescNameAsc().stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsDefault()))
                .findFirst()
                .map(LlmModel::getModelKey)
                .orElse("gemini-2.0-flash");
    }
}
