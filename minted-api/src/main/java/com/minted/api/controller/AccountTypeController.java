package com.minted.api.controller;

import com.minted.api.dto.AccountTypeRequest;
import com.minted.api.dto.AccountTypeResponse;
import com.minted.api.entity.User;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.AccountTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/account-types")
@RequiredArgsConstructor
public class AccountTypeController {

    private final AccountTypeService accountTypeService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAccountTypes(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<AccountTypeResponse> accountTypes = accountTypeService.getAllByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", accountTypes
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getAllActiveAccountTypes(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<AccountTypeResponse> accountTypes = accountTypeService.getAllActiveByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", accountTypes
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAccountTypeById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        AccountTypeResponse accountType = accountTypeService.getById(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", accountType
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAccountType(
            @Valid @RequestBody AccountTypeRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        AccountTypeResponse accountType = accountTypeService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", accountType,
                "message", "Account type created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAccountType(
            @PathVariable Long id,
            @Valid @RequestBody AccountTypeRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        AccountTypeResponse accountType = accountTypeService.update(id, request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", accountType,
                "message", "Account type updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAccountType(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        accountTypeService.delete(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account type deleted successfully"
        ));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleAccountType(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        accountTypeService.toggleActive(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account type status toggled successfully"
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
