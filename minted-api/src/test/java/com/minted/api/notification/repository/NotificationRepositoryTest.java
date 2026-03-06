package com.minted.api.notification.repository;

import com.minted.api.notification.entity.Notification;
import com.minted.api.notification.enums.NotificationType;
import com.minted.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired NotificationRepository notificationRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        em.flush();
    }

    @Test
    void countByUserIdAndIsReadFalse_countsUnread() {
        em.persist(buildNotification(user1, "Msg1", false));
        em.persist(buildNotification(user1, "Msg2", false));
        em.persist(buildNotification(user1, "Msg3", true));
        em.persist(buildNotification(user2, "OtherMsg", false));
        em.flush();

        long count = notificationRepository.countByUserIdAndIsReadFalse(user1.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void findByIdAndUserId_correctUser_found() {
        Notification n = em.persist(buildNotification(user1, "Hello", false));
        em.flush();

        Optional<Notification> result = notificationRepository.findByIdAndUserId(n.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo("Hello");
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {
        Notification n = em.persist(buildNotification(user1, "Hello", false));
        em.flush();

        Optional<Notification> result = notificationRepository.findByIdAndUserId(n.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_paginates() {
        for (int i = 0; i < 5; i++) {
            em.persist(buildNotification(user1, "Msg" + i, false));
        }
        em.flush();

        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                user1.getId(), PageRequest.of(0, 3));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    void markAllAsReadByUserId_marksAllUnread() {
        em.persist(buildNotification(user1, "A", false));
        em.persist(buildNotification(user1, "B", false));
        em.persist(buildNotification(user1, "C", true));
        em.flush();

        int updated = notificationRepository.markAllAsReadByUserId(user1.getId());

        assertThat(updated).isEqualTo(2);
        assertThat(notificationRepository.countByUserIdAndIsReadFalse(user1.getId())).isEqualTo(0);
    }

    @Test
    void deleteByIdAndUserId_deletesCorrectRecord() {
        Notification n = em.persist(buildNotification(user1, "To Delete", false));
        em.flush();

        int deleted = notificationRepository.deleteByIdAndUserId(n.getId(), user1.getId());
        em.flush();
        em.clear();

        assertThat(deleted).isEqualTo(1);
        assertThat(notificationRepository.findById(n.getId())).isEmpty();
    }

    @Test
    void deleteByIdAndUserId_wrongUser_deletesNothing() {
        Notification n = em.persist(buildNotification(user1, "Keep", false));
        em.flush();

        int deleted = notificationRepository.deleteByIdAndUserId(n.getId(), user2.getId());

        assertThat(deleted).isEqualTo(0);
        assertThat(notificationRepository.findById(n.getId())).isPresent();
    }

    @Test
    void deleteAllReadByUserId_deletesOnlyReadNotifications() {
        em.persist(buildNotification(user1, "Read1", true));
        em.persist(buildNotification(user1, "Read2", true));
        Notification unread = em.persist(buildNotification(user1, "Unread", false));
        em.flush();

        int deleted = notificationRepository.deleteAllReadByUserId(user1.getId());

        assertThat(deleted).isEqualTo(2);
        assertThat(notificationRepository.findById(unread.getId())).isPresent();
    }

    // helpers

    private User buildUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("hashed");
        u.setIsActive(true);
        u.setForcePasswordChange(false);
        u.setRole("USER");
        return u;
    }

    private Notification buildNotification(User user, String message, boolean isRead) {
        Notification n = new Notification();
        n.setUser(user);
        n.setTitle("Test Title");
        n.setMessage(message);
        n.setType(NotificationType.INFO);
        n.setIsRead(isRead);
        return n;
    }
}
