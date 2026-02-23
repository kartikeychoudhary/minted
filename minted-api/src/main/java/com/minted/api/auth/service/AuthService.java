package com.minted.api.auth.service;

import com.minted.api.auth.dto.ChangePasswordRequest;
import com.minted.api.auth.dto.LoginRequest;
import com.minted.api.auth.dto.LoginResponse;
import com.minted.api.auth.dto.RefreshTokenRequest;
import com.minted.api.auth.dto.SignupRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(RefreshTokenRequest request);

    void changePassword(String username, ChangePasswordRequest request);

    LoginResponse signup(SignupRequest request);

    boolean isSignupEnabled();
}
