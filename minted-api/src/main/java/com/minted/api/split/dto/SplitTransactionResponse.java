package com.minted.api.split.dto;

import com.minted.api.split.entity.SplitShare;
import com.minted.api.split.entity.SplitTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record SplitTransactionResponse(
        Long id,
        Long sourceTransactionId,
        String description,
        String categoryName,
        BigDecimal totalAmount,
        String splitType,
        LocalDate transactionDate,
        Boolean isSettled,
        BigDecimal yourShare,
        List<SplitShareResponse> shares,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SplitTransactionResponse from(SplitTransaction st) {
        List<SplitShareResponse> shareResponses = st.getShares().stream()
                .map(SplitShareResponse::from)
                .collect(Collectors.toList());

        // "Your share" is the share where friend is null (the user's own share)
        BigDecimal yourShare = st.getShares().stream()
                .filter(s -> s.getFriend() == null)
                .map(SplitShare::getShareAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        return new SplitTransactionResponse(
                st.getId(),
                st.getSourceTransaction() != null ? st.getSourceTransaction().getId() : null,
                st.getDescription(),
                st.getCategoryName(),
                st.getTotalAmount(),
                st.getSplitType().name(),
                st.getTransactionDate(),
                st.getIsSettled(),
                yourShare,
                shareResponses,
                st.getCreatedAt(),
                st.getUpdatedAt()
        );
    }
}
