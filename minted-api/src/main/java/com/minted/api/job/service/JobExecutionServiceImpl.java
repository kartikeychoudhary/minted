package com.minted.api.job.service;

import com.minted.api.job.dto.JobExecutionResponse;
import com.minted.api.job.dto.JobScheduleConfigRequest;
import com.minted.api.job.dto.JobScheduleConfigResponse;
import com.minted.api.job.dto.JobStepExecutionResponse;
import com.minted.api.job.entity.JobExecution;
import com.minted.api.job.entity.JobScheduleConfig;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.job.repository.JobExecutionRepository;
import com.minted.api.job.repository.JobScheduleConfigRepository;
import com.minted.api.job.service.JobExecutionService;
import com.minted.api.job.service.JobSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobExecutionServiceImpl implements JobExecutionService {

    private final JobExecutionRepository jobExecutionRepository;
    private final JobScheduleConfigRepository scheduleConfigRepository;
    private final JobSchedulerService jobSchedulerService;

    @Override
    @Transactional(readOnly = true)
    public Page<JobExecutionResponse> getAllJobExecutions(Pageable pageable) {
        return jobExecutionRepository.findAllByOrderByStartTimeDesc(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public JobExecutionResponse getJobExecutionById(Long id) {
        JobExecution execution = jobExecutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job execution not found"));
        return mapToResponse(execution);
    }

    @Override
    @Transactional
    public void triggerJobManually(String jobName) {
        log.info("Manual trigger requested for job: {}", jobName);
        JobScheduleConfig config = scheduleConfigRepository.findByJobName(jobName)
                .orElseThrow(() -> new ResourceNotFoundException("Job configuration not found for: " + jobName));
        
        // Asynchronously trigger the job
        jobSchedulerService.triggerJob(jobName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobScheduleConfigResponse> getAllScheduleConfigs() {
        return scheduleConfigRepository.findAll().stream()
                .map(config -> new JobScheduleConfigResponse(
                        config.getId(),
                        config.getJobName(),
                        config.getCronExpression(),
                        config.getEnabled(),
                        config.getLastRunAt(),
                        config.getDescription()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobScheduleConfigResponse updateScheduleConfig(Long id, JobScheduleConfigRequest request) {
        JobScheduleConfig config = scheduleConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule configuration not found"));

        boolean cronChanged = !config.getCronExpression().equals(request.cronExpression());
        boolean enabledChanged = !config.getEnabled().equals(request.enabled());

        config.setCronExpression(request.cronExpression());
        config.setEnabled(request.enabled());

        JobScheduleConfig saved = scheduleConfigRepository.save(config);

        if (cronChanged || enabledChanged) {
            log.info("Schedule config updated for job {}. Rescheduling...", config.getJobName());
            jobSchedulerService.rescheduleJob(config.getJobName(), request.cronExpression(), request.enabled());
        }

        return new JobScheduleConfigResponse(
                saved.getId(),
                saved.getJobName(),
                saved.getCronExpression(),
                saved.getEnabled(),
                saved.getLastRunAt(),
                saved.getDescription()
        );
    }

    private JobExecutionResponse mapToResponse(JobExecution execution) {
        List<JobStepExecutionResponse> stepResponses = execution.getSteps().stream()
                .map(step -> new JobStepExecutionResponse(
                        step.getId(),
                        step.getStepName(),
                        step.getStepOrder(),
                        step.getStatus().name(),
                        step.getContextJson(),
                        step.getErrorMessage(),
                        step.getStartTime(),
                        step.getEndTime()
                ))
                .collect(Collectors.toList());

        return new JobExecutionResponse(
                execution.getId(),
                execution.getJobName(),
                execution.getStatus().name(),
                execution.getTriggerType().name(),
                execution.getStartTime(),
                execution.getEndTime(),
                execution.getErrorMessage(),
                execution.getTotalSteps(),
                execution.getCompletedSteps(),
                stepResponses
        );
    }
}
