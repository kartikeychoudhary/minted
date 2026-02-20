package com.minted.api.dto;

import java.util.List;

public record CsvUploadResponse(
    Long importId,
    Integer totalRows,
    Integer validRows,
    Integer errorRows,
    Integer duplicateRows,
    List<CsvRowPreview> rows
) {}
