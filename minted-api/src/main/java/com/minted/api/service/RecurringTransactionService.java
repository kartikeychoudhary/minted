package com.minted.api.service;

import com.minted.api.dto.RecurringTransactionRequest;
import com.minted.api.dto.RecurringTransactionResponse;
import com.minted.api.dto.RecurringSummaryResponse;

import java.util.List;

public interface RecurringTransactionService {

    List<RecurringTransactionResponse> getAllByUserId(Long userId);

    RecurringTransactionResponse getById(Long id, Long userId);

    RecurringTransactionResponse create(RecurringTransactionRequest request, Long userId);

    RecurringTransactionResponse update(Long id, RecurringTransactionRequest request, Long userId);

    void delete(Long id, Long userId);

    void toggleStatus(Long id, Long userId);

    RecurringSummaryResponse getSummary(Long userId);

    List<RecurringTransactionResponse> search(Long userId, String query);
}
