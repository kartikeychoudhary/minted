package com.minted.api.service;

import com.minted.api.dto.UserProfileUpdateRequest;
import com.minted.api.dto.UserResponse;

public interface UserProfileService {

    UserResponse getProfile(String username);

    UserResponse updateProfile(String username, UserProfileUpdateRequest request);
}
