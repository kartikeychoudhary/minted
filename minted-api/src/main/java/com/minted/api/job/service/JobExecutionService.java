package com.minted.api.job.service;

import com.minted.api.job.dto.JobExecutionResponse;
import com.minted.api.job.dto.JobScheduleConfigRequest;
import com.minted.api.job.dto.JobScheduleConfigResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobExecutionService {

    Page<JobExecutionResponse> getAllJobExecutions(Pageable pageable);

    JobExecutionResponse getJobExecutionById(Long id);

    void triggerJobManually(String jobName);

    List<JobScheduleConfigResponse> getAllScheduleConfigs();

    JobScheduleConfigResponse updateScheduleConfig(Long id, JobScheduleConfigRequest request);
}
