package com.minted.api.job.dto;

import java.time.LocalDateTime;

public record JobScheduleConfigResponse(
        Long id,
        String jobName,
        String cronExpression,
        Boolean enabled,
        LocalDateTime lastRunAt,
        String description
) {
}
