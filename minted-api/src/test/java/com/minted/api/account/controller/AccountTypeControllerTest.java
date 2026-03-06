package com.minted.api.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.account.dto.AccountTypeRequest;
import com.minted.api.account.dto.AccountTypeResponse;
import com.minted.api.account.service.AccountTypeService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountTypeController.class)
@Import(TestSecurityConfig.class)
class AccountTypeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AccountTypeService accountTypeService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private AccountTypeResponse sampleResponse;

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

        sampleResponse = new AccountTypeResponse(1L, "Bank", null, null, true, false, null, null);
    }

    @Test
    @WithMockUser(username = "alice")
    void getAllAccountTypes_returns200() throws Exception {
        when(accountTypeService.getAllByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/account-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "alice")
    void getAllActiveAccountTypes_returns200() throws Exception {
        when(accountTypeService.getAllActiveByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/account-types/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Bank"));
    }

    @Test
    @WithMockUser(username = "alice")
    void getAccountTypeById_returns200() throws Exception {
        when(accountTypeService.getById(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/account-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(username = "alice")
    void createAccountType_returns201() throws Exception {
        when(accountTypeService.create(any(AccountTypeRequest.class), eq(1L))).thenReturn(sampleResponse);

        AccountTypeRequest request = new AccountTypeRequest("Bank", null, null);

        mockMvc.perform(post("/api/v1/account-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "alice")
    void updateAccountType_returns200() throws Exception {
        when(accountTypeService.update(eq(1L), any(AccountTypeRequest.class), eq(1L))).thenReturn(sampleResponse);

        AccountTypeRequest request = new AccountTypeRequest("Bank Updated", null, null);

        mockMvc.perform(put("/api/v1/account-types/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "alice")
    void deleteAccountType_returns200() throws Exception {
        doNothing().when(accountTypeService).delete(1L, 1L);

        mockMvc.perform(delete("/api/v1/account-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "alice")
    void toggleAccountType_returns200() throws Exception {
        doNothing().when(accountTypeService).toggleActive(1L, 1L);

        mockMvc.perform(patch("/api/v1/account-types/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
