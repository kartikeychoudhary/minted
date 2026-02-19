package com.minted.api.dto;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        String email,
        Boolean forcePasswordChange,
        String currency
) {
}
