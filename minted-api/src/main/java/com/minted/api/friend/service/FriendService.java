package com.minted.api.friend.service;

import com.minted.api.friend.dto.FriendRequest;
import com.minted.api.friend.dto.FriendResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FriendService {

    List<FriendResponse> getAllByUserId(Long userId);

    List<FriendResponse> getAllByUserId(Long userId, boolean includeAvatar);

    FriendResponse getById(Long id, Long userId);

    FriendResponse create(FriendRequest request, Long userId);

    FriendResponse update(Long id, FriendRequest request, Long userId);

    void delete(Long id, Long userId);

    FriendResponse uploadAvatar(Long id, Long userId, MultipartFile file);

    FriendResponse deleteAvatar(Long id, Long userId);
}

