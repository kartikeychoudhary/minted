package com.minted.api.service.impl;

import com.minted.api.dto.*;
import com.minted.api.entity.User;
import com.minted.api.exception.BadRequestException;
import com.minted.api.exception.UnauthorizedException;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.AuthService;
import com.minted.api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.jwt.expiration}")
    private Long jwtExpiration;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[0-9]).{8,}$"
    );

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("User account is not active");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        String refreshToken = jwtUtil.generateToken(user.getUsername()); // For simplicity, using same token

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getForcePasswordChange(),
                user.getCurrency(),
                user.getRole()
        );

        return new LoginResponse(
                token,
                refreshToken,
                "Bearer",
                jwtExpiration,
                userResponse
        );
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        try {
            String username = jwtUtil.extractUsername(request.refreshToken());

            if (!jwtUtil.validateToken(request.refreshToken())) {
                throw new UnauthorizedException("Invalid or expired refresh token");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            if (!user.getIsActive()) {
                throw new UnauthorizedException("User account is not active");
            }

            String newToken = jwtUtil.generateToken(user.getUsername());
            String newRefreshToken = jwtUtil.generateToken(user.getUsername());

            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getDisplayName(),
                    user.getEmail(),
                    user.getForcePasswordChange(),
                    user.getCurrency() != null ? user.getCurrency() : "USD",
                    user.getRole()
            );

            return new LoginResponse(
                    newToken,
                    newRefreshToken,
                    "Bearer",
                    jwtExpiration,
                    userResponse
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Validate current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Check new passwords match
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Check new password is different from current
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from current password");
        }

        // Validate new password strength
        if (!PASSWORD_PATTERN.matcher(request.newPassword()).matches()) {
            throw new BadRequestException(
                    "Password must be at least 8 characters long and contain at least one uppercase letter and one number"
            );
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setForcePasswordChange(false);
        userRepository.save(user);
    }
}
