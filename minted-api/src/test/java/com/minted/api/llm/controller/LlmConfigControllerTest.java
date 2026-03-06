package com.minted.api.llm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.llm.dto.*;
import com.minted.api.llm.entity.LlmModel;
import com.minted.api.llm.repository.LlmModelRepository;
import com.minted.api.llm.service.LlmConfigService;
import com.minted.api.llm.service.MerchantMappingService;
import com.minted.api.support.TestSecurityConfig;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LlmConfigController.class)
@Import(TestSecurityConfig.class)
class LlmConfigControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean LlmConfigService llmConfigService;
    @MockBean MerchantMappingService merchantMappingService;
    @MockBean LlmModelRepository modelRepository;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private LlmConfigResponse sampleConfig;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> { ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
        doAnswer(inv -> { ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(mdcFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        sampleConfig = new LlmConfigResponse(1L, "GEMINI", true, null, List.of());
    }

    // ── GET /api/v1/llm-config ────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getConfig_returns200() throws Exception {
        when(llmConfigService.getConfig(1L)).thenReturn(sampleConfig);

        mockMvc.perform(get("/api/v1/llm-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.provider").value("GEMINI"))
                .andExpect(jsonPath("$.data.hasApiKey").value(true));
    }

    // ── PUT /api/v1/llm-config ────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void saveConfig_returns200() throws Exception {
        LlmConfigRequest request = new LlmConfigRequest("new-api-key", null);
        when(llmConfigService.saveConfig(any(), eq(1L))).thenReturn(sampleConfig);

        mockMvc.perform(put("/api/v1/llm-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── GET /api/v1/llm-config/models ─────────────────────────────────────────

    @Test
    void getActiveModels_returns200WithoutAuth() throws Exception {
        LlmModel model = new LlmModel();
        model.setId(1L);
        model.setName("Gemini Flash");
        model.setProvider("GEMINI");
        model.setModelKey("gemini-2.0-flash");
        model.setIsActive(true);
        model.setIsDefault(true);
        when(modelRepository.findByIsActiveTrueOrderByIsDefaultDescNameAsc()).thenReturn(List.of(model));

        mockMvc.perform(get("/api/v1/llm-config/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].modelKey").value("gemini-2.0-flash"));
    }

    // ── GET /api/v1/llm-config/mappings ───────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getMappings_returns200() throws Exception {
        when(merchantMappingService.getMappings(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/llm-config/mappings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // ── DELETE /api/v1/llm-config/mappings/{id} ───────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void deleteMapping_returns204() throws Exception {
        doNothing().when(merchantMappingService).deleteMapping(1L, 1L);

        mockMvc.perform(delete("/api/v1/llm-config/mappings/1"))
                .andExpect(status().isNoContent());
    }
}
