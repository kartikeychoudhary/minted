package com.minted.api.account.service;

import com.minted.api.account.dto.AccountTypeRequest;
import com.minted.api.account.dto.AccountTypeResponse;
import com.minted.api.account.entity.AccountType;
import com.minted.api.account.repository.AccountTypeRepository;
import com.minted.api.admin.entity.DefaultAccountType;
import com.minted.api.admin.repository.DefaultAccountTypeRepository;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.DuplicateResourceException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountTypeServiceImplTest {

    @Mock AccountTypeRepository accountTypeRepository;
    @Mock UserRepository userRepository;
    @Mock DefaultAccountTypeRepository defaultAccountTypeRepository;

    @InjectMocks AccountTypeServiceImpl accountTypeService;

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_duplicateName_throwsDuplicate() {
        when(accountTypeRepository.existsByNameAndUserId("Bank", 1L)).thenReturn(true);

        assertThatThrownBy(() -> accountTypeService.create(new AccountTypeRequest("Bank", "desc", "bank"), 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_success() {
        when(accountTypeRepository.existsByNameAndUserId("Wallet", 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L)));

        AccountType saved = buildAccountType(1L, "Wallet", false);
        when(accountTypeRepository.save(any(AccountType.class))).thenReturn(saved);

        AccountTypeResponse response = accountTypeService.create(new AccountTypeRequest("Wallet", "desc", "wallet"), 1L);

        assertThat(response.name()).isEqualTo("Wallet");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_defaultType_throwsBadRequest() {
        AccountType defaultType = buildAccountType(1L, "Bank", true);
        when(accountTypeRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(defaultType));

        assertThatThrownBy(() -> accountTypeService.update(1L, new AccountTypeRequest("Bank2", "d", "b"), 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be modified");
    }

    @Test
    void update_duplicateName_throwsDuplicate() {
        AccountType existing = buildAccountType(1L, "Wallet", false);
        when(accountTypeRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(accountTypeRepository.existsByNameAndUserId("Bank", 1L)).thenReturn(true);

        assertThatThrownBy(() -> accountTypeService.update(1L, new AccountTypeRequest("Bank", "d", "b"), 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void update_success() {
        AccountType existing = buildAccountType(1L, "Wallet", false);
        when(accountTypeRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(accountTypeRepository.existsByNameAndUserId("NewWallet", 1L)).thenReturn(false);
        when(accountTypeRepository.save(existing)).thenReturn(existing);

        accountTypeService.update(1L, new AccountTypeRequest("NewWallet", "desc", "wallet"), 1L);

        assertThat(existing.getName()).isEqualTo("NewWallet");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_defaultType_throwsBadRequest() {
        AccountType defaultType = buildAccountType(1L, "Bank", true);
        when(accountTypeRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(defaultType));

        assertThatThrownBy(() -> accountTypeService.delete(1L, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be deleted");
    }

    @Test
    void delete_success_softDeletes() {
        AccountType at = buildAccountType(1L, "Wallet", false);
        when(accountTypeRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(at));
        when(accountTypeRepository.save(at)).thenReturn(at);

        accountTypeService.delete(1L, 1L);

        assertThat(at.getIsActive()).isFalse();
    }

    // ── getById ────────────────────────────────────────────────────────────────

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(accountTypeRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountTypeService.getById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // helpers

    private AccountType buildAccountType(Long id, String name, boolean isDefault) {
        AccountType at = new AccountType();
        at.setId(id);
        at.setName(name);
        at.setIsActive(true);
        at.setIsDefault(isDefault);
        return at;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("alice");
        return u;
    }
}
