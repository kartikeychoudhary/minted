package com.minted.api.service;

import com.minted.api.dto.ChangePasswordRequest;
import com.minted.api.dto.LoginRequest;
import com.minted.api.dto.LoginResponse;
import com.minted.api.dto.RefreshTokenRequest;
import com.minted.api.dto.SignupRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    LoginResponse signup(SignupRequest request);

    boolean isSignupEnabled();
}
