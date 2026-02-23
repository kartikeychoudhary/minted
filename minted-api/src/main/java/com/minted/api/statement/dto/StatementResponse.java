package com.minted.api.statement.dto;

import com.minted.api.statement.entity.CreditCardStatement;
import com.minted.api.statement.enums.StatementStatus;

import java.time.LocalDateTime;

public record StatementResponse(
        Long id,
        Long accountId,
        String accountName,
        String fileName,
        Long fileSize,
        StatementStatus status,
        Integer currentStep,
        String extractedText,
        Integer parsedCount,
        Integer duplicateCount,
        Integer importedCount,
        String errorMessage,
        Long jobExecutionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StatementResponse from(CreditCardStatement statement) {
        return new StatementResponse(
                statement.getId(),
                statement.getAccount().getId(),
                statement.getAccount().getName(),
                statement.getFileName(),
                statement.getFileSize(),
                statement.getStatus(),
                statement.getCurrentStep(),
                statement.getExtractedText(),
                statement.getParsedCount(),
                statement.getDuplicateCount(),
                statement.getImportedCount(),
                statement.getErrorMessage(),
                statement.getJobExecution() != null ? statement.getJobExecution().getId() : null,
                statement.getCreatedAt(),
                statement.getUpdatedAt()
        );
    }
}
