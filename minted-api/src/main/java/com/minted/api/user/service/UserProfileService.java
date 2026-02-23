package com.minted.api.user.service;

import com.minted.api.user.dto.UserProfileUpdateRequest;
import com.minted.api.user.dto.UserResponse;

public interface UserProfileService {

    UserResponse getProfile(String username);

    UserResponse updateProfile(String username, UserProfileUpdateRequest request);
}
