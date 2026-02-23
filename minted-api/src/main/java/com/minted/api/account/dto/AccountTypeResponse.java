package com.minted.api.account.dto;

import com.minted.api.account.entity.AccountType;

import java.time.LocalDateTime;

public record AccountTypeResponse(
        Long id,
        String name,
        String description,
        String icon,
        Boolean isActive,
        Boolean isDefault,
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
                accountType.getIsDefault(),
                accountType.getCreatedAt(),
                accountType.getUpdatedAt()
        );
    }
}
