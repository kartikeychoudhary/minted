package com.minted.api.auth.controller;

import com.minted.api.auth.dto.ChangePasswordRequest;
import com.minted.api.auth.dto.LoginRequest;
import com.minted.api.auth.dto.LoginResponse;
import com.minted.api.auth.dto.RefreshTokenRequest;
import com.minted.api.auth.dto.SignupRequest;
import com.minted.api.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response,
                "message", "Login successful"
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response,
                "message", "Token refreshed successfully"
        ));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        String username = authentication.getName();
        authService.changePassword(username, request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully"
        ));
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequest request) {
        LoginResponse response = authService.signup(request);
        return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "data", response,
                "message", "Account created successfully"
        ));
    }

    @GetMapping("/signup-enabled")
    public ResponseEntity<Map<String, Object>> isSignupEnabled() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", authService.isSignupEnabled()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "minted-api",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
