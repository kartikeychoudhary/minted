package com.minted.api.split.dto;

import jakarta.validation.constraints.NotNull;

public record SettleRequest(
        @NotNull(message = "Friend ID is required")
        Long friendId
) {}
