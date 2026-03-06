package com.minted.api.user.repository;

import com.minted.api.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired TestEntityManager em;
    @Autowired UserRepository userRepository;

    @Test
    void findByUsername_found() {
        em.persist(buildUser("alice"));
        em.flush();

        Optional<User> result = userRepository.findByUsername("alice");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
    }

    @Test
    void findByUsername_notFound() {
        Optional<User> result = userRepository.findByUsername("ghost");
        assertThat(result).isEmpty();
    }

    @Test
    void existsByUsername_existing_true() {
        em.persist(buildUser("bob"));
        em.flush();

        assertThat(userRepository.existsByUsername("bob")).isTrue();
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }

    @Test
    void save_persistsAllFields() {
        User u = buildUser("carol");
        u.setEmail("carol@example.com");
        u.setDisplayName("Carol");
        u.setCurrency("USD");

        User saved = userRepository.save(u);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("carol@example.com");
        assertThat(saved.getCurrency()).isEqualTo("USD");
    }

    private User buildUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("hashed");
        u.setIsActive(true);
        u.setForcePasswordChange(false);
        u.setRole("USER");
        return u;
    }
}
