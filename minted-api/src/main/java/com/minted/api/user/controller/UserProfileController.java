package com.minted.api.user.controller;

import com.minted.api.user.dto.UserProfileUpdateRequest;
import com.minted.api.user.dto.UserResponse;
import com.minted.api.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        String username = authentication.getName();
        UserResponse profile = userProfileService.getProfile(username);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", profile
        ));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        String username = authentication.getName();
        UserResponse updated = userProfileService.updateProfile(username, request);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", updated,
                "message", "Profile updated successfully"
        ));
    }
}
