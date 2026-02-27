package com.minted.api.friend.controller;

import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.friend.dto.FriendRequest;
import com.minted.api.friend.dto.FriendResponse;
import com.minted.api.friend.service.FriendService;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFriends(
            Authentication authentication,
            @RequestParam(defaultValue = "true") boolean includeAvatar
    ) {
        Long userId = getUserId(authentication);
        List<FriendResponse> friends = friendService.getAllByUserId(userId, includeAvatar);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", friends
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getFriendById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        FriendResponse friend = friendService.getById(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", friend
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFriend(
            @Valid @RequestBody FriendRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        FriendResponse friend = friendService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", friend,
                "message", "Friend added successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateFriend(
            @PathVariable Long id,
            @Valid @RequestBody FriendRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        FriendResponse friend = friendService.update(id, request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", friend,
                "message", "Friend updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteFriend(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        friendService.delete(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Friend removed successfully"
        ));
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadFriendAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        FriendResponse friend = friendService.uploadAvatar(id, userId, file);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", friend,
                "message", "Friend avatar uploaded successfully"
        ));
    }

    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<Map<String, Object>> deleteFriendAvatar(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        FriendResponse friend = friendService.deleteAvatar(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", friend,
                "message", "Friend avatar removed successfully"
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
