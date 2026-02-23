package com.minted.api.controller;

import com.minted.api.dto.NotificationResponse;
import com.minted.api.entity.User;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getNotifications(userId, pageable);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notifications
        ));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        Long userId = getUserId(authentication);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", count
        ));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        NotificationResponse notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", notification
        ));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        Long userId = getUserId(authentication);
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", count + " notifications marked as read"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> dismiss(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        notificationService.dismiss(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/read")
    public ResponseEntity<Map<String, Object>> dismissAllRead(Authentication authentication) {
        Long userId = getUserId(authentication);
        int count = notificationService.dismissAllRead(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", count + " read notifications cleared"
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
