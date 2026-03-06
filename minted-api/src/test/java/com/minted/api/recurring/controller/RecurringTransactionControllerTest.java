package com.minted.api.recurring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.recurring.dto.RecurringSummaryResponse;
import com.minted.api.recurring.dto.RecurringTransactionRequest;
import com.minted.api.recurring.dto.RecurringTransactionResponse;
import com.minted.api.recurring.service.RecurringTransactionService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecurringTransactionController.class)
@Import(TestSecurityConfig.class)
class RecurringTransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean RecurringTransactionService recurringService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private RecurringTransactionResponse sampleResponse;

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

        sampleResponse = new RecurringTransactionResponse(
                1L, "Rent", BigDecimal.valueOf(1000), "EXPENSE",
                1L, "Rent", null, null,
                1L, "Savings", "MONTHLY", 1,
                LocalDate.now(), null, "ACTIVE", null, null, null
        );
    }

    // ── GET /api/v1/recurring-transactions ───────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getAll_returns200() throws Exception {
        when(recurringService.getAllByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/recurring-transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    // ── GET /api/v1/recurring-transactions/{id} ───────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getById_returns200() throws Exception {
        when(recurringService.getById(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/recurring-transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Rent"));
    }

    // ── POST /api/v1/recurring-transactions ───────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void create_returns201() throws Exception {
        when(recurringService.create(any(RecurringTransactionRequest.class), eq(1L))).thenReturn(sampleResponse);

        RecurringTransactionRequest request = new RecurringTransactionRequest(
                "Rent", BigDecimal.valueOf(1000), "EXPENSE", 1L, 1L,
                "MONTHLY", 1, LocalDate.now(), null
        );

        mockMvc.perform(post("/api/v1/recurring-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── PUT /api/v1/recurring-transactions/{id} ───────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void update_returns200() throws Exception {
        when(recurringService.update(eq(1L), any(RecurringTransactionRequest.class), eq(1L))).thenReturn(sampleResponse);

        RecurringTransactionRequest request = new RecurringTransactionRequest(
                "Rent Updated", BigDecimal.valueOf(1200), "EXPENSE", 1L, 1L,
                "MONTHLY", 1, LocalDate.now(), null
        );

        mockMvc.perform(put("/api/v1/recurring-transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── DELETE /api/v1/recurring-transactions/{id} ────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void delete_returns200() throws Exception {
        doNothing().when(recurringService).delete(1L, 1L);

        mockMvc.perform(delete("/api/v1/recurring-transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── PATCH /api/v1/recurring-transactions/{id}/toggle ─────────────────────

    @Test
    @WithMockUser(username = "alice")
    void toggleStatus_returns200() throws Exception {
        doNothing().when(recurringService).toggleStatus(1L, 1L);

        mockMvc.perform(patch("/api/v1/recurring-transactions/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── GET /api/v1/recurring-transactions/summary ────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getSummary_returns200() throws Exception {
        RecurringSummaryResponse summary = new RecurringSummaryResponse(
                BigDecimal.valueOf(2000), BigDecimal.valueOf(5000), BigDecimal.valueOf(3000), 3L, 1L
        );
        when(recurringService.getSummary(1L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/recurring-transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.activeCount").value(3));
    }
}
