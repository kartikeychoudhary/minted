package com.minted.api.friend.service;

import com.minted.api.common.exception.DuplicateResourceException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.friend.dto.FriendRequest;
import com.minted.api.friend.dto.FriendResponse;
import com.minted.api.friend.entity.Friend;
import com.minted.api.friend.repository.FriendRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceImplTest {

    @Mock FriendRepository friendRepository;
    @Mock UserRepository userRepository;

    @InjectMocks FriendServiceImpl friendService;

    private User user;
    private Friend friend;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("alice");

        friend = new Friend();
        friend.setId(10L);
        friend.setName("Bob");
        friend.setEmail("bob@example.com");
        friend.setPhone("555-1234");
        friend.setAvatarColor("#3b82f6");
        friend.setIsActive(true);
        friend.setUser(user);
    }

    // ── getAllByUserId ─────────────────────────────────────────────────────────

    @Test
    void getAllByUserId_returnsMappedResponses() {
        when(friendRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(List.of(friend));

        List<FriendResponse> result = friendService.getAllByUserId(1L, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Bob");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_foundReturnsResponse() {
        when(friendRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(friend));

        FriendResponse result = friendService.getById(10L, 1L);

        assertThat(result.name()).isEqualTo("Bob");
    }

    @Test
    void getById_notFound_throwsException() {
        when(friendRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.getById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_newFriend_savesPersistsAndReturns() {
        FriendRequest request = new FriendRequest("Charlie", "charlie@test.com", null, "#ff0000");

        when(friendRepository.findByNameAndUserIdAndIsActiveFalse("Charlie", 1L)).thenReturn(Optional.empty());
        when(friendRepository.existsByNameAndUserIdAndIsActiveTrue("Charlie", 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(friendRepository.save(any())).thenAnswer(inv -> {
            Friend f = inv.getArgument(0);
            f.setId(20L);
            return f;
        });

        FriendResponse result = friendService.create(request, 1L);

        assertThat(result.name()).isEqualTo("Charlie");
        verify(friendRepository).save(any(Friend.class));
    }

    @Test
    void create_duplicateName_throwsDuplicateException() {
        FriendRequest request = new FriendRequest("Bob", null, null, null);

        when(friendRepository.findByNameAndUserIdAndIsActiveFalse("Bob", 1L)).thenReturn(Optional.empty());
        when(friendRepository.existsByNameAndUserIdAndIsActiveTrue("Bob", 1L)).thenReturn(true);

        assertThatThrownBy(() -> friendService.create(request, 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_softDeletedFriend_restoresInsteadOfCreating() {
        FriendRequest request = new FriendRequest("Bob", "bob@test.com", null, null);
        Friend softDeleted = new Friend();
        softDeleted.setId(5L);
        softDeleted.setName("Bob");
        softDeleted.setIsActive(false);
        softDeleted.setUser(user);

        when(friendRepository.findByNameAndUserIdAndIsActiveFalse("Bob", 1L)).thenReturn(Optional.of(softDeleted));
        when(friendRepository.save(any())).thenReturn(softDeleted);

        FriendResponse result = friendService.create(request, 1L);

        assertThat(result.name()).isEqualTo("Bob");
        verify(friendRepository, never()).existsByNameAndUserIdAndIsActiveTrue(any(), any());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_changesFields() {
        FriendRequest request = new FriendRequest("Bobby", "bobby@test.com", "999", "#ff0000");

        when(friendRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(friend));
        when(friendRepository.existsByNameAndUserIdAndIsActiveTrue("Bobby", 1L)).thenReturn(false);
        when(friendRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FriendResponse result = friendService.update(10L, request, 1L);

        assertThat(result.name()).isEqualTo("Bobby");
    }

    // ── delete (soft) ─────────────────────────────────────────────────────────

    @Test
    void delete_setsInactiveAndSaves() {
        when(friendRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(friend));
        when(friendRepository.save(any())).thenReturn(friend);

        friendService.delete(10L, 1L);

        assertThat(friend.getIsActive()).isFalse();
        verify(friendRepository).save(friend);
    }

    // ── uploadAvatar ──────────────────────────────────────────────────────────

    @Test
    void uploadAvatar_validImage_setsAvatarData() throws Exception {
        byte[] imageBytes = new byte[]{1, 2, 3};
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", imageBytes);

        when(friendRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(friend));
        when(friendRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FriendResponse result = friendService.uploadAvatar(10L, 1L, file);

        verify(friendRepository).save(argThat(f -> f.getAvatarData() != null));
    }

    // ── deleteAvatar ──────────────────────────────────────────────────────────

    @Test
    void deleteAvatar_clearsAvatarFields() {
        friend.setAvatarData(new byte[]{1, 2, 3});
        friend.setAvatarContentType("image/png");

        when(friendRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(friend));
        when(friendRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        friendService.deleteAvatar(10L, 1L);

        verify(friendRepository).save(argThat(f -> f.getAvatarData() == null));
    }
}
