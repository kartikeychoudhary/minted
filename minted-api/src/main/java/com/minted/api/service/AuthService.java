package com.minted.api.service;

import com.minted.api.dto.ChangePasswordRequest;
import com.minted.api.dto.LoginRequest;
import com.minted.api.dto.LoginResponse;
import com.minted.api.dto.RefreshTokenRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);

    void changePassword(String username, ChangePasswordRequest request);
}
