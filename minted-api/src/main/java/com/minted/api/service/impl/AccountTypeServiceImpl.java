package com.minted.api.service.impl;

import com.minted.api.dto.AccountTypeRequest;
import com.minted.api.dto.AccountTypeResponse;
import com.minted.api.entity.AccountType;
import com.minted.api.entity.User;
import com.minted.api.exception.BadRequestException;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.AccountTypeRepository;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.AccountTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountTypeServiceImpl implements AccountTypeService {

    private final AccountTypeRepository accountTypeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AccountTypeResponse> getAllByUserId(Long userId) {
        return accountTypeRepository.findByUserId(userId).stream()
                .map(AccountTypeResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountTypeResponse> getAllActiveByUserId(Long userId) {
        return accountTypeRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(AccountTypeResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountTypeResponse getById(Long id, Long userId) {
        AccountType accountType = findAccountTypeByIdAndUserId(id, userId);
        return AccountTypeResponse.from(accountType);
    }

    @Override
    @Transactional
    public AccountTypeResponse create(AccountTypeRequest request, Long userId) {
        // Check if account type with same name already exists for user
        if (accountTypeRepository.existsByNameAndUserId(request.name(), userId)) {
            throw new BadRequestException("Account type with name '" + request.name() + "' already exists");
        }

        User user = findUserById(userId);

        AccountType accountType = new AccountType();
        accountType.setName(request.name());
        accountType.setDescription(request.description());
        accountType.setIcon(request.icon());
        accountType.setUser(user);
        accountType.setIsActive(true);

        AccountType saved = accountTypeRepository.save(accountType);
        return AccountTypeResponse.from(saved);
    }

    @Override
    @Transactional
    public AccountTypeResponse update(Long id, AccountTypeRequest request, Long userId) {
        AccountType accountType = findAccountTypeByIdAndUserId(id, userId);

        // Check if name is changing and if new name already exists
        if (!accountType.getName().equals(request.name()) &&
                accountTypeRepository.existsByNameAndUserId(request.name(), userId)) {
            throw new BadRequestException("Account type with name '" + request.name() + "' already exists");
        }

        accountType.setName(request.name());
        accountType.setDescription(request.description());
        accountType.setIcon(request.icon());

        AccountType updated = accountTypeRepository.save(accountType);
        return AccountTypeResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        AccountType accountType = findAccountTypeByIdAndUserId(id, userId);
        accountTypeRepository.delete(accountType);
    }

    @Override
    @Transactional
    public void toggleActive(Long id, Long userId) {
        AccountType accountType = findAccountTypeByIdAndUserId(id, userId);
        accountType.setIsActive(!accountType.getIsActive());
        accountTypeRepository.save(accountType);
    }

    private AccountType findAccountTypeByIdAndUserId(Long id, Long userId) {
        return accountTypeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account type not found with id: " + id));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
