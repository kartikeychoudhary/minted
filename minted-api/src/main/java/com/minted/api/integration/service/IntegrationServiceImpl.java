package com.minted.api.integration.service;

import com.minted.api.admin.repository.SystemSettingRepository;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.friend.entity.Friend;
import com.minted.api.friend.repository.FriendRepository;
import com.minted.api.integration.dto.*;
import com.minted.api.integration.entity.FriendSplitwiseLink;
import com.minted.api.integration.entity.SplitSplitwisePush;
import com.minted.api.integration.entity.UserIntegration;
import com.minted.api.integration.repository.FriendSplitwiseLinkRepository;
import com.minted.api.integration.repository.SplitSplitwisePushRepository;
import com.minted.api.integration.repository.UserIntegrationRepository;
import com.minted.api.integration.splitwise.SplitwiseApiClient;
import com.minted.api.integration.splitwise.SplitwiseApiClient.*;
import com.minted.api.integration.splitwise.SplitwiseApiException;
import com.minted.api.split.entity.SplitShare;
import com.minted.api.split.entity.SplitTransaction;
import com.minted.api.split.repository.SplitTransactionRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationServiceImpl implements IntegrationService {

    private static final String PROVIDER = "SPLITWISE";
    private static final String KEY_ENABLED = "SPLITWISE_ENABLED";
    private static final String KEY_CLIENT_ID = "SPLITWISE_CLIENT_ID";
    private static final String KEY_CLIENT_SECRET = "SPLITWISE_CLIENT_SECRET";
    private static final String KEY_REDIRECT_URI = "SPLITWISE_REDIRECT_URI";

    private final UserIntegrationRepository userIntegrationRepository;
    private final FriendSplitwiseLinkRepository friendLinkRepository;
    private final SplitSplitwisePushRepository splitPushRepository;
    private final SplitTransactionRepository splitTransactionRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final SplitwiseApiClient splitwiseClient;

    // ── Status ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public IntegrationStatusResponse getSplitwiseStatus(Long userId) {
        boolean enabled = isSplitwiseEnabled();
        Optional<UserIntegration> integration = userIntegrationRepository.findByUserIdAndProvider(userId, PROVIDER);
        if (integration.isPresent()) {
            UserIntegration ui = integration.get();
            return new IntegrationStatusResponse(PROVIDER, enabled, true,
                    ui.getSplitwiseUserName(), ui.getSplitwiseUserEmail(), ui.getConnectedAt());
        }
        return new IntegrationStatusResponse(PROVIDER, enabled, false, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public SplitwiseAdminConfigResponse getSplitwiseAdminConfig() {
        String clientId = getSetting(KEY_CLIENT_ID);
        String clientSecret = getSetting(KEY_CLIENT_SECRET);
        String redirectUri = getSetting(KEY_REDIRECT_URI);
        boolean enabled = "true".equalsIgnoreCase(getSetting(KEY_ENABLED));
        return new SplitwiseAdminConfigResponse(
                enabled,
                clientId != null && !clientId.isBlank(),
                clientSecret != null && !clientSecret.isBlank(),
                redirectUri
        );
    }

    // ── OAuth ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public SplitwiseAuthUrlResponse getSplitwiseAuthUrl(Long userId) {
        requireSplitwiseEnabled();
        String clientId = requireSetting(KEY_CLIENT_ID, "Splitwise Client ID is not configured. Ask your administrator to set it up in Server Settings.");
        String redirectUri = requireSetting(KEY_REDIRECT_URI, "Splitwise Redirect URI is not configured. Ask your administrator to set it up in Server Settings.");
        String state = UUID.randomUUID().toString();
        String url = splitwiseClient.buildAuthorizationUrl(clientId, redirectUri, state);
        return new SplitwiseAuthUrlResponse(url);
    }

    @Override
    @Transactional
    public IntegrationStatusResponse connectSplitwise(String code, Long userId) {
        requireSplitwiseEnabled();

        if (code == null || code.isBlank()) {
            throw new BadRequestException("Authorization code is missing. Please try the OAuth flow again.");
        }

        String clientId = requireSetting(KEY_CLIENT_ID, "Splitwise Client ID is not configured.");
        String clientSecret = requireSetting(KEY_CLIENT_SECRET, "Splitwise Client Secret is not configured.");
        String redirectUri = requireSetting(KEY_REDIRECT_URI, "Splitwise Redirect URI is not configured.");

        // Exchange the code for an access token
        SplitwiseTokenResponse tokenResponse = splitwiseClient.exchangeCodeForToken(code, clientId, clientSecret, redirectUri);

        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            throw new BadRequestException("Splitwise did not return an access token. The authorization code may have expired. Please try again.");
        }

        // Fetch the Splitwise user info to confirm the connection
        SplitwiseUser swUser = splitwiseClient.getCurrentUser(tokenResponse.accessToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Upsert
        UserIntegration ui = userIntegrationRepository
                .findByUserIdAndProvider(userId, PROVIDER)
                .orElse(new UserIntegration());

        ui.setUser(user);
        ui.setProvider(PROVIDER);
        ui.setAccessToken(tokenResponse.accessToken());
        ui.setTokenType(tokenResponse.tokenType());
        ui.setSplitwiseUserId(swUser.id());
        ui.setSplitwiseUserName(swUser.displayName());
        ui.setSplitwiseUserEmail(swUser.email());
        ui.setConnectedAt(LocalDateTime.now());

        userIntegrationRepository.save(ui);
        log.info("User {} connected Splitwise account: swUserId={}", userId, swUser.id());

        return new IntegrationStatusResponse(PROVIDER, true, true,
                swUser.displayName(), swUser.email(), ui.getConnectedAt());
    }

    @Override
    @Transactional
    public void disconnectSplitwise(Long userId) {
        userIntegrationRepository.deleteByUserIdAndProvider(userId, PROVIDER);
        log.info("User {} disconnected Splitwise", userId);
    }

    // ── Friends ───────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<SplitwiseFriendDto> getSplitwiseFriends(Long userId) {
        String token = requireAccessToken(userId);

        List<SplitwiseFriend> swFriends = splitwiseClient.getFriends(token);

        // Load existing links so we can annotate which ones are already linked
        List<FriendSplitwiseLink> links = friendLinkRepository.findAll();
        Map<Long, Long> swIdToMintedFriendId = links.stream()
                .collect(Collectors.toMap(
                        FriendSplitwiseLink::getSplitwiseFriendId,
                        l -> l.getFriend().getId()
                ));

        return swFriends.stream()
                .map(f -> f.toDto(swIdToMintedFriendId.get(f.id())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FriendLinkResponse linkFriend(Long friendId, Long splitwiseFriendId, Long userId) {
        Friend friend = friendRepository.findByIdAndUserId(friendId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found with id: " + friendId + ". Make sure the friend belongs to your account."));

        // Verify the Splitwise friend exists by fetching friends list
        String token = requireAccessToken(userId);
        List<SplitwiseFriend> swFriends = splitwiseClient.getFriends(token);
        SplitwiseFriend swFriend = swFriends.stream()
                .filter(f -> f.id().equals(splitwiseFriendId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Splitwise friend with ID " + splitwiseFriendId + " was not found in your Splitwise account. " +
                        "Make sure you are linked with them on Splitwise first."));

        // Upsert the link
        FriendSplitwiseLink link = friendLinkRepository.findByFriendId(friendId)
                .orElse(new FriendSplitwiseLink());
        link.setFriend(friend);
        link.setSplitwiseFriendId(swFriend.id());
        link.setSplitwiseFriendName(swFriend.displayName());
        link.setSplitwiseFriendEmail(swFriend.email());
        link.setLinkedAt(LocalDateTime.now());

        FriendSplitwiseLink saved = friendLinkRepository.save(link);
        log.info("Linked minted friend {} to Splitwise friend {}", friendId, splitwiseFriendId);

        return new FriendLinkResponse(
                friend.getId(), friend.getName(),
                saved.getSplitwiseFriendId(), saved.getSplitwiseFriendName(),
                saved.getSplitwiseFriendEmail(), saved.getLinkedAt()
        );
    }

    @Override
    @Transactional
    public void unlinkFriend(Long friendId, Long userId) {
        // verify ownership
        friendRepository.findByIdAndUserId(friendId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found with id: " + friendId));
        friendLinkRepository.deleteByFriendId(friendId);
        log.info("Unlinked minted friend {} from Splitwise", friendId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendLinkResponse> getLinkedFriends(Long userId) {
        // Get all friends belonging to this user
        List<Friend> userFriends = friendRepository.findByUserId(userId);
        List<Long> friendIds = userFriends.stream().map(Friend::getId).collect(Collectors.toList());

        List<FriendSplitwiseLink> links = friendLinkRepository.findByFriendIdIn(friendIds);
        Map<Long, Friend> friendMap = userFriends.stream()
                .collect(Collectors.toMap(Friend::getId, f -> f));

        return links.stream()
                .map(link -> new FriendLinkResponse(
                        link.getFriend().getId(),
                        friendMap.getOrDefault(link.getFriend().getId(), link.getFriend()).getName(),
                        link.getSplitwiseFriendId(),
                        link.getSplitwiseFriendName(),
                        link.getSplitwiseFriendEmail(),
                        link.getLinkedAt()
                ))
                .collect(Collectors.toList());
    }

    // ── Push ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public PushResult pushSplitToSplitwise(Long splitTransactionId, boolean forcePush, Long userId) {
        requireSplitwiseEnabled();
        String token = requireAccessToken(userId);
        UserIntegration ui = userIntegrationRepository.findByUserIdAndProvider(userId, PROVIDER)
                .orElseThrow(() -> new BadRequestException("Splitwise account not connected."));

        SplitTransaction split = splitTransactionRepository.findByIdAndUserId(splitTransactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Split transaction not found with id: " + splitTransactionId));

        // Check for duplicate push
        if (!forcePush && splitPushRepository.existsBySplitTransactionIdAndUserId(splitTransactionId, userId)) {
            Optional<SplitSplitwisePush> existing = splitPushRepository
                    .findBySplitTransactionIdAndUserId(splitTransactionId, userId);
            return new PushResult(splitTransactionId, split.getDescription(), false, true,
                    existing.map(SplitSplitwisePush::getSplitwiseExpenseId).orElse(null),
                    "This split has already been pushed to Splitwise. Enable force push to push again.");
        }

        try {
            CreateExpenseRequest expenseRequest = buildExpenseRequest(split, ui, userId);
            SplitwiseExpense expense = splitwiseClient.createExpense(token, expenseRequest);

            // Record the push
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            SplitSplitwisePush push = splitPushRepository
                    .findBySplitTransactionIdAndUserId(splitTransactionId, userId)
                    .orElse(new SplitSplitwisePush());
            push.setSplitTransaction(split);
            push.setUser(user);
            push.setSplitwiseExpenseId(expense.id());
            push.setPushedAt(LocalDateTime.now());
            splitPushRepository.save(push);

            log.info("Pushed split {} to Splitwise as expense {}", splitTransactionId, expense.id());
            return new PushResult(splitTransactionId, split.getDescription(), true, false, expense.id(), null);

        } catch (SplitwiseApiException e) {
            log.warn("Failed to push split {} to Splitwise: {}", splitTransactionId, e.getMessage());
            return new PushResult(splitTransactionId, split.getDescription(), false, false, null, e.getMessage());
        }
    }

    @Override
    @Transactional
    public BulkPushResponse bulkPushToSplitwise(List<Long> splitTransactionIds, boolean forcePush, Long userId) {
        if (splitTransactionIds == null || splitTransactionIds.isEmpty()) {
            throw new BadRequestException("No split transactions specified for bulk push.");
        }
        if (splitTransactionIds.size() > 50) {
            throw new BadRequestException("Bulk push limit is 50 split transactions at a time. You selected " + splitTransactionIds.size() + ".");
        }

        List<PushResult> results = new ArrayList<>();
        for (Long id : splitTransactionIds) {
            results.add(pushSplitToSplitwise(id, forcePush, userId));
        }

        long successCount = results.stream().filter(PushResult::success).count();
        long skippedCount = results.stream().filter(PushResult::alreadyPushed).count();
        long failedCount = results.stream().filter(r -> !r.success() && !r.alreadyPushed()).count();

        return new BulkPushResponse(
                splitTransactionIds.size(),
                (int) successCount,
                (int) skippedCount,
                (int) failedCount,
                results
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CreateExpenseRequest buildExpenseRequest(SplitTransaction split, UserIntegration ui, Long userId) {
        List<ExpenseUser> expenseUsers = new ArrayList<>();

        for (SplitShare share : split.getShares()) {
            Long swUserId;
            if (share.getFriend() == null) {
                // "Me" — the current Splitwise user
                swUserId = ui.getSplitwiseUserId();
            } else {
                FriendSplitwiseLink link = friendLinkRepository.findByFriendId(share.getFriend().getId())
                        .orElseThrow(() -> new SplitwiseApiException(
                                "Friend '" + share.getFriend().getName() + "' is not linked to a Splitwise contact. " +
                                "Link them in the Integrations page before pushing this split."));
                swUserId = link.getSplitwiseFriendId();
            }

            BigDecimal paidShare = share.getIsPayer() ? split.getTotalAmount() : BigDecimal.ZERO;
            expenseUsers.add(new ExpenseUser(swUserId, paidShare, share.getShareAmount()));
        }

        return new CreateExpenseRequest(
                split.getDescription(),
                split.getTotalAmount(),
                split.getTransactionDate(),
                expenseUsers
        );
    }

    private String requireAccessToken(Long userId) {
        UserIntegration ui = userIntegrationRepository.findByUserIdAndProvider(userId, PROVIDER)
                .orElseThrow(() -> new BadRequestException(
                        "Your Splitwise account is not connected. Connect it in the Integrations page first."));
        if (ui.getAccessToken() == null || ui.getAccessToken().isBlank()) {
            throw new BadRequestException("Splitwise access token is missing. Please reconnect your Splitwise account.");
        }
        return ui.getAccessToken();
    }

    private void requireSplitwiseEnabled() {
        if (!isSplitwiseEnabled()) {
            throw new BadRequestException("The Splitwise integration is not enabled on this server. Contact your administrator.");
        }
    }

    private boolean isSplitwiseEnabled() {
        String val = getSetting(KEY_ENABLED);
        return "true".equalsIgnoreCase(val);
    }

    private String getSetting(String key) {
        return systemSettingRepository.findBySettingKey(key)
                .map(s -> s.getSettingValue())
                .orElse(null);
    }

    private String requireSetting(String key, String userFriendlyError) {
        String value = getSetting(key);
        if (value == null || value.isBlank()) {
            throw new BadRequestException(userFriendlyError);
        }
        return value;
    }
}
