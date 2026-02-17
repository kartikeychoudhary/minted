package com.minted.api.controller;

import com.minted.api.dto.TransactionCategoryRequest;
import com.minted.api.dto.TransactionCategoryResponse;
import com.minted.api.entity.User;
import com.minted.api.enums.TransactionType;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.TransactionCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class TransactionCategoryController {

    private final TransactionCategoryService categoryService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<TransactionCategoryResponse> categories = categoryService.getAllByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", categories
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getAllActiveCategories(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<TransactionCategoryResponse> categories = categoryService.getAllActiveByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", categories
        ));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getCategoriesByType(
            @PathVariable TransactionType type,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<TransactionCategoryResponse> categories = categoryService.getAllByUserIdAndType(userId, type);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", categories
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        TransactionCategoryResponse category = categoryService.getById(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", category
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(
            @Valid @RequestBody TransactionCategoryRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        TransactionCategoryResponse category = categoryService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", category,
                "message", "Category created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody TransactionCategoryRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        TransactionCategoryResponse category = categoryService.update(id, request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", category,
                "message", "Category updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        categoryService.delete(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Category deleted successfully"
        ));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleCategory(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        categoryService.toggleActive(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Category status toggled successfully"
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
