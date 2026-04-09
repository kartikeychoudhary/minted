package com.minted.api.integration.service;

import com.minted.api.integration.dto.*;
import com.minted.api.integration.splitwise.SplitwiseApiClient.SplitwiseFriend;

import java.util.List;

public interface IntegrationService {

    // ── Status ────────────────────────────────────────────────────────────────
    IntegrationStatusResponse getSplitwiseStatus(Long userId);

    SplitwiseAdminConfigResponse getSplitwiseAdminConfig();

    // ── OAuth ─────────────────────────────────────────────────────────────────
    SplitwiseAuthUrlResponse getSplitwiseAuthUrl(Long userId);

    IntegrationStatusResponse connectSplitwise(String code, Long userId);

    void disconnectSplitwise(Long userId);

    // ── Friends ───────────────────────────────────────────────────────────────
    List<SplitwiseFriendDto> getSplitwiseFriends(Long userId);

    FriendLinkResponse linkFriend(Long friendId, Long splitwiseFriendId, Long userId);

    void unlinkFriend(Long friendId, Long userId);

    List<FriendLinkResponse> getLinkedFriends(Long userId);

    // ── Push ──────────────────────────────────────────────────────────────────
    PushResult pushSplitToSplitwise(Long splitTransactionId, boolean forcePush, Long userId);

    BulkPushResponse bulkPushToSplitwise(List<Long> splitTransactionIds, boolean forcePush, Long userId);
}
