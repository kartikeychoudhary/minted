package com.minted.api.statement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.statement.dto.StatementResponse;
import com.minted.api.statement.enums.StatementStatus;
import com.minted.api.statement.service.CreditCardStatementService;
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

@WebMvcTest(CreditCardStatementController.class)
@Import(TestSecurityConfig.class)
class CreditCardStatementControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CreditCardStatementService statementService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private StatementResponse sampleStatement;

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

        sampleStatement = new StatementResponse(
                1L, 2L, "Credit Card", "statement.pdf",
                10240L, StatementStatus.UPLOADED, 1, null,
                null, null, null, null, null, "pdf", null, null
        );
    }

    // ── POST /api/v1/statements/upload ────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void upload_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "statement.pdf", "application/pdf", new byte[]{1, 2, 3});
        when(statementService.uploadAndExtract(any(), eq(2L), isNull(), eq(1L))).thenReturn(sampleStatement);

        mockMvc.perform(multipart("/api/v1/statements/upload")
                        .file(file)
                        .param("accountId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("statement.pdf"));
    }

    // ── POST /api/v1/statements/{id}/parse ────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void triggerParse_returns202() throws Exception {
        StatementResponse parsing = new StatementResponse(
                1L, 2L, "Credit Card", "statement.pdf",
                10240L, StatementStatus.SENT_FOR_AI_PARSING, 2, null,
                null, null, null, null, null, "pdf", null, null
        );
        when(statementService.triggerLlmParse(eq(1L), eq(1L), any())).thenReturn(parsing);

        mockMvc.perform(post("/api/v1/statements/1/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SENT_FOR_AI_PARSING"));
    }

    // ── GET /api/v1/statements/{id}/parsed-rows ───────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getParsedRows_returns200() throws Exception {
        when(statementService.getParsedRows(1L, 1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/statements/1/parsed-rows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ── POST /api/v1/statements/confirm ───────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void confirmImport_returns200() throws Exception {
        doNothing().when(statementService).confirmImport(any(), eq(1L));

        mockMvc.perform(post("/api/v1/statements/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statementId\":1,\"selectedRowIndices\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transactions imported successfully"));
    }

    // ── GET /api/v1/statements ────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getUserStatements_returns200() throws Exception {
        when(statementService.getUserStatements(1L)).thenReturn(List.of(sampleStatement));

        mockMvc.perform(get("/api/v1/statements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fileName").value("statement.pdf"));
    }

    // ── DELETE /api/v1/statements/{id} ────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void deleteStatement_returns200() throws Exception {
        doNothing().when(statementService).deleteStatement(1L, 1L);

        mockMvc.perform(delete("/api/v1/statements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── GET /api/v1/statements/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getStatementById_returns200() throws Exception {
        when(statementService.getStatementById(1L, 1L)).thenReturn(sampleStatement);

        mockMvc.perform(get("/api/v1/statements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
