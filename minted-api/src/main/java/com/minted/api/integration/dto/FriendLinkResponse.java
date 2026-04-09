package com.minted.api.integration.dto;

import java.time.LocalDateTime;

public record FriendLinkResponse(
        Long friendId,
        String friendName,
        Long splitwiseFriendId,
        String splitwiseFriendName,
        String splitwiseFriendEmail,
        LocalDateTime linkedAt
) {}
