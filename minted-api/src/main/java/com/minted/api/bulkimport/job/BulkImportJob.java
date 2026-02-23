package com.minted.api.bulkimport.job;

import com.minted.api.bulkimport.entity.BulkImport;
import com.minted.api.bulkimport.enums.ImportStatus;
import com.minted.api.bulkimport.repository.BulkImportRepository;
import com.minted.api.job.repository.JobScheduleConfigRepository;
import com.minted.api.job.service.JobSchedulerService;
import com.minted.api.bulkimport.service.BulkImportServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BulkImportJob implements Runnable {

    public static final String JOB_NAME = "BULK_IMPORT_PROCESSOR";

    private final BulkImportRepository bulkImportRepository;
    private final JobScheduleConfigRepository scheduleConfigRepository;
    private final JobSchedulerService jobSchedulerService;
    private final BulkImportServiceImpl bulkImportService;

    @PostConstruct
    public void init() {
        scheduleConfigRepository.findByJobName(JOB_NAME).ifPresent(config -> {
            jobSchedulerService.registerJob(JOB_NAME, this, config.getCronExpression(), config.getEnabled());
        });
    }

    @Override
    public void run() {
        log.info("Starting BulkImportJob sweep for stuck imports...");

        List<BulkImport> stuckImports = bulkImportRepository.findByStatus(ImportStatus.IMPORTING);

        if (stuckImports.isEmpty()) {
            log.info("No stuck imports found.");
            return;
        }

        for (BulkImport bi : stuckImports) {
            // Only process if the import has a job execution that isn't actively running
            if (bi.getJobExecution() == null) {
                log.info("Processing stuck import ID: {} (no job execution linked)", bi.getId());
                try {
                    bulkImportService.processImportAsync(bi.getId());
                } catch (Exception e) {
                    log.error("Failed to process stuck import ID: {}", bi.getId(), e);
                }
            }
        }

        log.info("BulkImportJob sweep completed.");
    }
}
