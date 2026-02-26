package com.minted.api.transaction.controller;

import com.minted.api.transaction.dto.TransactionRequest;
import com.minted.api.transaction.dto.TransactionResponse;
import com.minted.api.user.entity.User;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTransactions(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<TransactionResponse> transactions = transactionService.getAllByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", transactions
        ));
    }

    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<TransactionResponse> transactions = transactionService.getAllByUserIdAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", transactions
        ));
    }

    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> getTransactionsByFilters(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<TransactionResponse> transactions = transactionService.getAllByFilters(
                userId, accountId, categoryId, type, startDate, endDate
        );
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", transactions
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTransactionById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        TransactionResponse transaction = transactionService.getById(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", transaction
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        TransactionResponse transaction = transactionService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", transaction,
                "message", "Transaction created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        TransactionResponse transaction = transactionService.update(id, request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", transaction,
                "message", "Transaction updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTransaction(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        transactionService.delete(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Transaction deleted successfully"
        ));
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkDeleteTransactions(
            @RequestBody Map<String, List<Long>> request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<Long> ids = request.get("ids");
        transactionService.bulkDelete(ids, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Transactions deleted successfully"
        ));
    }

    @PutMapping("/bulk/category")
    public ResponseEntity<Map<String, Object>> bulkUpdateCategory(
            @RequestBody Map<String, Object> request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Number>) request.get("ids")).stream().map(Number::longValue).toList();
        Long categoryId = ((Number) request.get("categoryId")).longValue();
        transactionService.bulkUpdateCategory(ids, categoryId, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Transaction categories updated successfully"
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
