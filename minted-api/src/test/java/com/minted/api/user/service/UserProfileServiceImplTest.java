package com.minted.api.user.service;

import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.dto.UserProfileUpdateRequest;
import com.minted.api.user.dto.UserResponse;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserProfileServiceImpl userProfileService;

    @Test
    void getProfile_found_returnsResponse() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(buildUser("alice")));

        UserResponse response = userProfileService.getProfile("alice");

        assertThat(response.username()).isEqualTo("alice");
    }

    @Test
    void getProfile_notFound_throwsResourceNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getProfile("ghost"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfile_updatesFields() {
        User user = buildUser("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userProfileService.updateProfile("alice",
                new UserProfileUpdateRequest("Alice Updated", "new@email.com", "USD"));

        assertThat(user.getDisplayName()).isEqualTo("Alice Updated");
        assertThat(user.getEmail()).isEqualTo("new@email.com");
        assertThat(user.getCurrency()).isEqualTo("USD");
    }

    @Test
    void updateProfile_nullFields_doesNotOverwrite() {
        User user = buildUser("alice");
        user.setDisplayName("Existing");
        user.setEmail("old@email.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userProfileService.updateProfile("alice", new UserProfileUpdateRequest(null, null, null));

        assertThat(user.getDisplayName()).isEqualTo("Existing");
        assertThat(user.getEmail()).isEqualTo("old@email.com");
    }

    @Test
    void uploadAvatar_validImage_saves() throws Exception {
        User user = buildUser("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile("avatar", "test.png", "image/png", new byte[100]);
        userProfileService.uploadAvatar("alice", file);

        assertThat(user.getAvatarData()).isNotNull();
        assertThat(user.getAvatarContentType()).isEqualTo("image/png");
    }

    @Test
    void uploadAvatar_tooLarge_throwsIllegalArgument() {
        User user = buildUser("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        byte[] bigData = new byte[3 * 1024 * 1024]; // 3MB
        MockMultipartFile file = new MockMultipartFile("avatar", "big.png", "image/png", bigData);

        assertThatThrownBy(() -> userProfileService.uploadAvatar("alice", file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2MB");
    }

    @Test
    void uploadAvatar_nonImage_throwsIllegalArgument() {
        User user = buildUser("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[100]);

        assertThatThrownBy(() -> userProfileService.uploadAvatar("alice", file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("image");
    }

    @Test
    void deleteAvatar_clearsAvatarFields() {
        User user = buildUser("alice");
        user.setAvatarData(new byte[]{1, 2, 3});
        user.setAvatarContentType("image/png");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userProfileService.deleteAvatar("alice");

        assertThat(user.getAvatarData()).isNull();
        assertThat(user.getAvatarContentType()).isNull();
    }

    private User buildUser(String username) {
        User u = new User();
        u.setId(1L);
        u.setUsername(username);
        u.setPassword("hashed");
        u.setIsActive(true);
        u.setForcePasswordChange(false);
        u.setRole("USER");
        u.setCurrency("INR");
        return u;
    }
}
