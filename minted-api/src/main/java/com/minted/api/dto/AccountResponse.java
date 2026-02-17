package com.minted.api.dto;

import com.minted.api.entity.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String name,
        Long accountTypeId,
        String accountTypeName,
        BigDecimal balance,
        String currency,
        String color,
        String icon,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getAccountType().getId(),
                account.getAccountType().getName(),
                account.getBalance(),
                account.getCurrency(),
                account.getColor(),
                account.getIcon(),
                account.getIsActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
