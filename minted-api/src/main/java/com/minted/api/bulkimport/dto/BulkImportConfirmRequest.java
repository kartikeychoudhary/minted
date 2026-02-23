package com.minted.api.bulkimport.dto;

import jakarta.validation.constraints.NotNull;

public record BulkImportConfirmRequest(
    @NotNull Long importId,
    boolean skipDuplicates
) {}
