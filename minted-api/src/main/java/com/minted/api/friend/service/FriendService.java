package com.minted.api.friend.service;

import com.minted.api.friend.dto.FriendRequest;
import com.minted.api.friend.dto.FriendResponse;

import java.util.List;

public interface FriendService {

    List<FriendResponse> getAllByUserId(Long userId);

    FriendResponse getById(Long id, Long userId);

    FriendResponse create(FriendRequest request, Long userId);

    FriendResponse update(Long id, FriendRequest request, Long userId);

    void delete(Long id, Long userId);
}
