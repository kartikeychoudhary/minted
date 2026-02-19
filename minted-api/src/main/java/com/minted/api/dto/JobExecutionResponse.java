package com.minted.api.dto;

import com.minted.api.enums.JobStatus;
import com.minted.api.enums.JobTriggerType;

import java.time.LocalDateTime;
import java.util.List;

public record JobExecutionResponse(
        Long id,
        String jobName,
        String status,
        String triggerType,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String errorMessage,
        Integer totalSteps,
        Integer completedSteps,
        List<JobStepExecutionResponse> steps
) {
}
