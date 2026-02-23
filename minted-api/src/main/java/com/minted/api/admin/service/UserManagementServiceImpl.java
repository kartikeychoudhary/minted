package com.minted.api.admin.service;

import com.minted.api.admin.dto.AdminUserResponse;
import com.minted.api.admin.dto.CreateUserRequest;
import com.minted.api.admin.dto.ResetPasswordRequest;
import com.minted.api.user.entity.User;
import com.minted.api.account.entity.Account;
import com.minted.api.account.entity.AccountType;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.admin.entity.DefaultCategory;
import com.minted.api.admin.entity.DefaultAccountType;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.admin.repository.DefaultCategoryRepository;
import com.minted.api.admin.repository.DefaultAccountTypeRepository;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.account.repository.AccountTypeRepository;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.budget.repository.BudgetRepository;
import com.minted.api.recurring.repository.RecurringTransactionRepository;
import com.minted.api.bulkimport.repository.BulkImportRepository;
import com.minted.api.dashboard.repository.DashboardCardRepository;
import com.minted.api.notification.enums.NotificationType;
import com.minted.api.notification.service.NotificationHelper;
import com.minted.api.admin.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultCategoryRepository defaultCategoryRepository;
    private final DefaultAccountTypeRepository defaultAccountTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final BulkImportRepository bulkImportRepository;
    private final DashboardCardRepository dashboardCardRepository;
    private final NotificationHelper notificationHelper;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[0-9]).{8,}$"
    );

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AdminUserResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return AdminUserResponse.from(user);
    }

    @Override
    @Transactional
    public AdminUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already taken");
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
        user.setForcePasswordChange(true);
        user.setIsActive(true);
        user.setRole(request.role() != null ? request.role().toUpperCase() : "USER");

        User savedUser = userRepository.save(user);
        seedDefaultDataForUser(savedUser);
        notificationHelper.notify(savedUser.getId(), NotificationType.SUCCESS,
                "Welcome to Minted!", "Your account has been created. Start managing your finances today.");

        log.info("Admin created new user: {}", savedUser.getUsername());
        return AdminUserResponse.from(savedUser);
    }

    @Override
    @Transactional
    public AdminUserResponse toggleUserActive(Long userId, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getUsername().equals(currentUsername)) {
            throw new BadRequestException("Cannot disable your own account");
        }

        user.setIsActive(!user.getIsActive());
        User saved = userRepository.save(user);

        log.info("User {} {} by admin", saved.getUsername(), saved.getIsActive() ? "enabled" : "disabled");
        return AdminUserResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getUsername().equals(currentUsername)) {
            throw new BadRequestException("Cannot delete your own account");
        }

        // Delete all user-related data in correct order (children first)
        transactionRepository.deleteAll(transactionRepository.findByUserId(userId));
        recurringTransactionRepository.deleteAll(recurringTransactionRepository.findByUserId(userId));
        budgetRepository.deleteAll(budgetRepository.findByUserId(userId));
        accountRepository.deleteAll(accountRepository.findByUserId(userId));
        accountTypeRepository.deleteAll(accountTypeRepository.findByUserId(userId));
        transactionCategoryRepository.deleteAll(transactionCategoryRepository.findByUserId(userId));
        bulkImportRepository.deleteAll(bulkImportRepository.findByUserIdOrderByCreatedAtDesc(userId));
        dashboardCardRepository.deleteAll(dashboardCardRepository.findByUserIdOrderByPositionOrderAsc(userId));

        userRepository.delete(user);

        log.info("User {} deleted by admin", user.getUsername());
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!PASSWORD_PATTERN.matcher(request.newPassword()).matches()) {
            throw new BadRequestException(
                    "Password must be at least 8 characters long and contain at least one uppercase letter and one number"
            );
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setForcePasswordChange(true);
        userRepository.save(user);

        log.info("Password reset for user {} by admin", user.getUsername());
    }

    private void seedDefaultDataForUser(User user) {
        // Seed Account Types from defaults
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

        // Seed Transaction Categories from defaults
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
