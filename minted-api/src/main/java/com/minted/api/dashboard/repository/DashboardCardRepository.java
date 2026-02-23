package com.minted.api.dashboard.repository;

import com.minted.api.dashboard.entity.DashboardCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DashboardCardRepository extends JpaRepository<DashboardCard, Long> {

    List<DashboardCard> findByUserIdAndIsActiveTrueOrderByPositionOrderAsc(Long userId);

    List<DashboardCard> findByUserIdOrderByPositionOrderAsc(Long userId);

    Optional<DashboardCard> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
