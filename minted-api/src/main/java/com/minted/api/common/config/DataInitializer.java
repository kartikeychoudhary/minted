package com.minted.api.common.config;

import com.minted.api.account.entity.AccountType;
import com.minted.api.admin.entity.DefaultAccountType;
import com.minted.api.admin.entity.DefaultCategory;
import com.minted.api.transaction.entity.TransactionCategory;
import com.minted.api.user.entity.User;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.account.repository.AccountTypeRepository;
import com.minted.api.admin.repository.DefaultAccountTypeRepository;
import com.minted.api.admin.repository.DefaultCategoryRepository;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes default data on application startup.
 * Creates default admin user if it doesn't exist.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DefaultCategoryRepository defaultCategoryRepository;
    private final DefaultAccountTypeRepository defaultAccountTypeRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final AccountTypeRepository accountTypeRepository;

    @Override
    public void run(ApplicationArguments args) {
        User admin = createDefaultAdminUser();
        if (admin != null) {
            seedDefaultDataForAdmin(admin);
        }
    }

    /**
     * Creates default admin user if it doesn't exist.
     * Username: admin
     * Password: admin
     * Force password change: true
     */
    private User createDefaultAdminUser() {
        String adminUsername = "admin";

        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            log.info("Creating default admin user...");

            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@minted.local");
            admin.setDisplayName("System Administrator");
            admin.setForcePasswordChange(true);
            admin.setIsActive(true);
            admin.setRole("ADMIN");

            User savedAdmin = userRepository.save(admin);

            log.info("Default admin user created successfully. Username: admin, Password: admin");
            log.warn("SECURITY WARNING: Please change the default admin password on first login!");
            return savedAdmin;
        } else {
            log.debug("Admin user already exists, skipping creation.");
            return userRepository.findByUsername(adminUsername).get();
        }
    }

    private void seedDefaultDataForAdmin(User admin) {
        log.info("Checking default data for admin user...");

        // Seed Account Types
        List<AccountType> existingAccountTypes = accountTypeRepository.findByUserId(admin.getId());
        if (existingAccountTypes.isEmpty()) {
            log.info("Seeding default account types for admin user...");
            List<DefaultAccountType> defaultTypes = defaultAccountTypeRepository.findAll();
            for (DefaultAccountType type : defaultTypes) {
                AccountType accountType = new AccountType();
                accountType.setName(type.getName());
                accountType.setDescription(type.getName() + " Account");
                accountType.setIcon(getDefaultIconForAccountType(type.getName()));
                accountType.setUser(admin);
                accountType.setIsActive(true);
                accountType.setIsDefault(true);
                accountTypeRepository.save(accountType);
            }
        }

        // Seed Transaction Categories
        List<TransactionCategory> existingCategories = transactionCategoryRepository.findByUserId(admin.getId());
        if (existingCategories.isEmpty()) {
            log.info("Seeding default transaction categories for admin user...");
            List<DefaultCategory> defaultCategories = defaultCategoryRepository.findAll();
            for (DefaultCategory defCat : defaultCategories) {
                TransactionCategory category = new TransactionCategory();
                category.setName(defCat.getName());
                category.setType(TransactionType.valueOf(defCat.getType().toUpperCase()));
                category.setIcon(defCat.getIcon());
                category.setColor(getDefaultColorForCategory(defCat.getName()));
                category.setUser(admin);
                category.setIsActive(true);
                category.setIsDefault(true);
                transactionCategoryRepository.save(category);
            }
        }
    }

    private String getDefaultIconForAccountType(String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("bank")) return "bank";
        if (lowerName.contains("card")) return "credit-card";
        if (lowerName.contains("wallet")) return "wallet";
        if (lowerName.contains("invest")) return "chart";
        return "bank"; // fallback
    }

    private String getDefaultColorForCategory(String name) {
        // Fallback colors since default_categories table doesn't store colors, 
        // but transaction_categories requires it per frontend design.
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
