package com.minted.api.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.admin.dto.*;
import com.minted.api.admin.service.DefaultListsService;
import com.minted.api.admin.service.SystemSettingService;
import com.minted.api.admin.service.UserManagementService;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.job.service.JobExecutionService;
import com.minted.api.support.TestSecurityConfig;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(TestSecurityConfig.class)
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean JobExecutionService jobExecutionService;
    @MockBean DefaultListsService defaultListsService;
    @MockBean UserManagementService userManagementService;
    @MockBean SystemSettingService systemSettingService;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(inv -> { ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(jwtAuthFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
        doAnswer(inv -> { ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                .when(mdcFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class), any(FilterChain.class));
    }

    // ── default categories ────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDefaultCategories_returnsOk() throws Exception {
        when(defaultListsService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/defaults/categories"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDefaultCategory_returnsCreated() throws Exception {
        DefaultCategoryResponse response = new DefaultCategoryResponse(1L, "Food", null, "EXPENSE");
        when(defaultListsService.createCategory(any(DefaultCategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/defaults/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DefaultCategoryRequest("Food", null, "EXPENSE"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Food"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDefaultCategory_returnsNoContent() throws Exception {
        doNothing().when(defaultListsService).deleteCategory(1L);

        mockMvc.perform(delete("/api/v1/admin/defaults/categories/1"))
                .andExpect(status().isNoContent());
    }

    // ── default account types ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDefaultAccountTypes_returnsOk() throws Exception {
        when(defaultListsService.getAllAccountTypes()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/defaults/account-types"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDefaultAccountType_returnsCreated() throws Exception {
        DefaultAccountTypeResponse response = new DefaultAccountTypeResponse(1L, "Bank");
        when(defaultListsService.createAccountType(any(DefaultAccountTypeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/defaults/account-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DefaultAccountTypeRequest("Bank"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bank"));
    }

    // ── users ─────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_returnsOk() throws Exception {
        when(userManagementService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_returnsCreated() throws Exception {
        AdminUserResponse response = new AdminUserResponse(1L, "bob", "Bob", "bob@example.com", true, false, "INR", "USER", null, null);
        when(userManagementService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateUserRequest("bob", "Password1", "Bob", "bob@example.com", "USER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("bob"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_returnsNoContent() throws Exception {
        doNothing().when(userManagementService).deleteUser(anyLong(), anyString());

        mockMvc.perform(delete("/api/v1/admin/users/2"))
                .andExpect(status().isNoContent());
    }

    // ── settings ──────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSetting_returnsOk() throws Exception {
        SystemSettingResponse setting = new SystemSettingResponse(1L, "SIGNUP_ENABLED", "true", "Signup toggle");
        when(systemSettingService.getSetting("SIGNUP_ENABLED")).thenReturn(setting);

        mockMvc.perform(get("/api/v1/admin/settings/SIGNUP_ENABLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.settingValue").value("true"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSetting_returnsOk() throws Exception {
        SystemSettingResponse updated = new SystemSettingResponse(1L, "SIGNUP_ENABLED", "false", "Signup toggle");
        when(systemSettingService.updateSetting(eq("SIGNUP_ENABLED"), eq("false"))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/admin/settings/SIGNUP_ENABLED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"false\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
