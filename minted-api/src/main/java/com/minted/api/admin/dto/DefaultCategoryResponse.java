package com.minted.api.admin.dto;

public record DefaultCategoryResponse(
        Long id,
        String name,
        String icon,
        String type
) {
}
