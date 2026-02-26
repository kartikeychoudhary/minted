package com.minted.api.dashboardconfig.dto;

import com.minted.api.dashboardconfig.entity.DashboardConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record DashboardConfigResponse(
        Long id,
        List<Long> excludedCategoryIds
) {
    public static DashboardConfigResponse from(DashboardConfiguration config) {
        List<Long> ids = Collections.emptyList();
        if (config.getExcludedCategoryIds() != null && !config.getExcludedCategoryIds().isBlank()) {
            ids = Arrays.stream(config.getExcludedCategoryIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
        return new DashboardConfigResponse(config.getId(), ids);
    }

    public static DashboardConfigResponse empty() {
        return new DashboardConfigResponse(null, Collections.emptyList());
    }
}
