package com.minted.api.notification.service;

import com.minted.api.notification.entity.Notification;
import com.minted.api.notification.enums.NotificationType;
import com.minted.api.notification.repository.NotificationRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationHelperTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private NotificationHelper notificationHelper;

    // ── notify ────────────────────────────────────────────────────────────────

    @Test
    void notify_userNotFound_doesNotSaveNotification() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        notificationHelper.notify(99L, NotificationType.INFO, "Title", "Body");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void notify_userFound_savesNotification() {
        User user = buildUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        notificationHelper.notify(1L, NotificationType.SUCCESS, "Welcome", "Hello there");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getType()).isEqualTo(NotificationType.SUCCESS);
        assertThat(saved.getTitle()).isEqualTo("Welcome");
        assertThat(saved.getMessage()).isEqualTo("Hello there");
    }

    @Test
    void notify_repositoryThrows_exceptionSwallowed() {
        User user = buildUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        // Should NOT throw — exceptions are caught internally
        notificationHelper.notify(1L, NotificationType.ERROR, "Title", "Body");
    }

    // ── notifyAll ─────────────────────────────────────────────────────────────

    @Test
    void notifyAll_broadcastsToAllUsers() {
        User u1 = buildUser(1L);
        User u2 = buildUser(2L);
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        notificationHelper.notifyAll(NotificationType.SYSTEM, "Maintenance", "System going down");

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void notifyAll_noUsers_savesNothing() {
        when(userRepository.findAll()).thenReturn(List.of());

        notificationHelper.notifyAll(NotificationType.INFO, "Title", "Msg");

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void notifyAll_repositoryThrows_exceptionSwallowed() {
        User u = buildUser(1L);
        when(userRepository.findAll()).thenReturn(List.of(u));
        when(notificationRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        // Should NOT throw
        notificationHelper.notifyAll(NotificationType.SYSTEM, "Title", "Body");
    }

    @Test
    void notifyAll_setsCorrectFieldsOnEachNotification() {
        User u1 = buildUser(1L);
        User u2 = buildUser(2L);
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        notificationHelper.notifyAll(NotificationType.WARNING, "Heads Up", "Read this");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> saved = captor.getAllValues();
        assertThat(saved).allSatisfy(n -> {
            assertThat(n.getType()).isEqualTo(NotificationType.WARNING);
            assertThat(n.getTitle()).isEqualTo("Heads Up");
            assertThat(n.getMessage()).isEqualTo("Read this");
        });
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user-" + id);
        return u;
    }
}
