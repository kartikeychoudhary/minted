package com.minted.api.notification.service;

import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.notification.dto.NotificationResponse;
import com.minted.api.notification.entity.Notification;
import com.minted.api.notification.enums.NotificationType;
import com.minted.api.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    // ── getNotifications ──────────────────────────────────────────────────────

    @Test
    void getNotifications_returnsPagedResponses() {
        Notification n = buildNotification(1L, false);
        Pageable pageable = PageRequest.of(0, 10);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(n)));

        Page<NotificationResponse> result = notificationService.getNotifications(1L, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
    }

    // ── getUnreadCount ────────────────────────────────────────────────────────

    @Test
    void getUnreadCount_returnsCount() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertThat(count).isEqualTo(5L);
    }

    // ── markAsRead ────────────────────────────────────────────────────────────

    @Test
    void markAsRead_found_marksAndReturns() {
        Notification n = buildNotification(1L, false);
        when(notificationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(n)).thenReturn(n);

        NotificationResponse response = notificationService.markAsRead(1L, 1L);

        assertThat(n.getIsRead()).isTrue();
        assertThat(response.isRead()).isTrue();
    }

    @Test
    void markAsRead_notFound_throwsResourceNotFound() {
        when(notificationRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── markAllAsRead ─────────────────────────────────────────────────────────

    @Test
    void markAllAsRead_returnsCount() {
        when(notificationRepository.markAllAsReadByUserId(1L)).thenReturn(3);

        int count = notificationService.markAllAsRead(1L);

        assertThat(count).isEqualTo(3);
    }

    // ── dismiss ───────────────────────────────────────────────────────────────

    @Test
    void dismiss_found_deletes() {
        when(notificationRepository.deleteByIdAndUserId(1L, 1L)).thenReturn(1);

        notificationService.dismiss(1L, 1L);

        verify(notificationRepository).deleteByIdAndUserId(1L, 1L);
    }

    @Test
    void dismiss_notFound_throwsResourceNotFound() {
        when(notificationRepository.deleteByIdAndUserId(99L, 1L)).thenReturn(0);

        assertThatThrownBy(() -> notificationService.dismiss(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── dismissAllRead ────────────────────────────────────────────────────────

    @Test
    void dismissAllRead_returnsCount() {
        when(notificationRepository.deleteAllReadByUserId(1L)).thenReturn(4);

        int count = notificationService.dismissAllRead(1L);

        assertThat(count).isEqualTo(4);
    }

    // helpers

    private Notification buildNotification(Long id, boolean isRead) {
        Notification n = new Notification();
        n.setId(id);
        n.setTitle("Test Title");
        n.setMessage("Test Message");
        n.setType(NotificationType.INFO);
        n.setIsRead(isRead);
        return n;
    }
}
