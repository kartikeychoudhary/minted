package com.minted.api.dto;

import com.minted.api.entity.DashboardCard;
import com.minted.api.enums.CardWidth;
import com.minted.api.enums.ChartType;

import java.time.LocalDateTime;

public record DashboardCardResponse(
        Long id,
        String title,
        ChartType chartType,
        String xAxisMeasure,
        String yAxisMeasure,
        String filters,
        Integer positionOrder,
        CardWidth width,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static DashboardCardResponse from(DashboardCard card) {
        return new DashboardCardResponse(
                card.getId(),
                card.getTitle(),
                card.getChartType(),
                card.getXAxisMeasure(),
                card.getYAxisMeasure(),
                card.getFilters(),
                card.getPositionOrder(),
                card.getWidth(),
                card.getIsActive(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
