package com.minted.api.admin.service;

import com.minted.api.admin.dto.AdminUserResponse;
import com.minted.api.admin.dto.CreateUserRequest;
import com.minted.api.admin.dto.ResetPasswordRequest;

import java.util.List;

public interface UserManagementService {
    List<AdminUserResponse> getAllUsers();
    AdminUserResponse getUserById(Long id);
    AdminUserResponse createUser(CreateUserRequest request);
    AdminUserResponse toggleUserActive(Long userId, String currentUsername);
    void deleteUser(Long userId, String currentUsername);
    void resetPassword(Long userId, ResetPasswordRequest request);
}
