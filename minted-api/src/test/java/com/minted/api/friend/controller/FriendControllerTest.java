package com.minted.api.friend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.friend.dto.FriendRequest;
import com.minted.api.friend.dto.FriendResponse;
import com.minted.api.friend.service.FriendService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendController.class)
@Import(TestSecurityConfig.class)
class FriendControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean FriendService friendService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private FriendResponse sampleFriend;

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

        sampleFriend = new FriendResponse(
                1L, "Bob", "bob@example.com", "555-1234", "#3b82f6", null, true, null, null
        );
    }

    // ── GET /api/v1/friends ───────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getAllFriends_returns200() throws Exception {
        when(friendService.getAllByUserId(eq(1L), anyBoolean())).thenReturn(List.of(sampleFriend));

        mockMvc.perform(get("/api/v1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Bob"));
    }

    // ── GET /api/v1/friends/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getFriendById_returns200() throws Exception {
        when(friendService.getById(1L, 1L)).thenReturn(sampleFriend);

        mockMvc.perform(get("/api/v1/friends/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Bob"));
    }

    // ── POST /api/v1/friends ──────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void createFriend_returns201() throws Exception {
        FriendRequest request = new FriendRequest("Bob", "bob@example.com", "555-1234", "#3b82f6");
        when(friendService.create(any(), eq(1L))).thenReturn(sampleFriend);

        mockMvc.perform(post("/api/v1/friends")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Bob"));
    }

    // ── PUT /api/v1/friends/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void updateFriend_returns200() throws Exception {
        FriendRequest request = new FriendRequest("Bob Updated", "bob@example.com", "555-9999", "#3b82f6");
        FriendResponse updated = new FriendResponse(1L, "Bob Updated", "bob@example.com", "555-9999", "#3b82f6", null, true, null, null);
        when(friendService.update(eq(1L), any(), eq(1L))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/friends/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Bob Updated"));
    }

    // ── DELETE /api/v1/friends/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void deleteFriend_returns200() throws Exception {
        doNothing().when(friendService).delete(1L, 1L);

        mockMvc.perform(delete("/api/v1/friends/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── POST /api/v1/friends/{id}/avatar ──────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void uploadAvatar_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});
        FriendResponse withAvatar = new FriendResponse(1L, "Bob", "bob@example.com", "555-1234", "#3b82f6", "data:image/png;base64,AQID", true, null, null);
        when(friendService.uploadAvatar(eq(1L), eq(1L), any())).thenReturn(withAvatar);

        mockMvc.perform(multipart("/api/v1/friends/1/avatar").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── DELETE /api/v1/friends/{id}/avatar ────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void deleteAvatar_returns200() throws Exception {
        when(friendService.deleteAvatar(1L, 1L)).thenReturn(sampleFriend);

        mockMvc.perform(delete("/api/v1/friends/1/avatar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
