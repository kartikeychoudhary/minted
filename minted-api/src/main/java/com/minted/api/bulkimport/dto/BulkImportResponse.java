package com.minted.api.bulkimport.dto;

import com.minted.api.bulkimport.entity.BulkImport;

import java.time.LocalDateTime;

public record BulkImportResponse(
    Long id,
    Long accountId,
    String accountName,
    String importType,
    String fileName,
    Long fileSize,
    Integer totalRows,
    Integer validRows,
    Integer duplicateRows,
    Integer errorRows,
    Integer importedRows,
    String status,
    Long jobExecutionId,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static BulkImportResponse from(BulkImport bi) {
        return new BulkImportResponse(
            bi.getId(),
            bi.getAccount().getId(),
            bi.getAccount().getName(),
            bi.getImportType().name(),
            bi.getFileName(),
            bi.getFileSize(),
            bi.getTotalRows(),
            bi.getValidRows(),
            bi.getDuplicateRows(),
            bi.getErrorRows(),
            bi.getImportedRows(),
            bi.getStatus().name(),
            bi.getJobExecution() != null ? bi.getJobExecution().getId() : null,
            bi.getErrorMessage(),
            bi.getCreatedAt(),
            bi.getUpdatedAt()
        );
    }
}
