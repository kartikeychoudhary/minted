package com.minted.api.integration.dto;

public record PushResult(
        Long splitTransactionId,
        String description,
        boolean success,
        boolean alreadyPushed,
        Long splitwiseExpenseId,
        String errorMessage
) {}
