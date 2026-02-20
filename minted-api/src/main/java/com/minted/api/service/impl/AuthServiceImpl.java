package com.minted.api.service.impl;

import com.minted.api.dto.*;
import com.minted.api.entity.*;
import com.minted.api.enums.TransactionType;
import com.minted.api.exception.BadRequestException;
import com.minted.api.exception.UnauthorizedException;
import com.minted.api.repository.*;
import com.minted.api.service.AuthService;
import com.minted.api.service.SystemSettingService;
import com.minted.api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SystemSettingService systemSettingService;

    @Autowired
    private DefaultCategoryRepository defaultCategoryRepository;

    @Autowired
    private DefaultAccountTypeRepository defaultAccountTypeRepository;

    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;

    @Autowired
    private AccountTypeRepository accountTypeRepository;

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

    @Override
    @Transactional
    public LoginResponse signup(SignupRequest request) {
        if (!systemSettingService.isSignupEnabled()) {
            throw new BadRequestException("Public registration is currently disabled");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already taken");
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (!PASSWORD_PATTERN.matcher(request.password()).matches()) {
            throw new BadRequestException(
                    "Password must be at least 8 characters long and contain at least one uppercase letter and one number"
            );
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setEmail(request.email());
        user.setForcePasswordChange(false);
        user.setIsActive(true);
        user.setRole("USER");

        User savedUser = userRepository.save(user);
        seedDefaultDataForUser(savedUser);

        // Auto-login: generate tokens and return
        String token = jwtUtil.generateToken(savedUser.getUsername());
        String refreshToken = jwtUtil.generateToken(savedUser.getUsername());

        UserResponse userResponse = new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getDisplayName(),
                savedUser.getEmail(),
                savedUser.getForcePasswordChange(),
                savedUser.getCurrency(),
                savedUser.getRole()
        );

        return new LoginResponse(token, refreshToken, "Bearer", jwtExpiration, userResponse);
    }

    @Override
    public boolean isSignupEnabled() {
        return systemSettingService.isSignupEnabled();
    }

    private void seedDefaultDataForUser(User user) {
        List<DefaultAccountType> defaultTypes = defaultAccountTypeRepository.findAll();
        for (DefaultAccountType type : defaultTypes) {
            AccountType accountType = new AccountType();
            accountType.setName(type.getName());
            accountType.setDescription(type.getName() + " Account");
            accountType.setIcon(getDefaultIconForAccountType(type.getName()));
            accountType.setUser(user);
            accountType.setIsActive(true);
            accountType.setIsDefault(true);
            accountTypeRepository.save(accountType);
        }

        List<DefaultCategory> defaultCategories = defaultCategoryRepository.findAll();
        for (DefaultCategory defCat : defaultCategories) {
            TransactionCategory category = new TransactionCategory();
            category.setName(defCat.getName());
            category.setType(TransactionType.valueOf(defCat.getType().toUpperCase()));
            category.setIcon(defCat.getIcon());
            category.setColor(getDefaultColorForCategory(defCat.getName()));
            category.setUser(user);
            category.setIsActive(true);
            category.setIsDefault(true);
            transactionCategoryRepository.save(category);
        }
    }

    private String getDefaultIconForAccountType(String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("bank")) return "bank";
        if (lowerName.contains("card")) return "credit-card";
        if (lowerName.contains("wallet")) return "wallet";
        if (lowerName.contains("invest")) return "chart";
        return "bank";
    }

    private String getDefaultColorForCategory(String name) {
        return switch (name) {
            case "Salary" -> "#4CAF50";
            case "Freelance" -> "#8BC34A";
            case "Interest" -> "#CDDC39";
            case "Food & Dining" -> "#FF5722";
            case "Groceries" -> "#FF9800";
            case "Transport" -> "#2196F3";
            case "Utilities" -> "#FFC107";
            case "Entertainment" -> "#9C27B0";
            case "Shopping" -> "#E91E63";
            case "Health" -> "#00BCD4";
            case "Education" -> "#3F51B5";
            case "Rent" -> "#795548";
            case "EMI" -> "#607D8B";
            case "Transfer" -> "#9E9E9E";
            default -> "#607D8B";
        };
    }
}
