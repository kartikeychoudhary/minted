package com.minted.api.split.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SplitShareRequest(
        Long friendId,

        @NotNull(message = "Share amount is required")
        BigDecimal shareAmount,

        BigDecimal sharePercentage,

        Boolean isPayer
) {}
