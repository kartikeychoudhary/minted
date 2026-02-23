package com.minted.api.service.impl;

import com.minted.api.dto.NotificationResponse;
import com.minted.api.entity.Notification;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.NotificationRepository;
import com.minted.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setIsRead(true);
        return NotificationResponse.from(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void dismiss(Long id, Long userId) {
        int deleted = notificationRepository.deleteByIdAndUserId(id, userId);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Notification not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public int dismissAllRead(Long userId) {
        return notificationRepository.deleteAllReadByUserId(userId);
    }
}
