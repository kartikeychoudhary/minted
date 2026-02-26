package com.minted.api.dashboardconfig.repository;

import com.minted.api.dashboardconfig.entity.DashboardConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DashboardConfigurationRepository extends JpaRepository<DashboardConfiguration, Long> {
    Optional<DashboardConfiguration> findByUserId(Long userId);
}
