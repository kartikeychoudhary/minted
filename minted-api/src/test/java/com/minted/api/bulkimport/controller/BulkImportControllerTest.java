package com.minted.api.bulkimport.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.auth.service.CustomUserDetailsService;
import com.minted.api.bulkimport.dto.BulkImportConfirmRequest;
import com.minted.api.bulkimport.dto.BulkImportResponse;
import com.minted.api.bulkimport.dto.CsvUploadResponse;
import com.minted.api.bulkimport.service.BulkImportService;
import com.minted.api.common.filter.JwtAuthFilter;
import com.minted.api.common.filter.MdcFilter;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.job.dto.JobExecutionResponse;
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

@WebMvcTest(BulkImportController.class)
@Import(TestSecurityConfig.class)
class BulkImportControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean BulkImportService bulkImportService;
    @MockBean UserRepository userRepository;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean MdcFilter mdcFilter;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    private BulkImportResponse sampleImport;

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

        sampleImport = new BulkImportResponse(
                1L, 2L, "Savings", "MINTED_CSV", "import.csv",
                1024L, 10, 9, 1, 0, 9,
                "COMPLETED", null, null, null, null
        );
    }

    // ── GET /api/v1/imports/template ──────────────────────────────────────────

    @Test
    void downloadTemplate_returns200WithCsvBytes() throws Exception {
        byte[] csvBytes = "date,amount,type\n".getBytes();
        when(bulkImportService.getCsvTemplate()).thenReturn(csvBytes);

        mockMvc.perform(get("/api/v1/imports/template"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=minted_import_template.csv"));
    }

    // ── POST /api/v1/imports/upload ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void uploadCsv_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "import.csv", "text/csv", "date,amount\n2025-01-01,100\n".getBytes());
        CsvUploadResponse uploadResponse = new CsvUploadResponse(1L, 1, 1, 0, 0, List.of());
        when(bulkImportService.uploadAndValidate(any(), eq(2L), eq(1L))).thenReturn(uploadResponse);

        mockMvc.perform(multipart("/api/v1/imports/upload")
                        .file(file)
                        .param("accountId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.importId").value(1));
    }

    // ── POST /api/v1/imports/confirm ──────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void confirmImport_returns202() throws Exception {
        BulkImportConfirmRequest request = new BulkImportConfirmRequest(1L, true);
        when(bulkImportService.confirmImport(any(), eq(1L))).thenReturn(sampleImport);

        mockMvc.perform(post("/api/v1/imports/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    // ── GET /api/v1/imports ───────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getUserImports_returns200() throws Exception {
        when(bulkImportService.getUserImports(1L)).thenReturn(List.of(sampleImport));

        mockMvc.perform(get("/api/v1/imports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].fileName").value("import.csv"));
    }

    // ── GET /api/v1/imports/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getImportById_returns200() throws Exception {
        when(bulkImportService.getImportById(1L, 1L)).thenReturn(sampleImport);

        mockMvc.perform(get("/api/v1/imports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // ── GET /api/v1/imports/{id}/job ──────────────────────────────────────────

    @Test
    @WithMockUser(username = "alice")
    void getImportJobDetails_returns200() throws Exception {
        JobExecutionResponse jobResponse = new JobExecutionResponse(10L, "BULK_IMPORT", "COMPLETED", null, null, null, null, null, null, null);
        when(bulkImportService.getImportJobDetails(1L, 1L)).thenReturn(jobResponse);

        mockMvc.perform(get("/api/v1/imports/1/job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}
