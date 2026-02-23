package com.minted.api.dashboard.dto;

import com.minted.api.dashboard.enums.CardWidth;
import com.minted.api.dashboard.enums.ChartType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DashboardCardRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must not exceed 100 characters")
        String title,

        @NotNull(message = "Chart type is required")
        ChartType chartType,

        @NotBlank(message = "X-axis measure is required")
        @Size(max = 50, message = "X-axis measure must not exceed 50 characters")
        String xAxisMeasure,

        @NotBlank(message = "Y-axis measure is required")
        @Size(max = 50, message = "Y-axis measure must not exceed 50 characters")
        String yAxisMeasure,

        String filters,

        Integer positionOrder,

        CardWidth width
) {}
