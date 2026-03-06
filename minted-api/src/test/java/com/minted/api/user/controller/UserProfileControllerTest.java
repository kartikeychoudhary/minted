package com.minted.api.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.support.TestSecurityConfig;
import com.minted.api.user.dto.UserProfileUpdateRequest;
import com.minted.api.user.dto.UserResponse;
import com.minted.api.user.service.UserProfileService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
@Import(TestSecurityConfig.class)
class UserProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserProfileService userProfileService;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private UserResponse sampleUser;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> { ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
        doAnswer(inv -> { ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(mdcFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));

        sampleUser = new UserResponse(1L, "alice", "Alice", "alice@example.com", false, "INR", "USER", null);
    }

    @Test
    @WithMockUser(username = "alice")
    void getProfile_returnsOk() throws Exception {
        when(userProfileService.getProfile("alice")).thenReturn(sampleUser);

        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("alice"));
    }

    @Test
    @WithMockUser(username = "alice")
    void updateProfile_returnsOk() throws Exception {
        when(userProfileService.updateProfile(eq("alice"), any(UserProfileUpdateRequest.class))).thenReturn(sampleUser);

        mockMvc.perform(put("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UserProfileUpdateRequest("Alice Updated", "alice@example.com", "USD"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
    }

    @Test
    @WithMockUser(username = "alice")
    void deleteAvatar_returnsOk() throws Exception {
        when(userProfileService.deleteAvatar("alice")).thenReturn(sampleUser);

        mockMvc.perform(delete("/api/v1/profile/avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Avatar removed successfully"));
    }
}
