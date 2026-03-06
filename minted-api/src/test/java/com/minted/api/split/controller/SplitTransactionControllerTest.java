package com.minted.api.split.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.split.dto.*;
import com.minted.api.split.enums.SplitType;
import com.minted.api.split.service.SplitService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SplitTransactionController.class)
@Import(TestSecurityConfig.class)
class SplitTransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean SplitService splitService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private SplitTransactionResponse sampleSplit;

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

        sampleSplit = new SplitTransactionResponse(
                1L, null, "Dinner", "Food",
                BigDecimal.valueOf(200), "EQUAL", LocalDate.of(2025, 1, 15),
                false, BigDecimal.valueOf(100), List.of(), null, null
        );
    }

    // ── GET /api/v1/splits ────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getAllSplits_returns200() throws Exception {
        when(splitService.getAllByUserId(1L)).thenReturn(List.of(sampleSplit));

        mockMvc.perform(get("/api/v1/splits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].description").value("Dinner"));
    }

    // ── GET /api/v1/splits/{id} ───────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getSplitById_returns200() throws Exception {
        when(splitService.getById(1L, 1L)).thenReturn(sampleSplit);

        mockMvc.perform(get("/api/v1/splits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ── POST /api/v1/splits ───────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void createSplit_returns201() throws Exception {
        SplitShareRequest shareReq = new SplitShareRequest(2L, BigDecimal.valueOf(100), null, false);
        SplitTransactionRequest request = new SplitTransactionRequest(
                null, "Dinner", "Food", BigDecimal.valueOf(200),
                SplitType.EQUAL, LocalDate.of(2025, 1, 15), List.of(shareReq)
        );
        when(splitService.create(any(), eq(1L))).thenReturn(sampleSplit);

        mockMvc.perform(post("/api/v1/splits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.description").value("Dinner"));
    }

    // ── PUT /api/v1/splits/{id} ───────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void updateSplit_returns200() throws Exception {
        SplitShareRequest shareReq = new SplitShareRequest(2L, BigDecimal.valueOf(100), null, false);
        SplitTransactionRequest request = new SplitTransactionRequest(
                null, "Dinner Updated", "Food", BigDecimal.valueOf(200),
                SplitType.EQUAL, LocalDate.of(2025, 1, 15), List.of(shareReq)
        );
        SplitTransactionResponse updated = new SplitTransactionResponse(
                1L, null, "Dinner Updated", "Food",
                BigDecimal.valueOf(200), "EQUAL", LocalDate.of(2025, 1, 15),
                false, BigDecimal.valueOf(100), List.of(), null, null
        );
        when(splitService.update(eq(1L), any(), eq(1L))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/splits/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Dinner Updated"));
    }

    // ── DELETE /api/v1/splits/{id} ────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void deleteSplit_returns200() throws Exception {
        doNothing().when(splitService).delete(1L, 1L);

        mockMvc.perform(delete("/api/v1/splits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── GET /api/v1/splits/summary ────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getBalanceSummary_returns200() throws Exception {
        SplitBalanceSummaryResponse summary = new SplitBalanceSummaryResponse(
                BigDecimal.valueOf(500), BigDecimal.valueOf(200)
        );
        when(splitService.getBalanceSummary(1L)).thenReturn(summary);

        mockMvc.perform(get("/api/v1/splits/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.youAreOwed").value(500));
    }

    // ── GET /api/v1/splits/balances ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getFriendBalances_returns200() throws Exception {
        FriendBalanceResponse balance = new FriendBalanceResponse(2L, "Bob", "#3b82f6", BigDecimal.valueOf(100));
        when(splitService.getFriendBalances(1L)).thenReturn(List.of(balance));

        mockMvc.perform(get("/api/v1/splits/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].friendName").value("Bob"));
    }

    // ── POST /api/v1/splits/settle ────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void settleFriend_returns200() throws Exception {
        SettleRequest request = new SettleRequest(2L);
        doNothing().when(splitService).settleFriend(any(), eq(1L));

        mockMvc.perform(post("/api/v1/splits/settle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── GET /api/v1/splits/friend/{friendId}/shares ───────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getSharesByFriend_returns200() throws Exception {
        SplitShareResponse share = new SplitShareResponse(
                1L, 2L, "Bob", "#3b82f6",
                BigDecimal.valueOf(100), BigDecimal.valueOf(50),
                false, false, null,
                "Dinner", "Food", "2025-01-15"
        );
        when(splitService.getSharesByFriend(2L, 1L)).thenReturn(List.of(share));

        mockMvc.perform(get("/api/v1/splits/friend/2/shares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].friendName").value("Bob"));
    }
}
