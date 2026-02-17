package com.minted.api.service;

import com.minted.api.dto.BudgetRequest;
import com.minted.api.dto.BudgetResponse;

import java.util.List;

public interface BudgetService {

    List<BudgetResponse> getAllByUserId(Long userId);

    List<BudgetResponse> getAllByUserIdAndMonthYear(Long userId, Integer month, Integer year);

    List<BudgetResponse> getAllByUserIdAndYear(Long userId, Integer year);

    BudgetResponse getById(Long id, Long userId);

    BudgetResponse create(BudgetRequest request, Long userId);

    BudgetResponse update(Long id, BudgetRequest request, Long userId);

    void delete(Long id, Long userId);
}
