package com.minted.api.split.service;

import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.friend.entity.Friend;
import com.minted.api.friend.repository.FriendRepository;
import com.minted.api.notification.enums.NotificationType;
import com.minted.api.notification.service.NotificationHelper;
import com.minted.api.split.dto.*;
import com.minted.api.split.entity.SplitShare;
import com.minted.api.split.entity.SplitTransaction;
import com.minted.api.split.repository.SplitShareRepository;
import com.minted.api.split.repository.SplitTransactionRepository;
import com.minted.api.transaction.entity.Transaction;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SplitServiceImpl implements SplitService {

    private final SplitTransactionRepository splitTransactionRepository;
    private final SplitShareRepository splitShareRepository;
    private final FriendRepository friendRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationHelper notificationHelper;

    @Override
    @Transactional(readOnly = true)
    public List<SplitTransactionResponse> getAllByUserId(Long userId) {
        return splitTransactionRepository.findByUserIdOrderByTransactionDateDesc(userId).stream()
                .map(SplitTransactionResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SplitTransactionResponse getById(Long id, Long userId) {
        SplitTransaction st = findByIdAndUserId(id, userId);
        return SplitTransactionResponse.from(st);
    }

    @Override
    @Transactional
    public SplitTransactionResponse create(SplitTransactionRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        SplitTransaction st = new SplitTransaction();
        st.setUser(user);
        st.setDescription(request.description());
        st.setCategoryName(request.categoryName());
        st.setTotalAmount(request.totalAmount());
        st.setSplitType(request.splitType());
        st.setTransactionDate(request.transactionDate());
        st.setIsSettled(false);

        // Link source transaction if provided
        if (request.sourceTransactionId() != null) {
            Transaction source = transactionRepository.findByIdAndUserId(request.sourceTransactionId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Source transaction not found"));
            st.setSourceTransaction(source);
        }

        // Build shares
        List<SplitShare> shares = buildShares(request, st, userId);
        st.setShares(shares);

        SplitTransaction saved = splitTransactionRepository.save(st);
        log.info("Split transaction created: id={}, description={}", saved.getId(), saved.getDescription());
        return SplitTransactionResponse.from(saved);
    }

    @Override
    @Transactional
    public SplitTransactionResponse update(Long id, SplitTransactionRequest request, Long userId) {
        SplitTransaction st = findByIdAndUserId(id, userId);

        st.setDescription(request.description());
        st.setCategoryName(request.categoryName());
        st.setTotalAmount(request.totalAmount());
        st.setSplitType(request.splitType());
        st.setTransactionDate(request.transactionDate());

        // Link source transaction if provided
        if (request.sourceTransactionId() != null) {
            Transaction source = transactionRepository.findByIdAndUserId(request.sourceTransactionId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Source transaction not found"));
            st.setSourceTransaction(source);
        } else {
            st.setSourceTransaction(null);
        }

        // Clear existing shares (orphanRemoval will delete them) and rebuild
        st.getShares().clear();
        List<SplitShare> newShares = buildShares(request, st, userId);
        st.getShares().addAll(newShares);

        // Recheck settlement status
        boolean allSettled = st.getShares().stream().allMatch(SplitShare::getIsSettled);
        st.setIsSettled(allSettled);

        SplitTransaction updated = splitTransactionRepository.save(st);
        log.info("Split transaction updated: id={}", updated.getId());
        return SplitTransactionResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        SplitTransaction st = findByIdAndUserId(id, userId);
        splitTransactionRepository.delete(st);
        log.info("Split transaction deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public SplitBalanceSummaryResponse getBalanceSummary(Long userId) {
        BigDecimal youAreOwed = splitTransactionRepository.sumOwedToUser(userId);
        BigDecimal youOwe = splitTransactionRepository.sumUserOwes(userId);
        return new SplitBalanceSummaryResponse(youAreOwed, youOwe);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendBalanceResponse> getFriendBalances(Long userId) {
        List<Object[]> results = splitShareRepository.findUnsettledBalancesByUserId(userId);
        return results.stream()
                .map(row -> new FriendBalanceResponse(
                        (Long) row[0],
                        (String) row[1],
                        (String) row[2],
                        (BigDecimal) row[3]
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void settleFriend(SettleRequest request, Long userId) {
        Friend friend = friendRepository.findByIdAndUserId(request.friendId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found with id: " + request.friendId()));

        List<SplitShare> unsettledShares = splitShareRepository.findUnsettledByUserIdAndFriendId(userId, request.friendId());

        if (unsettledShares.isEmpty()) {
            throw new BadRequestException("No unsettled shares found for friend: " + friend.getName());
        }

        LocalDateTime now = LocalDateTime.now();
        for (SplitShare share : unsettledShares) {
            share.setIsSettled(true);
            share.setSettledAt(now);
            splitShareRepository.save(share);

            // Check if all shares in the parent split transaction are settled
            SplitTransaction st = share.getSplitTransaction();
            boolean allSettled = st.getShares().stream().allMatch(SplitShare::getIsSettled);
            if (allSettled) {
                st.setIsSettled(true);
                splitTransactionRepository.save(st);
            }
        }

        log.info("Settled {} shares for friend: id={}, name={}", unsettledShares.size(), friend.getId(), friend.getName());

        notificationHelper.notify(userId, NotificationType.SUCCESS,
                "Settlement Complete",
                "All expenses with " + friend.getName() + " have been settled.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SplitShareResponse> getSharesByFriend(Long friendId, Long userId) {
        return splitShareRepository.findByUserIdAndFriendId(userId, friendId).stream()
                .map(SplitShareResponse::from)
                .collect(Collectors.toList());
    }

    private List<SplitShare> buildShares(SplitTransactionRequest request, SplitTransaction st, Long userId) {
        List<SplitShare> shares = new ArrayList<>();

        // Validate that share amounts sum to total
        BigDecimal sharesSum = request.shares().stream()
                .map(SplitShareRequest::shareAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sharesSum.compareTo(request.totalAmount()) != 0) {
            // For EQUAL split, auto-calculate
            if (request.splitType() == com.minted.api.split.enums.SplitType.EQUAL) {
                return buildEqualShares(request, st, userId);
            }
            throw new BadRequestException("Share amounts (" + sharesSum + ") must equal total amount (" + request.totalAmount() + ")");
        }

        for (SplitShareRequest shareReq : request.shares()) {
            SplitShare share = new SplitShare();
            share.setSplitTransaction(st);
            share.setShareAmount(shareReq.shareAmount());
            share.setSharePercentage(shareReq.sharePercentage());
            share.setIsPayer(shareReq.isPayer() != null ? shareReq.isPayer() : false);
            share.setIsSettled(false);

            if (shareReq.friendId() != null) {
                Friend friend = friendRepository.findByIdAndUserId(shareReq.friendId(), userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Friend not found with id: " + shareReq.friendId()));
                share.setFriend(friend);
            }
            // friendId == null means "Me" (the user)

            shares.add(share);
        }

        return shares;
    }

    private List<SplitShare> buildEqualShares(SplitTransactionRequest request, SplitTransaction st, Long userId) {
        List<SplitShare> shares = new ArrayList<>();
        int count = request.shares().size();
        BigDecimal equalAmount = request.totalAmount().divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
        BigDecimal remainder = request.totalAmount().subtract(equalAmount.multiply(BigDecimal.valueOf(count)));

        for (int i = 0; i < request.shares().size(); i++) {
            SplitShareRequest shareReq = request.shares().get(i);
            SplitShare share = new SplitShare();
            share.setSplitTransaction(st);

            // Give remainder to the first share
            BigDecimal amount = (i == 0) ? equalAmount.add(remainder) : equalAmount;
            share.setShareAmount(amount);
            share.setIsPayer(shareReq.isPayer() != null ? shareReq.isPayer() : false);
            share.setIsSettled(false);

            if (shareReq.friendId() != null) {
                Friend friend = friendRepository.findByIdAndUserId(shareReq.friendId(), userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Friend not found with id: " + shareReq.friendId()));
                share.setFriend(friend);
            }

            shares.add(share);
        }

        return shares;
    }

    private SplitTransaction findByIdAndUserId(Long id, Long userId) {
        return splitTransactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Split transaction not found with id: " + id));
    }
}
