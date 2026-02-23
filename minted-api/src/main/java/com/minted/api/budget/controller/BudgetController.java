package com.minted.api.budget.controller;

import com.minted.api.budget.dto.BudgetRequest;
import com.minted.api.budget.dto.BudgetResponse;
import com.minted.api.user.entity.User;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBudgets(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<BudgetResponse> budgets = budgetService.getAllByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", budgets
        ));
    }

    @GetMapping("/month/{month}/year/{year}")
    public ResponseEntity<Map<String, Object>> getBudgetsByMonthYear(
            @PathVariable Integer month,
            @PathVariable Integer year,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<BudgetResponse> budgets = budgetService.getAllByUserIdAndMonthYear(userId, month, year);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", budgets
        ));
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<Map<String, Object>> getBudgetsByYear(
            @PathVariable Integer year,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<BudgetResponse> budgets = budgetService.getAllByUserIdAndYear(userId, year);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", budgets
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBudgetById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        BudgetResponse budget = budgetService.getById(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", budget
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBudget(
            @Valid @RequestBody BudgetRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        BudgetResponse budget = budgetService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", budget,
                "message", "Budget created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        BudgetResponse budget = budgetService.update(id, request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", budget,
                "message", "Budget updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBudget(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        budgetService.delete(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Budget deleted successfully"
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
