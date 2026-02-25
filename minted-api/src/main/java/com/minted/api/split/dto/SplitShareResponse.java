package com.minted.api.split.dto;

import com.minted.api.split.entity.SplitShare;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SplitShareResponse(
        Long id,
        Long friendId,
        String friendName,
        String friendAvatarColor,
        BigDecimal shareAmount,
        BigDecimal sharePercentage,
        Boolean isPayer,
        Boolean isSettled,
        LocalDateTime settledAt,
        String splitDescription,
        String splitCategoryName,
        String splitTransactionDate
) {
    public static SplitShareResponse from(SplitShare share) {
        return new SplitShareResponse(
                share.getId(),
                share.getFriend() != null ? share.getFriend().getId() : null,
                share.getFriend() != null ? share.getFriend().getName() : "Me",
                share.getFriend() != null ? share.getFriend().getAvatarColor() : null,
                share.getShareAmount(),
                share.getSharePercentage(),
                share.getIsPayer(),
                share.getIsSettled(),
                share.getSettledAt(),
                share.getSplitTransaction().getDescription(),
                share.getSplitTransaction().getCategoryName(),
                share.getSplitTransaction().getTransactionDate().toString()
        );
    }
}
