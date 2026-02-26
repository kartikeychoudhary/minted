package com.minted.api.dashboardconfig.service;

import com.minted.api.dashboardconfig.dto.DashboardConfigRequest;
import com.minted.api.dashboardconfig.dto.DashboardConfigResponse;
import com.minted.api.dashboardconfig.entity.DashboardConfiguration;
import com.minted.api.dashboardconfig.repository.DashboardConfigurationRepository;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardConfigServiceImpl implements DashboardConfigService {

    private final DashboardConfigurationRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardConfigResponse getConfig(Long userId) {
        return repository.findByUserId(userId)
                .map(DashboardConfigResponse::from)
                .orElse(DashboardConfigResponse.empty());
    }

    @Override
    @Transactional
    public DashboardConfigResponse saveConfig(DashboardConfigRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DashboardConfiguration config = repository.findByUserId(userId)
                .orElseGet(() -> {
                    DashboardConfiguration newConfig = new DashboardConfiguration();
                    newConfig.setUser(user);
                    return newConfig;
                });

        String idsString = request.excludedCategoryIds() != null
                ? request.excludedCategoryIds().stream().map(String::valueOf).collect(Collectors.joining(","))
                : "";
        config.setExcludedCategoryIds(idsString);

        DashboardConfiguration saved = repository.save(config);
        log.info("Dashboard config saved for userId={}, excludedCategories={}", userId, idsString);
        return DashboardConfigResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getExcludedCategoryIds(Long userId) {
        return repository.findByUserId(userId)
                .map(config -> {
                    if (config.getExcludedCategoryIds() == null || config.getExcludedCategoryIds().isBlank()) {
                        return Collections.<Long>emptyList();
                    }
                    return Arrays.stream(config.getExcludedCategoryIds().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::parseLong)
                            .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());
    }
}
