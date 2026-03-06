package com.minted.api.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.account.dto.AccountRequest;
import com.minted.api.account.dto.AccountResponse;
import com.minted.api.account.service.AccountService;
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

@WebMvcTest(AccountController.class)
@Import(TestSecurityConfig.class)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AccountService accountService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private AccountResponse sampleResponse;

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

        sampleResponse = new AccountResponse(1L, "Savings", 1L, "Bank", BigDecimal.ZERO, "INR", null, null, true, null, null);
    }

    @Test
    @WithMockUser(username = "alice")
    void getAllAccounts_returns200() throws Exception {
        when(accountService.getAllByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "alice")
    void getAllActiveAccounts_returns200() throws Exception {
        when(accountService.getAllActiveByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/accounts/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Savings"));
    }

    @Test
    @WithMockUser(username = "alice")
    void getAccountById_returns200() throws Exception {
        when(accountService.getById(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(username = "alice")
    void createAccount_returns201() throws Exception {
        when(accountService.create(any(AccountRequest.class), eq(1L))).thenReturn(sampleResponse);

        AccountRequest request = new AccountRequest("Savings", 1L, BigDecimal.ZERO, "INR", null, null);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "alice")
    void createAccount_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"accountTypeId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "alice")
    void updateAccount_returns200() throws Exception {
        when(accountService.update(eq(1L), any(AccountRequest.class), eq(1L))).thenReturn(sampleResponse);

        AccountRequest request = new AccountRequest("Updated", 1L, null, "INR", null, null);

        mockMvc.perform(put("/api/v1/accounts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "alice")
    void deleteAccount_returns200() throws Exception {
        doNothing().when(accountService).delete(1L, 1L);

        mockMvc.perform(delete("/api/v1/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "alice")
    void toggleAccount_returns200() throws Exception {
        doNothing().when(accountService).toggleActive(1L, 1L);

        mockMvc.perform(patch("/api/v1/accounts/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
