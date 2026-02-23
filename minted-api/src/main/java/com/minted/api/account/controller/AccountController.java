package com.minted.api.account.controller;

import com.minted.api.account.dto.AccountRequest;
import com.minted.api.account.dto.AccountResponse;
import com.minted.api.user.entity.User;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAccounts(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<AccountResponse> accounts = accountService.getAllByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", accounts
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getAllActiveAccounts(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<AccountResponse> accounts = accountService.getAllActiveByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", accounts
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAccountById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        AccountResponse account = accountService.getById(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", account
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAccount(
            @Valid @RequestBody AccountRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        AccountResponse account = accountService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", account,
                "message", "Account created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        AccountResponse account = accountService.update(id, request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", account,
                "message", "Account updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAccount(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        accountService.delete(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account deleted successfully"
        ));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleAccount(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        accountService.toggleActive(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account status toggled successfully"
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
