package com.minted.api.service;

import com.minted.api.dto.BulkImportConfirmRequest;
import com.minted.api.dto.BulkImportResponse;
import com.minted.api.dto.CsvUploadResponse;
import com.minted.api.dto.JobExecutionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BulkImportService {
    byte[] getCsvTemplate();
    CsvUploadResponse uploadAndValidate(MultipartFile file, Long accountId, Long userId);
    BulkImportResponse confirmImport(BulkImportConfirmRequest request, Long userId);
    List<BulkImportResponse> getUserImports(Long userId);
    BulkImportResponse getImportById(Long importId, Long userId);
    JobExecutionResponse getImportJobDetails(Long importId, Long userId);
}
