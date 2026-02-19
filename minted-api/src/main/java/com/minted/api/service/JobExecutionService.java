package com.minted.api.service;

import com.minted.api.dto.JobExecutionResponse;
import com.minted.api.dto.JobScheduleConfigRequest;
import com.minted.api.dto.JobScheduleConfigResponse;
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
