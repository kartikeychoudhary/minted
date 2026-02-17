package com.minted.api.dto;

public record LoginResponse(
        String token,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
}
