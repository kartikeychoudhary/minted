package com.minted.api.job.dto;

import jakarta.validation.constraints.NotBlank;

public record JobScheduleConfigRequest(
        @NotBlank(message = "Cron expression is required")
        String cronExpression,
        Boolean enabled
) {
}
