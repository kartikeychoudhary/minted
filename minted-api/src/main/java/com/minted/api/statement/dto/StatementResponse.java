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
        String fileType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static StatementResponse from(CreditCardStatement statement) {
        // Suppress extractedText for statuses beyond TEXT_EXTRACTED to avoid bloating responses
        String text = statement.getExtractedText();
        StatementStatus status = statement.getStatus();
        if (status == StatementStatus.SENT_FOR_AI_PARSING || status == StatementStatus.LLM_PARSED
                || status == StatementStatus.CONFIRMING || status == StatementStatus.COMPLETED) {
            text = null;
        }

        return new StatementResponse(
                statement.getId(),
                statement.getAccount().getId(),
                statement.getAccount().getName(),
                statement.getFileName(),
                statement.getFileSize(),
                statement.getStatus(),
                statement.getCurrentStep(),
                text,
                statement.getParsedCount(),
                statement.getDuplicateCount(),
                statement.getImportedCount(),
                statement.getErrorMessage(),
                statement.getJobExecution() != null ? statement.getJobExecution().getId() : null,
                statement.getFileType(),
                statement.getCreatedAt(),
                statement.getUpdatedAt()
        );
    }
}
