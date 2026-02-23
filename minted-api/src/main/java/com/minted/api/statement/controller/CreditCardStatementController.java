package com.minted.api.statement.controller;

import com.minted.api.statement.dto.StatementResponse;
import com.minted.api.statement.dto.ConfirmStatementRequest;
import com.minted.api.statement.dto.ParsedTransactionRow;
import com.minted.api.user.entity.User;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.statement.service.CreditCardStatementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class CreditCardStatementController {

    private final CreditCardStatementService statementService;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("accountId") Long accountId,
            @RequestParam(value = "pdfPassword", required = false) String pdfPassword,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        StatementResponse response = statementService.uploadAndExtract(file, accountId, pdfPassword, userId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PostMapping("/{id}/parse")
    public ResponseEntity<Map<String, Object>> triggerParse(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        StatementResponse response = statementService.triggerLlmParse(id, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "success", true,
                "data", response,
                "message", "AI parsing started"
        ));
    }

    @GetMapping("/{id}/parsed-rows")
    public ResponseEntity<Map<String, Object>> getParsedRows(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<ParsedTransactionRow> rows = statementService.getParsedRows(id, userId);
        return ResponseEntity.ok(Map.of("success", true, "data", rows));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmImport(
            @Valid @RequestBody ConfirmStatementRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        statementService.confirmImport(request, userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Transactions imported successfully"));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserStatements(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<StatementResponse> statements = statementService.getUserStatements(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", statements));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStatementById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        StatementResponse response = statementService.getStatementById(id, userId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
