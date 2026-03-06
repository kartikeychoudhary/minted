package com.minted.api.dashboardconfig.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.dashboardconfig.dto.DashboardConfigRequest;
import com.minted.api.dashboardconfig.dto.DashboardConfigResponse;
import com.minted.api.dashboardconfig.service.DashboardConfigService;
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

@WebMvcTest(DashboardConfigController.class)
@Import(TestSecurityConfig.class)
class DashboardConfigControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean DashboardConfigService dashboardConfigService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

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
    }

    // ── GET /api/v1/dashboard-config ──────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getConfig_returns200() throws Exception {
        DashboardConfigResponse config = new DashboardConfigResponse(1L, List.of(5L, 10L));
        when(dashboardConfigService.getConfig(1L)).thenReturn(config);

        mockMvc.perform(get("/api/v1/dashboard-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.excludedCategoryIds[0]").value(5));
    }

    // ── PUT /api/v1/dashboard-config ──────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void saveConfig_returns200() throws Exception {
        DashboardConfigRequest request = new DashboardConfigRequest(List.of(3L, 7L));
        DashboardConfigResponse saved = new DashboardConfigResponse(1L, List.of(3L, 7L));
        when(dashboardConfigService.saveConfig(any(), eq(1L))).thenReturn(saved);

        mockMvc.perform(put("/api/v1/dashboard-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.excludedCategoryIds[0]").value(3))
                .andExpect(jsonPath("$.message").value("Dashboard configuration saved successfully"));
    }
}
