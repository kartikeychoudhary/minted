package com.minted.api.split.service;

import com.minted.api.split.dto.*;

import java.util.List;

public interface SplitService {

    List<SplitTransactionResponse> getAllByUserId(Long userId);

    SplitTransactionResponse getById(Long id, Long userId);

    SplitTransactionResponse create(SplitTransactionRequest request, Long userId);

    SplitTransactionResponse update(Long id, SplitTransactionRequest request, Long userId);

    void delete(Long id, Long userId);

    SplitBalanceSummaryResponse getBalanceSummary(Long userId);

    List<FriendBalanceResponse> getFriendBalances(Long userId);

    void settleFriend(SettleRequest request, Long userId);

    List<SplitShareResponse> getSharesByFriend(Long friendId, Long userId);
}
