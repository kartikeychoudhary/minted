package com.minted.api.service;

import com.minted.api.dto.AdminUserResponse;
import com.minted.api.dto.CreateUserRequest;
import com.minted.api.dto.ResetPasswordRequest;

import java.util.List;

public interface UserManagementService {
    List<AdminUserResponse> getAllUsers();
    AdminUserResponse getUserById(Long id);
    AdminUserResponse createUser(CreateUserRequest request);
    AdminUserResponse toggleUserActive(Long userId, String currentUsername);
    void deleteUser(Long userId, String currentUsername);
    void resetPassword(Long userId, ResetPasswordRequest request);
}
