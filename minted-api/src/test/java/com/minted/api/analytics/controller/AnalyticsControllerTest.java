package com.minted.api.analytics.controller;

import com.minted.api.analytics.dto.AnalyticsSummaryResponse;
import com.minted.api.analytics.dto.BudgetSummaryResponse;
import com.minted.api.analytics.dto.CategoryWiseResponse;
import com.minted.api.analytics.dto.SpendingActivityResponse;
import com.minted.api.analytics.dto.TotalBalanceResponse;
import com.minted.api.analytics.dto.TrendResponse;
import com.minted.api.analytics.service.AnalyticsService;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.support.TestSecurityConfig;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@Import(TestSecurityConfig.class)
class AnalyticsControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean AnalyticsService analyticsService;
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

    // ── GET /api/v1/analytics/summary ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getSummary_returns200() throws Exception {
        AnalyticsSummaryResponse summary = new AnalyticsSummaryResponse(
                BigDecimal.valueOf(5000), BigDecimal.valueOf(2000), BigDecimal.valueOf(3000), 10L
        );
        when(analyticsService.getSummary(eq(1L), any(), any(), isNull())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/analytics/summary")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionCount").value(10));
    }

    // ── GET /api/v1/analytics/category-wise ───────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getCategoryWise_returns200() throws Exception {
        CategoryWiseResponse cat = new CategoryWiseResponse(1L, "Food", null, null, BigDecimal.valueOf(1000), 5L, 50.0);
        when(analyticsService.getCategoryWise(eq(1L), any(), any(), any(), any())).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/v1/analytics/category-wise")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryName").value("Food"));
    }

    // ── GET /api/v1/analytics/trend ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getTrend_returns200() throws Exception {
        TrendResponse trend = new TrendResponse("2025-01", BigDecimal.valueOf(5000), BigDecimal.valueOf(2000), BigDecimal.valueOf(3000));
        when(analyticsService.getTrend(eq(1L), eq(6), isNull())).thenReturn(List.of(trend));

        mockMvc.perform(get("/api/v1/analytics/trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].month").value("2025-01"));
    }

    // ── GET /api/v1/analytics/spending-activity ───────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getSpendingActivity_returns200() throws Exception {
        SpendingActivityResponse activity = new SpendingActivityResponse("2025-01-01", "Wed", BigDecimal.valueOf(500));
        when(analyticsService.getSpendingActivity(eq(1L), any(), any())).thenReturn(List.of(activity));

        mockMvc.perform(get("/api/v1/analytics/spending-activity")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].date").value("2025-01-01"));
    }

    // ── GET /api/v1/analytics/total-balance ───────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getTotalBalance_returns200() throws Exception {
        TotalBalanceResponse balance = new TotalBalanceResponse(
                BigDecimal.valueOf(15000), BigDecimal.valueOf(15000),
                BigDecimal.valueOf(10), BigDecimal.valueOf(-5)
        );
        when(analyticsService.getTotalBalance(1L)).thenReturn(balance);

        mockMvc.perform(get("/api/v1/analytics/total-balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalBalance").value(15000));
    }

    // ── GET /api/v1/analytics/budget-summary ──────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getBudgetSummary_returns200() throws Exception {
        BudgetSummaryResponse budget = new BudgetSummaryResponse(
                1L, "Food Budget", "Food",
                BigDecimal.valueOf(5000), BigDecimal.valueOf(2500), BigDecimal.valueOf(2500), 50.0
        );
        when(analyticsService.getBudgetSummary(1L)).thenReturn(List.of(budget));

        mockMvc.perform(get("/api/v1/analytics/budget-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].budgetName").value("Food Budget"));
    }
}
