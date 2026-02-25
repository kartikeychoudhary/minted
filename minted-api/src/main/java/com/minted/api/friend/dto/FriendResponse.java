package com.minted.api.friend.dto;

import com.minted.api.friend.entity.Friend;

import java.time.LocalDateTime;

public record FriendResponse(
        Long id,
        String name,
        String email,
        String phone,
        String avatarColor,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FriendResponse from(Friend friend) {
        return new FriendResponse(
                friend.getId(),
                friend.getName(),
                friend.getEmail(),
                friend.getPhone(),
                friend.getAvatarColor(),
                friend.getIsActive(),
                friend.getCreatedAt(),
                friend.getUpdatedAt()
        );
    }
}
