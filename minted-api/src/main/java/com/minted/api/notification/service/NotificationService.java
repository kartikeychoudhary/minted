package com.minted.api.notification.service;

import com.minted.api.notification.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<NotificationResponse> getNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    NotificationResponse markAsRead(Long id, Long userId);

    int markAllAsRead(Long userId);

    void dismiss(Long id, Long userId);

    int dismissAllRead(Long userId);
}
