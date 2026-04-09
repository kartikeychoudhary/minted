package com.minted.api.integration.dto;

public record SplitwiseFriendDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String displayName,
        String avatarUrl,
        Long linkedMintedFriendId
) {}
