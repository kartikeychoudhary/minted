package com.minted.api.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.support.TestSecurityConfig;
import com.minted.api.transaction.dto.TransactionRequest;
import com.minted.api.transaction.dto.TransactionResponse;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.transaction.service.TransactionService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(TestSecurityConfig.class)
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean TransactionService transactionService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private TransactionResponse sampleResponse;

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

        sampleResponse = new TransactionResponse(
                1L, BigDecimal.valueOf(100), TransactionType.EXPENSE,
                "Coffee", null, LocalDate.now(),
                1L, "Savings", null, null,
                1L, "Food", null, null,
                false, null, false, false,
                null, null
        );
    }

    // ── GET /api/v1/transactions ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getAllTransactions_returns200() throws Exception {
        when(transactionService.getAllByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    // ── GET /api/v1/transactions/date-range ───────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getByDateRange_returns200() throws Exception {
        when(transactionService.getAllByUserIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/transactions/date-range")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    // ── GET /api/v1/transactions/filter ───────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getByFilters_returns200() throws Exception {
        when(transactionService.getAllByFilters(eq(1L), any(), any(), any(), any(), any()))
                .thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/transactions/filter")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── GET /api/v1/transactions/{id} ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getById_returns200() throws Exception {
        when(transactionService.getById(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ── POST /api/v1/transactions ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void createTransaction_returns201() throws Exception {
        when(transactionService.create(any(TransactionRequest.class), eq(1L))).thenReturn(sampleResponse);

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(100), TransactionType.EXPENSE, "Coffee", null,
                LocalDate.now(), 1L, null, 1L, false, null, false
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(username = "alice")
    void createTransaction_missingAmount_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"EXPENSE\",\"transactionDate\":\"2025-01-01\",\"accountId\":1,\"categoryId\":1}"))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/v1/transactions/{id} ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void updateTransaction_returns200() throws Exception {
        when(transactionService.update(eq(1L), any(TransactionRequest.class), eq(1L))).thenReturn(sampleResponse);

        TransactionRequest request = new TransactionRequest(
                BigDecimal.valueOf(150), TransactionType.EXPENSE, "Lunch", null,
                LocalDate.now(), 1L, null, 1L, false, null, false
        );

        mockMvc.perform(put("/api/v1/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── DELETE /api/v1/transactions/{id} ─────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void deleteTransaction_returns200() throws Exception {
        doNothing().when(transactionService).delete(1L, 1L);

        mockMvc.perform(delete("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── DELETE /api/v1/transactions/bulk ──────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void bulkDelete_returns200() throws Exception {
        doNothing().when(transactionService).bulkDelete(anyList(), eq(1L));

        mockMvc.perform(delete("/api/v1/transactions/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("ids", List.of(1L, 2L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
