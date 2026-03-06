package com.minted.api.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.support.TestSecurityConfig;
import com.minted.api.transaction.dto.TransactionCategoryRequest;
import com.minted.api.transaction.dto.TransactionCategoryResponse;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.transaction.service.TransactionCategoryService;
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

@WebMvcTest(TransactionCategoryController.class)
@Import(TestSecurityConfig.class)
class TransactionCategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean TransactionCategoryService categoryService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private TransactionCategoryResponse sampleResponse;

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

        sampleResponse = new TransactionCategoryResponse(
                1L, "Food", TransactionType.EXPENSE, "pi-shopping", "#ff0000",
                null, null, true, false, null, null
        );
    }

    // ── GET /api/v1/categories ────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getAllCategories_returns200() throws Exception {
        when(categoryService.getAllByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    // ── GET /api/v1/categories/active ─────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getAllActiveCategories_returns200() throws Exception {
        when(categoryService.getAllActiveByUserId(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Food"));
    }

    // ── GET /api/v1/categories/type/{type} ────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getCategoriesByType_returns200() throws Exception {
        when(categoryService.getAllByUserIdAndType(1L, TransactionType.EXPENSE)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/categories/type/EXPENSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("EXPENSE"));
    }

    // ── GET /api/v1/categories/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getCategoryById_returns200() throws Exception {
        when(categoryService.getById(1L, 1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ── POST /api/v1/categories ───────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void createCategory_returns201() throws Exception {
        when(categoryService.create(any(TransactionCategoryRequest.class), eq(1L))).thenReturn(sampleResponse);

        TransactionCategoryRequest request = new TransactionCategoryRequest(
                "Food", TransactionType.EXPENSE, "pi-shopping", "#ff0000", null
        );

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(username = "alice")
    void createCategory_missingName_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/v1/categories/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void updateCategory_returns200() throws Exception {
        when(categoryService.update(eq(1L), any(TransactionCategoryRequest.class), eq(1L))).thenReturn(sampleResponse);

        TransactionCategoryRequest request = new TransactionCategoryRequest(
                "Food Updated", TransactionType.EXPENSE, null, null, null
        );

        mockMvc.perform(put("/api/v1/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── DELETE /api/v1/categories/{id} ────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void deleteCategory_returns200() throws Exception {
        doNothing().when(categoryService).delete(1L, 1L);

        mockMvc.perform(delete("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── PATCH /api/v1/categories/{id}/toggle ─────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void toggleCategory_returns200() throws Exception {
        doNothing().when(categoryService).toggleActive(1L, 1L);

        mockMvc.perform(patch("/api/v1/categories/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
