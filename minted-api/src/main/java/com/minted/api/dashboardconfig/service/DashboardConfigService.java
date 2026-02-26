package com.minted.api.dashboardconfig.service;

import com.minted.api.dashboardconfig.dto.DashboardConfigRequest;
import com.minted.api.dashboardconfig.dto.DashboardConfigResponse;

import java.util.List;

public interface DashboardConfigService {
    DashboardConfigResponse getConfig(Long userId);
    DashboardConfigResponse saveConfig(DashboardConfigRequest request, Long userId);
    List<Long> getExcludedCategoryIds(Long userId);
}
