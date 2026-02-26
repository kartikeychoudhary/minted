package com.minted.api.user.service;

import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.dto.UserProfileUpdateRequest;
import com.minted.api.user.dto.UserResponse;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024; // 2MB

    private final UserRepository userRepository;

    @Override
    public UserResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String username, UserProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.currency() != null) {
            user.setCurrency(request.currency());
        }

        userRepository.save(user);
        log.info("Profile updated for user: {}", username);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse uploadAvatar(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("Avatar file size must not exceed 2MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        try {
            user.setAvatarData(file.getBytes());
            user.setAvatarContentType(contentType);
            user.setAvatarFileSize((int) file.getSize());
            user.setAvatarUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            log.info("Avatar uploaded for user: {}", username);
            return toResponse(user);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read avatar file", e);
        }
    }

    @Override
    @Transactional
    public UserResponse deleteAvatar(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setAvatarData(null);
        user.setAvatarContentType(null);
        user.setAvatarFileSize(null);
        user.setAvatarUpdatedAt(null);
        userRepository.save(user);
        log.info("Avatar deleted for user: {}", username);
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        String avatarBase64 = null;
        if (user.getAvatarData() != null && user.getAvatarContentType() != null) {
            avatarBase64 = "data:" + user.getAvatarContentType() + ";base64,"
                    + Base64.getEncoder().encodeToString(user.getAvatarData());
        }
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail(),
                user.getForcePasswordChange(),
                user.getCurrency(),
                user.getRole(),
                avatarBase64
        );
    }
}
