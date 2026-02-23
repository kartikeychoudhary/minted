package com.minted.api.account.service;

import com.minted.api.account.dto.AccountRequest;
import com.minted.api.account.dto.AccountResponse;

import java.util.List;

public interface AccountService {

    List<AccountResponse> getAllByUserId(Long userId);

    List<AccountResponse> getAllActiveByUserId(Long userId);

    AccountResponse getById(Long id, Long userId);

    AccountResponse create(AccountRequest request, Long userId);

    AccountResponse update(Long id, AccountRequest request, Long userId);

    void delete(Long id, Long userId);

    void toggleActive(Long id, Long userId);
}
