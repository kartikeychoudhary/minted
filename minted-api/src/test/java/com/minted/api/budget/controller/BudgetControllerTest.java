package com.minted.api.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.budget.dto.BudgetRequest;
import com.minted.api.budget.dto.BudgetResponse;
import com.minted.api.budget.service.BudgetService;
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
import org.springframework.http.MediaType;
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

@WebMvcTest(BudgetController.class)
@Import(TestSecurityConfig.class)
class BudgetControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean BudgetService budgetService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private BudgetResponse sampleResponse;

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

        sampleResponse = new BudgetResponse(1L, "Monthly Food", BigDecimal.valueOf(5000), 3, 2025, 1L, "Food", null, null);
    }

    @Test
    @WithMockUser(username = "alice")
    void getAllBudgets_returns200() throws Exception {
        when(budgetService.getAllByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "alice")
    void getBudgetsByMonthYear_returns200() throws Exception {
        when(budgetService.getAllByUserIdAndMonthYear(1L, 3, 2025)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/budgets/month/3/year/2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].month").value(3));
    }

    @Test
    @WithMockUser(username = "alice")
    void getBudgetsByYear_returns200() throws Exception {
        when(budgetService.getAllByUserIdAndYear(1L, 2025)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/budgets/year/2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].year").value(2025));
    }

    @Test
    @WithMockUser(username = "alice")
    void getBudgetById_returns200() throws Exception {
        when(budgetService.getById(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/budgets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(username = "alice")
    void createBudget_returns201() throws Exception {
        when(budgetService.create(any(BudgetRequest.class), eq(1L))).thenReturn(sampleResponse);

        BudgetRequest request = new BudgetRequest("Monthly Food", BigDecimal.valueOf(5000), 3, 2025, 1L);

        mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "alice")
    void createBudget_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"amount\":5000,\"month\":3,\"year\":2025}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void updateBudget_returns200() throws Exception {
        when(budgetService.update(eq(1L), any(BudgetRequest.class), eq(1L))).thenReturn(sampleResponse);

        BudgetRequest request = new BudgetRequest("Updated Food", BigDecimal.valueOf(6000), 3, 2025, null);

        mockMvc.perform(put("/api/v1/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "alice")
    void deleteBudget_returns200() throws Exception {
        doNothing().when(budgetService).delete(1L, 1L);

        mockMvc.perform(delete("/api/v1/budgets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
