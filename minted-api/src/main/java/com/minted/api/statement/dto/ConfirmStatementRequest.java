package com.minted.api.statement.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmStatementRequest(
        @NotNull(message = "Statement ID is required")
        Long statementId,

        boolean skipDuplicates
) {}
