package com.minted.api.user.service;

import com.minted.api.user.dto.UserProfileUpdateRequest;
import com.minted.api.user.dto.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileService {
    UserResponse getProfile(String username);
    UserResponse updateProfile(String username, UserProfileUpdateRequest request);
    UserResponse uploadAvatar(String username, MultipartFile file);
    UserResponse deleteAvatar(String username);
}
