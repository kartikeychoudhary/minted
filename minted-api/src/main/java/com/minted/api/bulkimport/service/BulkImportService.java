package com.minted.api.bulkimport.service;

import com.minted.api.bulkimport.dto.BulkImportConfirmRequest;
import com.minted.api.bulkimport.dto.BulkImportResponse;
import com.minted.api.bulkimport.dto.CsvUploadResponse;
import com.minted.api.job.dto.JobExecutionResponse;
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
