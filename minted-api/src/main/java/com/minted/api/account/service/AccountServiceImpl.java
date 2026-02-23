package com.minted.api.account.service;

import com.minted.api.account.dto.AccountRequest;
import com.minted.api.account.dto.AccountResponse;
import com.minted.api.account.entity.Account;
import com.minted.api.account.entity.AccountType;
import com.minted.api.user.entity.User;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.account.repository.AccountTypeRepository;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountTypeRepository accountTypeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllActiveByUserId(Long userId) {
        return accountRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getById(Long id, Long userId) {
        Account account = findAccountByIdAndUserId(id, userId);
        return AccountResponse.from(account);
    }

    @Override
    @Transactional
    public AccountResponse create(AccountRequest request, Long userId) {
        // Check if account with same name already exists for user
        if (accountRepository.existsByNameAndUserId(request.name(), userId)) {
            throw new com.minted.api.common.exception.DuplicateResourceException("Account with name '" + request.name() + "' already exists");
        }

        User user = findUserById(userId);
        AccountType accountType = findAccountTypeByIdAndUserId(request.accountTypeId(), userId);

        Account account = new Account();
        account.setName(request.name());
        account.setAccountType(accountType);
        account.setBalance(request.balance() != null ? request.balance() : BigDecimal.ZERO);
        account.setCurrency(request.currency() != null ? request.currency() : "INR");
        account.setColor(request.color());
        account.setIcon(request.icon());
        account.setUser(user);
        account.setIsActive(true);

        Account saved = accountRepository.save(account);
        return AccountResponse.from(saved);
    }

    @Override
    @Transactional
    public AccountResponse update(Long id, AccountRequest request, Long userId) {
        Account account = findAccountByIdAndUserId(id, userId);

        // Check if name is changing and if new name already exists
        if (!account.getName().equals(request.name()) &&
                accountRepository.existsByNameAndUserId(request.name(), userId)) {
            throw new com.minted.api.common.exception.DuplicateResourceException("Account with name '" + request.name() + "' already exists");
        }

        AccountType accountType = findAccountTypeByIdAndUserId(request.accountTypeId(), userId);

        account.setName(request.name());
        account.setAccountType(accountType);
        account.setBalance(request.balance() != null ? request.balance() : account.getBalance());
        account.setCurrency(request.currency() != null ? request.currency() : account.getCurrency());
        account.setColor(request.color());
        account.setIcon(request.icon());

        Account updated = accountRepository.save(account);
        return AccountResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Account account = findAccountByIdAndUserId(id, userId);
        accountRepository.delete(account);
    }

    @Override
    @Transactional
    public void toggleActive(Long id, Long userId) {
        Account account = findAccountByIdAndUserId(id, userId);
        account.setIsActive(!account.getIsActive());
        accountRepository.save(account);
    }

    private Account findAccountByIdAndUserId(Long id, Long userId) {
        return accountRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    private AccountType findAccountTypeByIdAndUserId(Long accountTypeId, Long userId) {
        return accountTypeRepository.findByIdAndUserId(accountTypeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account type not found with id: " + accountTypeId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
