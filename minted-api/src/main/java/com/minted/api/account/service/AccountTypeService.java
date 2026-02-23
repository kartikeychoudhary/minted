package com.minted.api.account.service;

import com.minted.api.account.dto.AccountTypeRequest;
import com.minted.api.account.dto.AccountTypeResponse;

import java.util.List;

public interface AccountTypeService {

    List<AccountTypeResponse> getAllByUserId(Long userId);

    List<AccountTypeResponse> getAllActiveByUserId(Long userId);

    AccountTypeResponse getById(Long id, Long userId);

    AccountTypeResponse create(AccountTypeRequest request, Long userId);

    AccountTypeResponse update(Long id, AccountTypeRequest request, Long userId);

    void delete(Long id, Long userId);

    void toggleActive(Long id, Long userId);
}
