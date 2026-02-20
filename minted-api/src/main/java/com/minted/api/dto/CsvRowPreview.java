package com.minted.api.dto;

public record CsvRowPreview(
    Integer rowNumber,
    String date,
    String amount,
    String type,
    String description,
    String categoryName,
    String notes,
    String tags,
    String status,
    String errorMessage,
    Long matchedCategoryId,
    boolean isDuplicate
) {}
