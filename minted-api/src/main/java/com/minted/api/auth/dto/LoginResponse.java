package com.minted.api.auth.dto;

import com.minted.api.user.dto.UserResponse;

public record LoginResponse(
        String token,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
}
