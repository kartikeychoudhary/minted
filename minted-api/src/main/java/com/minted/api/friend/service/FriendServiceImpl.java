package com.minted.api.friend.service;

import com.minted.api.common.exception.DuplicateResourceException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.friend.dto.FriendRequest;
import com.minted.api.friend.dto.FriendResponse;
import com.minted.api.friend.entity.Friend;
import com.minted.api.friend.repository.FriendRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponse> getAllByUserId(Long userId) {
        return friendRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(FriendResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FriendResponse getById(Long id, Long userId) {
        Friend friend = findFriendByIdAndUserId(id, userId);
        return FriendResponse.from(friend);
    }

    @Override
    @Transactional
    public FriendResponse create(FriendRequest request, Long userId) {
        // Check if a soft-deleted friend with same name exists — restore it
        Optional<Friend> softDeleted = friendRepository.findByNameAndUserIdAndIsActiveFalse(request.name(), userId);
        if (softDeleted.isPresent()) {
            Friend friend = softDeleted.get();
            friend.setEmail(request.email());
            friend.setPhone(request.phone());
            friend.setAvatarColor(request.avatarColor() != null ? request.avatarColor() : "#6366f1");
            friend.setIsActive(true);
            Friend restored = friendRepository.save(friend);
            log.info("Friend restored from soft-delete: id={}, name={}", restored.getId(), restored.getName());
            return FriendResponse.from(restored);
        }

        // Check if active friend with same name exists
        if (friendRepository.existsByNameAndUserIdAndIsActiveTrue(request.name(), userId)) {
            throw new DuplicateResourceException("Friend with name '" + request.name() + "' already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Friend friend = new Friend();
        friend.setUser(user);
        friend.setName(request.name());
        friend.setEmail(request.email());
        friend.setPhone(request.phone());
        friend.setAvatarColor(request.avatarColor() != null ? request.avatarColor() : "#6366f1");
        friend.setIsActive(true);

        Friend saved = friendRepository.save(friend);
        log.info("Friend created: id={}, name={}", saved.getId(), saved.getName());
        return FriendResponse.from(saved);
    }

    @Override
    @Transactional
    public FriendResponse update(Long id, FriendRequest request, Long userId) {
        Friend friend = findFriendByIdAndUserId(id, userId);

        // Check if name is changing and if new name already exists
        if (!friend.getName().equals(request.name()) &&
                friendRepository.existsByNameAndUserIdAndIsActiveTrue(request.name(), userId)) {
            throw new DuplicateResourceException("Friend with name '" + request.name() + "' already exists");
        }

        friend.setName(request.name());
        friend.setEmail(request.email());
        friend.setPhone(request.phone());
        if (request.avatarColor() != null) {
            friend.setAvatarColor(request.avatarColor());
        }

        Friend updated = friendRepository.save(friend);
        log.info("Friend updated: id={}", updated.getId());
        return FriendResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Friend friend = findFriendByIdAndUserId(id, userId);
        friend.setIsActive(false);
        friendRepository.save(friend);
        log.info("Friend soft-deleted: id={}", id);
    }

    private Friend findFriendByIdAndUserId(Long id, Long userId) {
        return friendRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found with id: " + id));
    }
}
