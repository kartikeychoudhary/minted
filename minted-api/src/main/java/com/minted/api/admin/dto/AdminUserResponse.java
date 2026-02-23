package com.minted.api.admin.dto;

import com.minted.api.user.entity.User;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String username,
        String displayName,
        String email,
        Boolean isActive,
        Boolean forcePasswordChange,
        String currency,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getIsActive(),
                user.getForcePasswordChange(),
                user.getCurrency(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
