package com.minted.api.dto;

public record DefaultCategoryResponse(
        Long id,
        String name,
        String icon,
        String type
) {
}
