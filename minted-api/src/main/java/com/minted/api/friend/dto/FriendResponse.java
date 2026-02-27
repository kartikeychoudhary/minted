package com.minted.api.friend.dto;

import com.minted.api.friend.entity.Friend;

import java.time.LocalDateTime;
import java.util.Base64;

public record FriendResponse(
        Long id,
        String name,
        String email,
        String phone,
        String avatarColor,
        String avatarBase64,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FriendResponse from(Friend friend) {
        return from(friend, true);
    }

    public static FriendResponse from(Friend friend, boolean includeAvatar) {
        String avatarBase64 = null;
        if (includeAvatar && friend.getAvatarData() != null && friend.getAvatarContentType() != null) {
            avatarBase64 = "data:" + friend.getAvatarContentType() + ";base64,"
                    + Base64.getEncoder().encodeToString(friend.getAvatarData());
        }
        return new FriendResponse(
                friend.getId(),
                friend.getName(),
                friend.getEmail(),
                friend.getPhone(),
                friend.getAvatarColor(),
                avatarBase64,
                friend.getIsActive(),
                friend.getCreatedAt(),
                friend.getUpdatedAt()
        );
    }
}
