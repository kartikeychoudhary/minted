package com.minted.api.dashboard.dto;

import java.util.List;

public record ChartDataResponse(
        List<String> labels,
        List<ChartDataset> datasets
) {
    public record ChartDataset(
            String label,
            List<Number> data,
            List<String> backgroundColor
    ) {}
}
