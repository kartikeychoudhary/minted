package com.minted.api.dto;

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
