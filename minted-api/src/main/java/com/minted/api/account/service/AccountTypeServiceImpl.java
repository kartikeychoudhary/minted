package com.minted.api.account.service;

import com.minted.api.account.dto.AccountTypeRequest;
import com.minted.api.account.dto.AccountTypeResponse;
import com.minted.api.account.entity.AccountType;
import com.minted.api.admin.entity.DefaultAccountType;
import com.minted.api.user.entity.User;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.account.repository.AccountTypeRepository;
import com.minted.api.admin.repository.DefaultAccountTypeRepository;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.account.service.AccountTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountTypeServiceImpl implements AccountTypeService {

    private final AccountTypeRepository accountTypeRepository;
    private final UserRepository userRepository;
    private final DefaultAccountTypeRepository defaultAccountTypeRepository;

    @Override
    @Transactional
    public List<AccountTypeResponse> getAllByUserId(Long userId) {
        List<AccountTypeResponse> userTypes = accountTypeRepository.findByUserId(userId).stream()
                .map(AccountTypeResponse::from)
                .collect(Collectors.toList());

        List<AccountTypeResponse> defaultTypes = defaultAccountTypeRepository.findAll().stream()
                .map(this::mapDefaultToResponse)
                .collect(Collectors.toList());

        return mergeAccountTypes(userTypes, defaultTypes, userId);
    }

    @Override
    @Transactional
    public List<AccountTypeResponse> getAllActiveByUserId(Long userId) {
        List<AccountTypeResponse> userTypes = accountTypeRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .map(AccountTypeResponse::from)
                .collect(Collectors.toList());

        List<AccountTypeResponse> defaultTypes = defaultAccountTypeRepository.findAll().stream()
                .map(this::mapDefaultToResponse)
                .collect(Collectors.toList());

        return mergeAccountTypes(userTypes, defaultTypes, userId);
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
            throw new com.minted.api.common.exception.DuplicateResourceException("Account type with name '" + request.name() + "' already exists");
        }

        User user = findUserById(userId);

        AccountType accountType = new AccountType();
        accountType.setName(request.name());
        accountType.setDescription(request.description());
        accountType.setIcon(request.icon());
        accountType.setUser(user);
        accountType.setIsActive(true);

        AccountType saved = accountTypeRepository.save(accountType);
        log.info("AccountType created: id={}, name={}", saved.getId(), saved.getName());
        return AccountTypeResponse.from(saved);
    }

    @Override
    @Transactional
    public AccountTypeResponse update(Long id, AccountTypeRequest request, Long userId) {
        AccountType accountType = findAccountTypeByIdAndUserId(id, userId);

        if (Boolean.TRUE.equals(accountType.getIsDefault())) {
            throw new BadRequestException("Default account types cannot be modified");
        }

        // Check if name is changing and if new name already exists
        if (!accountType.getName().equals(request.name()) &&
                accountTypeRepository.existsByNameAndUserId(request.name(), userId)) {
            throw new com.minted.api.common.exception.DuplicateResourceException("Account type with name '" + request.name() + "' already exists");
        }

        accountType.setName(request.name());
        accountType.setDescription(request.description());
        accountType.setIcon(request.icon());

        AccountType updated = accountTypeRepository.save(accountType);
        log.info("AccountType updated: id={}", updated.getId());
        return AccountTypeResponse.from(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        AccountType accountType = findAccountTypeByIdAndUserId(id, userId);

        if (Boolean.TRUE.equals(accountType.getIsDefault())) {
            throw new BadRequestException("Default account types cannot be deleted");
        }

        accountTypeRepository.delete(accountType);
        log.info("AccountType deleted: id={}", id);
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

    private AccountTypeResponse mapDefaultToResponse(DefaultAccountType type) {
        return new AccountTypeResponse(
                null, // id is null for defaults to indicate they are read-only defaults
                type.getName(),
                type.getName() + " Account",
                getDefaultIconForAccountType(type.getName()),
                true, // isActive
                true, // isDefault
                null,
                null
        );
    }

    private List<AccountTypeResponse> mergeAccountTypes(List<AccountTypeResponse> userTypes, List<AccountTypeResponse> defaultTypes, Long userId) {
        List<String> existingNames = userTypes.stream()
                .map(t -> t.name().toLowerCase())
                .collect(Collectors.toList());

        User user = findUserById(userId);
        List<AccountTypeResponse> merged = new java.util.ArrayList<>(userTypes);
        for (AccountTypeResponse defType : defaultTypes) {
            if (!existingNames.contains(defType.name().toLowerCase())) {
                // Auto-create the default account type as a real user account type
                AccountType accountType = new AccountType();
                accountType.setName(defType.name());
                accountType.setDescription(defType.description());
                accountType.setIcon(defType.icon());
                accountType.setUser(user);
                accountType.setIsActive(true);
                accountType.setIsDefault(true);
                AccountType saved = accountTypeRepository.save(accountType);
                merged.add(AccountTypeResponse.from(saved));
            }
        }
        return merged;
    }

    private String getDefaultIconForAccountType(String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.contains("bank")) return "bank";
        if (lowerName.contains("card")) return "credit-card";
        if (lowerName.contains("wallet")) return "wallet";
        if (lowerName.contains("invest")) return "chart";
        return "bank"; // fallback
    }
}
