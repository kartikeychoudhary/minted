package com.minted.api.controller;

import com.minted.api.dto.BulkImportConfirmRequest;
import com.minted.api.dto.BulkImportResponse;
import com.minted.api.dto.CsvUploadResponse;
import com.minted.api.dto.JobExecutionResponse;
import com.minted.api.entity.User;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.BulkImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/imports")
@RequiredArgsConstructor
public class BulkImportController {

    private final BulkImportService bulkImportService;
    private final UserRepository userRepository;

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] template = bulkImportService.getCsvTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=minted_import_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(template);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("accountId") Long accountId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        CsvUploadResponse response = bulkImportService.uploadAndValidate(file, accountId, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
        ));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmImport(
            @Valid @RequestBody BulkImportConfirmRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        BulkImportResponse response = bulkImportService.confirmImport(request, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "success", true,
                "data", response,
                "message", "Import job started successfully"
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserImports(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<BulkImportResponse> imports = bulkImportService.getUserImports(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", imports
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getImportById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        BulkImportResponse response = bulkImportService.getImportById(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
        ));
    }

    @GetMapping("/{id}/job")
    public ResponseEntity<Map<String, Object>> getImportJobDetails(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        JobExecutionResponse response = bulkImportService.getImportJobDetails(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
