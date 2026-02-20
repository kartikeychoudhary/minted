package com.minted.api.dto;

import jakarta.validation.constraints.NotNull;

public record BulkImportConfirmRequest(
    @NotNull Long importId,
    boolean skipDuplicates
) {}
