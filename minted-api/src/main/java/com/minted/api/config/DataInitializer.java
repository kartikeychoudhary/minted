package com.minted.api.config;

import com.minted.api.entity.User;
import com.minted.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

    @Override
    public void run(ApplicationArguments args) {
        createDefaultAdminUser();
    }

    /**
     * Creates default admin user if it doesn't exist.
     * Username: admin
     * Password: admin
     * Force password change: true
     */
    private void createDefaultAdminUser() {
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

            userRepository.save(admin);

            log.info("Default admin user created successfully. Username: admin, Password: admin");
            log.warn("SECURITY WARNING: Please change the default admin password on first login!");
        } else {
            log.debug("Admin user already exists, skipping creation.");
        }
    }
}
