package com.minted.api.split.dto;

import java.math.BigDecimal;

public record SplitBalanceSummaryResponse(
        BigDecimal youAreOwed,
        BigDecimal youOwe
) {}
