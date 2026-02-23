package com.minted.api.transaction.service;

import com.minted.api.transaction.dto.TransactionCategoryRequest;
import com.minted.api.transaction.dto.TransactionCategoryResponse;
import com.minted.api.transaction.enums.TransactionType;

import java.util.List;

public interface TransactionCategoryService {

    List<TransactionCategoryResponse> getAllByUserId(Long userId);

    List<TransactionCategoryResponse> getAllActiveByUserId(Long userId);

    List<TransactionCategoryResponse> getAllByUserIdAndType(Long userId, TransactionType type);

    TransactionCategoryResponse getById(Long id, Long userId);

    TransactionCategoryResponse create(TransactionCategoryRequest request, Long userId);

    TransactionCategoryResponse update(Long id, TransactionCategoryRequest request, Long userId);

    void delete(Long id, Long userId);

    void toggleActive(Long id, Long userId);
}
