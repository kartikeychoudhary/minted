package com.minted.api.split.service;

import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.friend.entity.Friend;
import com.minted.api.friend.repository.FriendRepository;
import com.minted.api.notification.service.NotificationHelper;
import com.minted.api.split.dto.*;
import com.minted.api.split.entity.SplitShare;
import com.minted.api.split.entity.SplitTransaction;
import com.minted.api.split.enums.SplitType;
import com.minted.api.split.repository.SplitShareRepository;
import com.minted.api.split.repository.SplitTransactionRepository;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SplitServiceImplTest {

    @Mock SplitTransactionRepository splitTransactionRepository;
    @Mock SplitShareRepository splitShareRepository;
    @Mock FriendRepository friendRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationHelper notificationHelper;

    @InjectMocks SplitServiceImpl splitService;

    private User user;
    private Friend friend;
    private SplitTransaction splitTx;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("alice");

        friend = new Friend();
        friend.setId(2L);
        friend.setName("Bob");
        friend.setAvatarColor("#3b82f6");
        friend.setIsActive(true);
        friend.setUser(user);

        SplitShare share = new SplitShare();
        share.setId(1L);
        share.setFriend(friend);
        share.setShareAmount(BigDecimal.valueOf(100));
        share.setIsPayer(false);
        share.setIsSettled(false);

        splitTx = new SplitTransaction();
        splitTx.setId(10L);
        splitTx.setUser(user);
        splitTx.setDescription("Dinner");
        splitTx.setCategoryName("Food");
        splitTx.setTotalAmount(BigDecimal.valueOf(200));
        splitTx.setSplitType(SplitType.EQUAL);
        splitTx.setTransactionDate(LocalDate.of(2025, 1, 15));
        splitTx.setIsSettled(false);
        List<SplitShare> shares = new ArrayList<>();
        shares.add(share);
        share.setSplitTransaction(splitTx);
        splitTx.setShares(shares);
    }

    // ── getAllByUserId ─────────────────────────────────────────────────────────

    @Test
    void getAllByUserId_returnsList() {
        when(splitTransactionRepository.findByUserIdOrderByTransactionDateDesc(1L)).thenReturn(List.of(splitTx));

        List<SplitTransactionResponse> result = splitService.getAllByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("Dinner");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsResponse() {
        when(splitTransactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(splitTx));

        SplitTransactionResponse result = splitService.getById(10L, 1L);

        assertThat(result.description()).isEqualTo("Dinner");
    }

    @Test
    void getById_notFound_throwsException() {
        when(splitTransactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> splitService.getById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_found_deletesTransaction() {
        when(splitTransactionRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(splitTx));
        doNothing().when(splitTransactionRepository).delete(splitTx);

        splitService.delete(10L, 1L);

        verify(splitTransactionRepository).delete(splitTx);
    }

    // ── getBalanceSummary ─────────────────────────────────────────────────────

    @Test
    void getBalanceSummary_returnsSums() {
        when(splitTransactionRepository.sumOwedToUser(1L)).thenReturn(BigDecimal.valueOf(300));
        when(splitTransactionRepository.sumUserOwes(1L)).thenReturn(BigDecimal.valueOf(150));

        SplitBalanceSummaryResponse result = splitService.getBalanceSummary(1L);

        assertThat(result.youAreOwed()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(result.youOwe()).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    // ── getFriendBalances ─────────────────────────────────────────────────────

    @Test
    void getFriendBalances_mapsRows() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{2L, "Bob", "#3b82f6", BigDecimal.valueOf(100)});
        when(splitShareRepository.findUnsettledBalancesByUserId(1L)).thenReturn(rows);

        List<FriendBalanceResponse> result = splitService.getFriendBalances(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).friendName()).isEqualTo("Bob");
        assertThat(result.get(0).balance()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    // ── getSharesByFriend ─────────────────────────────────────────────────────

    @Test
    void getSharesByFriend_returnsMappedShares() {
        SplitShare share = splitTx.getShares().get(0);
        when(splitShareRepository.findByUserIdAndFriendId(1L, 2L)).thenReturn(List.of(share));

        List<SplitShareResponse> result = splitService.getSharesByFriend(2L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).friendName()).isEqualTo("Bob");
    }
}
