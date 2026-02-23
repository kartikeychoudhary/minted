package com.minted.api.dashboard.service;

import com.minted.api.dashboard.dto.DashboardCardRequest;
import com.minted.api.dashboard.dto.DashboardCardResponse;

import java.util.List;

public interface DashboardCardService {

    List<DashboardCardResponse> getAllActiveByUserId(Long userId);

    List<DashboardCardResponse> getAllByUserId(Long userId);

    DashboardCardResponse getById(Long id, Long userId);

    DashboardCardResponse create(DashboardCardRequest request, Long userId);

    DashboardCardResponse update(Long id, DashboardCardRequest request, Long userId);

    void delete(Long id, Long userId);

    void toggleActive(Long id, Long userId);

    void reorderCards(Long userId, List<Long> cardIds);
}
