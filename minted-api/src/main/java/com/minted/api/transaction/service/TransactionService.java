package com.minted.api.transaction.service;

import com.minted.api.transaction.dto.TransactionRequest;
import com.minted.api.transaction.dto.TransactionResponse;
import com.minted.api.transaction.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    List<TransactionResponse> getAllByUserId(Long userId);

    List<TransactionResponse> getAllByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    List<TransactionResponse> getAllByFilters(Long userId, Long accountId, Long categoryId,
                                              TransactionType type, LocalDate startDate, LocalDate endDate);

    TransactionResponse getById(Long id, Long userId);

    TransactionResponse create(TransactionRequest request, Long userId);

    TransactionResponse update(Long id, TransactionRequest request, Long userId);

    void delete(Long id, Long userId);
}
