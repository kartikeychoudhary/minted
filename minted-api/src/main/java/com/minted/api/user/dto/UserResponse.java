package com.minted.api.user.dto;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        String email,
        Boolean forcePasswordChange,
        String currency,
        String role
) {
}
