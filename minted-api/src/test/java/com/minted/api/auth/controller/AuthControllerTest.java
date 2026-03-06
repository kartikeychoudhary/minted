package com.minted.api.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.dto.*;
import com.minted.api.auth.service.AuthService;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.support.TestSecurityConfig;
import com.minted.api.user.dto.UserResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private LoginResponse sampleLoginResponse;

    @BeforeEach
    void setUp() throws Exception {
        // Make filters pass through
        doAnswer(inv -> { ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
        doAnswer(inv -> { ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(mdcFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));

        UserResponse user = new UserResponse(1L, "alice", "Alice", "alice@example.com", false, "INR", "USER", null);
        sampleLoginResponse = new LoginResponse("token", "refresh", "Bearer", 86400000L, user);
    }

    // ── POST /auth/login ─────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(sampleLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("alice", "Password1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token"));
    }

    @Test
    void login_missingUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"Password1\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── POST /auth/refresh ───────────────────────────────────────────────────

    @Test
    void refreshToken_validToken_returns200() throws Exception {
        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(sampleLoginResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("old-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token"));
    }

    // ── PUT /auth/change-password ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void changePassword_success_returns200() throws Exception {
        doNothing().when(authService).changePassword(anyString(), any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ChangePasswordRequest("OldPass1", "NewPass2", "NewPass2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── POST /auth/signup ─────────────────────────────────────────────────────

    @Test
    void signup_validRequest_returns201() throws Exception {
        when(authService.signup(any(SignupRequest.class))).thenReturn(sampleLoginResponse);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SignupRequest("alice", "Password1", "Password1", "Alice", "alice@example.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── GET /auth/signup-enabled ───────────────────────────────────────────────

    @Test
    void isSignupEnabled_returnsBoolean() throws Exception {
        when(authService.isSignupEnabled()).thenReturn(true);

        mockMvc.perform(get("/api/v1/auth/signup-enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    // ── GET /auth/health ──────────────────────────────────────────────────────

    @Test
    void health_returnsUp() throws Exception {
        mockMvc.perform(get("/api/v1/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
