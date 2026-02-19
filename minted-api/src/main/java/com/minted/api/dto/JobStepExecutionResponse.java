package com.minted.api.dto;

import java.time.LocalDateTime;

public record JobStepExecutionResponse(
        Long id,
        String stepName,
        Integer stepOrder,
        String status,
        String contextJson,
        String errorMessage,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
