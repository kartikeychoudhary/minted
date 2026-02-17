package com.minted.api.dto;

import com.minted.api.entity.AccountType;

import java.time.LocalDateTime;

public record AccountTypeResponse(
        Long id,
        String name,
        String description,
        String icon,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AccountTypeResponse from(AccountType accountType) {
        return new AccountTypeResponse(
                accountType.getId(),
                accountType.getName(),
                accountType.getDescription(),
                accountType.getIcon(),
                accountType.getIsActive(),
                accountType.getCreatedAt(),
                accountType.getUpdatedAt()
        );
    }
}
