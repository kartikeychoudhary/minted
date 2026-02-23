package com.minted.api.dto;

import com.minted.api.entity.MerchantCategoryMapping;

import java.util.List;

public record MerchantMappingResponse(
        Long id,
        String snippets,
        List<String> snippetList,
        Long categoryId,
        String categoryName,
        String categoryIcon,
        String categoryColor
) {
    public static MerchantMappingResponse from(MerchantCategoryMapping mapping) {
        return new MerchantMappingResponse(
                mapping.getId(),
                mapping.getSnippets(),
                mapping.getSnippetList(),
                mapping.getCategory().getId(),
                mapping.getCategory().getName(),
                mapping.getCategory().getIcon(),
                mapping.getCategory().getColor()
        );
    }
}
