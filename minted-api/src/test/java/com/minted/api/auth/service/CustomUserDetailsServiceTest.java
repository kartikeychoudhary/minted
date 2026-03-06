package com.minted.api.auth.service;

import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_activeUser_returnsUserDetails() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(buildUser(true, "USER")));

        UserDetails details = service.loadUserByUsername("alice");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    void loadUserByUsername_inactiveUser_throwsUsernameNotFound() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(buildUser(false, "USER")));

        assertThatThrownBy(() -> service.loadUserByUsername("alice"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_adminRole_hasAdminAuthority() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(buildUser(true, "ADMIN")));

        UserDetails details = service.loadUserByUsername("admin");

        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    void getUserByUsername_found_returnsUser() {
        User user = buildUser(true, "USER");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        User result = service.getUserByUsername("alice");

        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void getUserByUsername_notFound_throwsUsernameNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private User buildUser(boolean active, String role) {
        User u = new User();
        u.setId(1L);
        u.setUsername("alice");
        u.setPassword("hashed");
        u.setIsActive(active);
        u.setRole(role);
        return u;
    }
}
