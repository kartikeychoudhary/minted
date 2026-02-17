package com.minted.api.controller;

import com.minted.api.dto.RecurringTransactionRequest;
import com.minted.api.dto.RecurringTransactionResponse;
import com.minted.api.dto.RecurringSummaryResponse;
import com.minted.api.entity.User;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<RecurringTransactionResponse> list = recurringService.getAllByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", list
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        RecurringTransactionResponse response = recurringService.getById(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestBody RecurringTransactionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        RecurringTransactionResponse response = recurringService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", response,
                "message", "Recurring transaction created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody RecurringTransactionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        RecurringTransactionResponse response = recurringService.update(id, request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response,
                "message", "Recurring transaction updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        recurringService.delete(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recurring transaction deleted successfully"
        ));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleStatus(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        recurringService.toggleStatus(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Recurring transaction status toggled successfully"
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(Authentication authentication) {
        Long userId = getUserId(authentication);
        RecurringSummaryResponse summary = recurringService.getSummary(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", summary
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String q,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<RecurringTransactionResponse> results = recurringService.search(userId, q);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", results
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
