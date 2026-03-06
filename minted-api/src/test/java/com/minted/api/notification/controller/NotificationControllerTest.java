package com.minted.api.notification.controller;

import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.notification.dto.NotificationResponse;
import com.minted.api.notification.service.NotificationService;
import com.minted.api.support.TestSecurityConfig;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

@WebMvcTest(NotificationController.class)
@Import(TestSecurityConfig.class)
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean NotificationService notificationService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private NotificationResponse sampleResponse;

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

        sampleResponse = new NotificationResponse(1L, "INFO", "Test", "Test message", false, null);
    }

    // ── GET /api/v1/notifications ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getNotifications_returns200() throws Exception {
        when(notificationService.getNotifications(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── GET /api/v1/notifications/unread-count ────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getUnreadCount_returns200() throws Exception {
        when(notificationService.getUnreadCount(1L)).thenReturn(3L);

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3));
    }

    // ── PUT /api/v1/notifications/{id}/read ───────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void markAsRead_returns200() throws Exception {
        when(notificationService.markAsRead(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── PUT /api/v1/notifications/read-all ────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void markAllAsRead_returns200() throws Exception {
        when(notificationService.markAllAsRead(1L)).thenReturn(5);

        mockMvc.perform(put("/api/v1/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── DELETE /api/v1/notifications/{id} ────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void dismiss_returns204() throws Exception {
        doNothing().when(notificationService).dismiss(1L, 1L);

        mockMvc.perform(delete("/api/v1/notifications/1"))
                .andExpect(status().isNoContent());
    }

    // ── DELETE /api/v1/notifications/read ────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void dismissAllRead_returns200() throws Exception {
        when(notificationService.dismissAllRead(1L)).thenReturn(4);

        mockMvc.perform(delete("/api/v1/notifications/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
