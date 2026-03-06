package com.minted.api.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.analytics.service.AnalyticsService;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.dashboard.dto.ChartDataResponse;
import com.minted.api.dashboard.dto.DashboardCardRequest;
import com.minted.api.dashboard.dto.DashboardCardResponse;
import com.minted.api.dashboard.enums.CardWidth;
import com.minted.api.dashboard.enums.ChartType;
import com.minted.api.dashboard.service.DashboardCardService;
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

@WebMvcTest(DashboardCardController.class)
@Import(TestSecurityConfig.class)
class DashboardCardControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean DashboardCardService cardService;
    @MockBean AnalyticsService analyticsService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private DashboardCardResponse sampleResponse;

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

        sampleResponse = new DashboardCardResponse(
                1L, "Spending Trend", ChartType.LINE, "month", "amount",
                null, 1, CardWidth.FULL, true, null, null
        );
    }

    // ── GET /api/v1/dashboard-cards ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getAllCards_returns200() throws Exception {
        when(cardService.getAllByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/dashboard-cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Spending Trend"));
    }

    // ── GET /api/v1/dashboard-cards/active ────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getAllActiveCards_returns200() throws Exception {
        when(cardService.getAllActiveByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/dashboard-cards/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].isActive").value(true));
    }

    // ── GET /api/v1/dashboard-cards/{id} ──────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getCardById_returns200() throws Exception {
        when(cardService.getById(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/dashboard-cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ── GET /api/v1/dashboard-cards/{id}/data ─────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getCardData_returns200() throws Exception {
        ChartDataResponse chartData = new ChartDataResponse(null, null);
        when(analyticsService.getCardData(eq(1L), eq(1L), any(), any())).thenReturn(chartData);

        mockMvc.perform(get("/api/v1/dashboard-cards/1/data")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── POST /api/v1/dashboard-cards ──────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void createCard_returns201() throws Exception {
        DashboardCardRequest request = new DashboardCardRequest(
                "Spending Trend", ChartType.LINE, "month", "amount", null, 1, CardWidth.FULL
        );
        when(cardService.create(any(), eq(1L))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/dashboard-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Spending Trend"));
    }

    // ── PUT /api/v1/dashboard-cards/{id} ──────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void updateCard_returns200() throws Exception {
        DashboardCardRequest request = new DashboardCardRequest(
                "Updated Trend", ChartType.BAR, "month", "amount", null, 1, CardWidth.HALF
        );
        DashboardCardResponse updated = new DashboardCardResponse(
                1L, "Updated Trend", ChartType.BAR, "month", "amount", null, 1, CardWidth.HALF, true, null, null
        );
        when(cardService.update(eq(1L), any(), eq(1L))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/dashboard-cards/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Trend"));
    }

    // ── DELETE /api/v1/dashboard-cards/{id} ───────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void deleteCard_returns200() throws Exception {
        doNothing().when(cardService).delete(1L, 1L);

        mockMvc.perform(delete("/api/v1/dashboard-cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── PATCH /api/v1/dashboard-cards/{id}/toggle ─────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void toggleCard_returns200() throws Exception {
        doNothing().when(cardService).toggleActive(1L, 1L);

        mockMvc.perform(patch("/api/v1/dashboard-cards/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── PUT /api/v1/dashboard-cards/reorder ───────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void reorderCards_returns200() throws Exception {
        doNothing().when(cardService).reorderCards(eq(1L), any());

        mockMvc.perform(put("/api/v1/dashboard-cards/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2, 3]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
