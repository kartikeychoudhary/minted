package com.minted.api.split.dto;

import java.math.BigDecimal;

public record FriendBalanceResponse(
        Long friendId,
        String friendName,
        String avatarColor,
        BigDecimal balance
) {}
