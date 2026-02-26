package com.minted.api.dashboardconfig.dto;

import java.util.List;

public record DashboardConfigRequest(
        List<Long> excludedCategoryIds
) {}
