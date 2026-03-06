package com.minted.api.llm.service;

import com.minted.api.admin.service.SystemSettingService;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.llm.dto.LlmConfigRequest;
import com.minted.api.llm.dto.LlmConfigResponse;
import com.minted.api.llm.dto.MerchantMappingResponse;
import com.minted.api.llm.entity.LlmConfiguration;
import com.minted.api.llm.entity.LlmModel;
import com.minted.api.llm.repository.LlmConfigurationRepository;
import com.minted.api.llm.repository.LlmModelRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmConfigServiceImplTest {

    @Mock LlmConfigurationRepository configRepository;
    @Mock LlmModelRepository modelRepository;
    @Mock UserRepository userRepository;
    @Mock MerchantMappingService merchantMappingService;
    @Mock SystemSettingService systemSettingService;

    @InjectMocks LlmConfigServiceImpl llmConfigService;

    private User user;
    private LlmConfiguration config;
    private LlmModel model;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setRole("USER");

        model = new LlmModel();
        model.setId(1L);
        model.setName("Gemini Flash");
        model.setProvider("GEMINI");
        model.setModelKey("gemini-2.0-flash");
        model.setIsActive(true);
        model.setIsDefault(true);

        config = new LlmConfiguration();
        config.setId(1L);
        config.setUser(user);
        config.setProvider("GEMINI");
        config.setApiKey("test-api-key");
        config.setModel(model);
    }

    // ── getConfig ─────────────────────────────────────────────────────────────

    @Test
    void getConfig_existingConfig_returnsResponse() {
        when(configRepository.findByUserId(1L)).thenReturn(Optional.of(config));
        when(merchantMappingService.getMappings(1L)).thenReturn(List.of());

        LlmConfigResponse result = llmConfigService.getConfig(1L);

        assertThat(result.provider()).isEqualTo("GEMINI");
        assertThat(result.hasApiKey()).isTrue();
    }

    @Test
    void getConfig_noConfig_returnsDefaultResponse() {
        when(configRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(merchantMappingService.getMappings(1L)).thenReturn(List.of());

        LlmConfigResponse result = llmConfigService.getConfig(1L);

        assertThat(result.id()).isNull();
        assertThat(result.provider()).isEqualTo("GEMINI");
        assertThat(result.hasApiKey()).isFalse();
    }

    // ── saveConfig ────────────────────────────────────────────────────────────

    @Test
    void saveConfig_newConfig_createsAndReturns() {
        LlmConfigRequest request = new LlmConfigRequest("new-api-key", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(configRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any())).thenAnswer(inv -> {
            LlmConfiguration c = inv.getArgument(0);
            c.setId(5L);
            return c;
        });
        when(merchantMappingService.getMappings(1L)).thenReturn(List.of());

        LlmConfigResponse result = llmConfigService.saveConfig(request, 1L);

        assertThat(result.hasApiKey()).isTrue();
        verify(configRepository).save(any(LlmConfiguration.class));
    }

    @Test
    void saveConfig_existingConfig_updatesApiKey() {
        LlmConfigRequest request = new LlmConfigRequest("updated-key", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(configRepository.findByUserId(1L)).thenReturn(Optional.of(config));
        when(configRepository.save(any())).thenReturn(config);
        when(merchantMappingService.getMappings(1L)).thenReturn(List.of());

        LlmConfigResponse result = llmConfigService.saveConfig(request, 1L);

        assertThat(result).isNotNull();
        verify(configRepository).save(config);
    }

    @Test
    void saveConfig_withModelId_setsModel() {
        LlmConfigRequest request = new LlmConfigRequest(null, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(configRepository.findByUserId(1L)).thenReturn(Optional.of(config));
        when(modelRepository.findById(1L)).thenReturn(Optional.of(model));
        when(configRepository.save(any())).thenReturn(config);
        when(merchantMappingService.getMappings(1L)).thenReturn(List.of());

        LlmConfigResponse result = llmConfigService.saveConfig(request, 1L);

        assertThat(result).isNotNull();
        verify(modelRepository).findById(1L);
    }

    @Test
    void saveConfig_invalidModelId_throwsException() {
        LlmConfigRequest request = new LlmConfigRequest(null, 999L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(configRepository.findByUserId(1L)).thenReturn(Optional.of(config));
        when(modelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> llmConfigService.saveConfig(request, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
