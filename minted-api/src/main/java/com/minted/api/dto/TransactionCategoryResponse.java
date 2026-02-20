package com.minted.api.dto;

import com.minted.api.entity.TransactionCategory;
import com.minted.api.enums.TransactionType;

import java.time.LocalDateTime;

public record TransactionCategoryResponse(
        Long id,
        String name,
        TransactionType type,
        String icon,
        String color,
        Long parentId,
        String parentName,
        Boolean isActive,
        Boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TransactionCategoryResponse from(TransactionCategory category) {
        return new TransactionCategoryResponse(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getIcon(),
                category.getColor(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                category.getIsActive(),
                category.getIsDefault(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
