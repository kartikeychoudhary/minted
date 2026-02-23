package com.minted.api.service;

import com.minted.api.entity.Notification;
import com.minted.api.entity.User;
import com.minted.api.enums.NotificationType;
import com.minted.api.repository.NotificationRepository;
import com.minted.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shared notification creation utility.
 * Any backend service can inject this to fire notifications for users.
 * Runs in its own transaction so failures never roll back the caller.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationHelper {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Create a notification for a user.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notify(Long userId, NotificationType type, String title, String message) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("Cannot create notification: user {} not found", userId);
                return;
            }

            Notification notification = new Notification();
            notification.setUser(user);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);

            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to create notification for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Create a notification for all users (system-wide broadcast).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyAll(NotificationType type, String title, String message) {
        try {
            userRepository.findAll().forEach(user -> {
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setType(type);
                notification.setTitle(title);
                notification.setMessage(message);
                notificationRepository.save(notification);
            });
        } catch (Exception e) {
            log.error("Failed to create broadcast notification: {}", e.getMessage(), e);
        }
    }
}
