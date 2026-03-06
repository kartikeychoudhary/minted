package com.minted.api.job.service;

import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.job.dto.JobExecutionResponse;
import com.minted.api.job.dto.JobScheduleConfigRequest;
import com.minted.api.job.dto.JobScheduleConfigResponse;
import com.minted.api.job.entity.JobExecution;
import com.minted.api.job.entity.JobScheduleConfig;
import com.minted.api.job.enums.JobStatus;
import com.minted.api.job.enums.JobTriggerType;
import com.minted.api.job.repository.JobExecutionRepository;
import com.minted.api.job.repository.JobScheduleConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobExecutionServiceImplTest {

    @Mock private JobExecutionRepository jobExecutionRepository;
    @Mock private JobScheduleConfigRepository scheduleConfigRepository;
    @Mock private JobSchedulerService jobSchedulerService;

    @InjectMocks
    private JobExecutionServiceImpl jobExecutionService;

    // ── getAllJobExecutions ────────────────────────────────────────────────────

    @Test
    void getAllJobExecutions_returnsPaginatedResults() {
        JobExecution je = buildJobExecution(1L, "recurringJob");
        PageRequest pageable = PageRequest.of(0, 10);
        when(jobExecutionRepository.findAllByOrderByStartTimeDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(je)));

        Page<JobExecutionResponse> result = jobExecutionService.getAllJobExecutions(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).jobName()).isEqualTo("recurringJob");
    }

    // ── getJobExecutionById ───────────────────────────────────────────────────

    @Test
    void getJobExecutionById_found_returnsResponse() {
        JobExecution je = buildJobExecution(1L, "recurringJob");
        when(jobExecutionRepository.findById(1L)).thenReturn(Optional.of(je));

        JobExecutionResponse response = jobExecutionService.getJobExecutionById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void getJobExecutionById_notFound_throwsResourceNotFound() {
        when(jobExecutionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExecutionService.getJobExecutionById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── triggerJobManually ────────────────────────────────────────────────────

    @Test
    void triggerJobManually_found_triggersJob() {
        JobScheduleConfig config = buildScheduleConfig(1L, "recurringJob");
        when(scheduleConfigRepository.findByJobName("recurringJob")).thenReturn(Optional.of(config));

        jobExecutionService.triggerJobManually("recurringJob");

        verify(jobSchedulerService).triggerJob("recurringJob");
    }

    @Test
    void triggerJobManually_notFound_throwsResourceNotFound() {
        when(scheduleConfigRepository.findByJobName("unknownJob")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExecutionService.triggerJobManually("unknownJob"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getAllScheduleConfigs ─────────────────────────────────────────────────

    @Test
    void getAllScheduleConfigs_returnsList() {
        JobScheduleConfig config = buildScheduleConfig(1L, "recurringJob");
        when(scheduleConfigRepository.findAll()).thenReturn(List.of(config));

        List<JobScheduleConfigResponse> result = jobExecutionService.getAllScheduleConfigs();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).jobName()).isEqualTo("recurringJob");
    }

    // ── updateScheduleConfig ──────────────────────────────────────────────────

    @Test
    void updateScheduleConfig_success_reschedulesOnChange() {
        JobScheduleConfig config = buildScheduleConfig(1L, "recurringJob");
        when(scheduleConfigRepository.findById(1L)).thenReturn(Optional.of(config));
        when(scheduleConfigRepository.save(config)).thenReturn(config);

        JobScheduleConfigRequest request = new JobScheduleConfigRequest("0 0 * * *", true);
        JobScheduleConfigResponse response = jobExecutionService.updateScheduleConfig(1L, request);

        assertThat(response.jobName()).isEqualTo("recurringJob");
        verify(jobSchedulerService).rescheduleJob("recurringJob", "0 0 * * *", true);
    }

    @Test
    void updateScheduleConfig_notFound_throwsResourceNotFound() {
        when(scheduleConfigRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExecutionService.updateScheduleConfig(99L,
                new JobScheduleConfigRequest("0 * * * *", true)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // helpers

    private JobExecution buildJobExecution(Long id, String jobName) {
        JobExecution je = new JobExecution();
        je.setId(id);
        je.setJobName(jobName);
        je.setStatus(JobStatus.COMPLETED);
        je.setTriggerType(JobTriggerType.SCHEDULED);
        je.setStartTime(LocalDateTime.now().minusMinutes(5));
        je.setEndTime(LocalDateTime.now());
        je.setTotalSteps(1);
        je.setCompletedSteps(1);
        je.setSteps(new ArrayList<>());
        return je;
    }

    private JobScheduleConfig buildScheduleConfig(Long id, String jobName) {
        JobScheduleConfig config = new JobScheduleConfig();
        config.setId(id);
        config.setJobName(jobName);
        config.setCronExpression("0 0 0 * * *");
        config.setEnabled(true);
        return config;
    }
}
