package com.minted.api.friend.repository;

import com.minted.api.friend.entity.Friend;
import com.minted.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FriendRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired FriendRepository friendRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = em.persist(buildUser("alice"));
        user2 = em.persist(buildUser("bob"));
        em.flush();
    }

    @Test
    void findByUserIdAndIsActiveTrue_returnsOnlyActiveFriends() {
        em.persist(buildFriend("Charlie", user1, true));
        em.persist(buildFriend("Dave", user1, false));
        em.persist(buildFriend("Eve", user2, true));
        em.flush();

        List<Friend> result = friendRepository.findByUserIdAndIsActiveTrue(user1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Charlie");
    }

    @Test
    void findByIdAndUserId_ownerFound() {
        Friend friend = em.persist(buildFriend("Charlie", user1, true));
        em.flush();

        Optional<Friend> result = friendRepository.findByIdAndUserId(friend.getId(), user1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Charlie");
    }

    @Test
    void findByIdAndUserId_wrongUser_returnsEmpty() {
        Friend friend = em.persist(buildFriend("Charlie", user1, true));
        em.flush();

        Optional<Friend> result = friendRepository.findByIdAndUserId(friend.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void existsByNameAndUserIdAndIsActiveTrue_exists_returnsTrue() {
        em.persist(buildFriend("Charlie", user1, true));
        em.flush();

        boolean exists = friendRepository.existsByNameAndUserIdAndIsActiveTrue("Charlie", user1.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameAndUserIdAndIsActiveTrue_inactive_returnsFalse() {
        em.persist(buildFriend("Charlie", user1, false));
        em.flush();

        boolean exists = friendRepository.existsByNameAndUserIdAndIsActiveTrue("Charlie", user1.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void findByNameAndUserIdAndIsActiveFalse_findsInactiveFriend() {
        em.persist(buildFriend("Charlie", user1, false));
        em.flush();

        Optional<Friend> result = friendRepository.findByNameAndUserIdAndIsActiveFalse("Charlie", user1.getId());

        assertThat(result).isPresent();
    }

    @Test
    void findByNameAndUserIdAndIsActiveFalse_activeFriend_returnsEmpty() {
        em.persist(buildFriend("Charlie", user1, true));
        em.flush();

        Optional<Friend> result = friendRepository.findByNameAndUserIdAndIsActiveFalse("Charlie", user1.getId());

        assertThat(result).isEmpty();
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

    private Friend buildFriend(String name, User user, boolean active) {
        Friend f = new Friend();
        f.setName(name);
        f.setUser(user);
        f.setIsActive(active);
        f.setAvatarColor("#6366f1");
        return f;
    }
}
