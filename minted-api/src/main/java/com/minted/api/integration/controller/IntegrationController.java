package com.minted.api.integration.controller;

import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.integration.dto.*;
import com.minted.api.integration.service.IntegrationService;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;
    private final UserRepository userRepository;

    // ── Status ────────────────────────────────────────────────────────────────

    @GetMapping("/splitwise/status")
    public ResponseEntity<Map<String, Object>> getSplitwiseStatus(Authentication auth) {
        Long userId = getUserId(auth);
        IntegrationStatusResponse status = integrationService.getSplitwiseStatus(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", status));
    }

    @GetMapping("/splitwise/admin-config")
    public ResponseEntity<Map<String, Object>> getSplitwiseAdminConfig() {
        SplitwiseAdminConfigResponse config = integrationService.getSplitwiseAdminConfig();
        return ResponseEntity.ok(Map.of("success", true, "data", config));
    }

    // ── OAuth ─────────────────────────────────────────────────────────────────

    @GetMapping("/splitwise/auth-url")
    public ResponseEntity<Map<String, Object>> getAuthUrl(Authentication auth) {
        Long userId = getUserId(auth);
        SplitwiseAuthUrlResponse response = integrationService.getSplitwiseAuthUrl(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    @PostMapping("/splitwise/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(
            @RequestBody SplitwiseCallbackRequest request,
            Authentication auth) {
        Long userId = getUserId(auth);
        IntegrationStatusResponse status = integrationService.connectSplitwise(request.code(), userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", status,
                "message", "Splitwise account connected successfully! Welcome, " + status.connectedUserName() + "."
        ));
    }

    @DeleteMapping("/splitwise/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect(Authentication auth) {
        Long userId = getUserId(auth);
        integrationService.disconnectSplitwise(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Splitwise account has been disconnected."
        ));
    }

    // ── Friends ───────────────────────────────────────────────────────────────

    @GetMapping("/splitwise/friends")
    public ResponseEntity<Map<String, Object>> getSplitwiseFriends(Authentication auth) {
        Long userId = getUserId(auth);
        List<SplitwiseFriendDto> friends = integrationService.getSplitwiseFriends(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", friends));
    }

    @GetMapping("/splitwise/linked-friends")
    public ResponseEntity<Map<String, Object>> getLinkedFriends(Authentication auth) {
        Long userId = getUserId(auth);
        List<FriendLinkResponse> links = integrationService.getLinkedFriends(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", links));
    }

    @PostMapping("/splitwise/link-friend")
    public ResponseEntity<Map<String, Object>> linkFriend(
            @RequestBody LinkFriendRequest request,
            Authentication auth) {
        Long userId = getUserId(auth);
        FriendLinkResponse link = integrationService.linkFriend(
                request.friendId(), request.splitwiseFriendId(), userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", link,
                "message", "Friend linked successfully to " + link.splitwiseFriendName() + " on Splitwise."
        ));
    }

    @DeleteMapping("/splitwise/link-friend/{friendId}")
    public ResponseEntity<Map<String, Object>> unlinkFriend(
            @PathVariable Long friendId,
            Authentication auth) {
        Long userId = getUserId(auth);
        integrationService.unlinkFriend(friendId, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Friend unlinked from Splitwise."
        ));
    }

    // ── Push ──────────────────────────────────────────────────────────────────

    @PostMapping("/splitwise/push-split/{splitId}")
    public ResponseEntity<Map<String, Object>> pushSplit(
            @PathVariable Long splitId,
            @RequestBody(required = false) PushSingleRequest request,
            Authentication auth) {
        Long userId = getUserId(auth);
        boolean forcePush = request != null && request.forcePush();
        PushResult result = integrationService.pushSplitToSplitwise(splitId, forcePush, userId);

        if (result.success()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result,
                    "message", "Split \"" + result.description() + "\" pushed to Splitwise successfully."
            ));
        } else if (result.alreadyPushed()) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "data", result,
                    "message", "This split has already been pushed to Splitwise. Use forcePush=true to push again."
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "data", result,
                    "message", result.errorMessage()
            ));
        }
    }

    @PostMapping("/splitwise/push-splits")
    public ResponseEntity<Map<String, Object>> bulkPushSplits(
            @RequestBody BulkPushRequest request,
            Authentication auth) {
        Long userId = getUserId(auth);
        BulkPushResponse response = integrationService.bulkPushToSplitwise(
                request.splitTransactionIds(), request.forcePush(), userId);

        String summary = String.format(
                "%d of %d splits pushed to Splitwise successfully.%s",
                response.successCount(),
                response.totalRequested(),
                response.failedCount() > 0 ? " " + response.failedCount() + " failed." : ""
        );

        return ResponseEntity.ok(Map.of(
                "success", response.failedCount() == 0,
                "data", response,
                "message", summary
        ));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
