package com.minted.api.account.service;

import com.minted.api.account.dto.AccountRequest;
import com.minted.api.account.dto.AccountResponse;
import com.minted.api.account.entity.Account;
import com.minted.api.account.entity.AccountType;
import com.minted.api.account.repository.AccountRepository;
import com.minted.api.account.repository.AccountTypeRepository;
import com.minted.api.common.exception.DuplicateResourceException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private AccountTypeRepository accountTypeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_softDeletedExists_restoresAccount() {
        Account softDeleted = buildAccount(1L, "Savings", false);
        AccountType type = buildAccountType(10L);
        AccountRequest request = new AccountRequest("Savings", 10L, BigDecimal.TEN, "INR", "#fff", "bank");

        when(accountRepository.findByNameAndUserIdAndIsActiveFalse("Savings", 1L))
                .thenReturn(Optional.of(softDeleted));
        when(accountTypeRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(type));
        when(accountRepository.save(softDeleted)).thenReturn(softDeleted);

        AccountResponse response = accountService.create(request, 1L);

        assertThat(response.name()).isEqualTo("Savings");
        assertThat(softDeleted.getIsActive()).isTrue();
        verify(accountRepository).save(softDeleted);
    }

    @Test
    void create_duplicateName_throwsDuplicateResourceException() {
        AccountRequest request = new AccountRequest("Savings", 10L, null, null, null, null);

        when(accountRepository.findByNameAndUserIdAndIsActiveFalse("Savings", 1L))
                .thenReturn(Optional.empty());
        when(accountRepository.existsByNameAndUserId("Savings", 1L)).thenReturn(true);

        assertThatThrownBy(() -> accountService.create(request, 1L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Savings");
    }

    @Test
    void create_newAccount_success() {
        AccountRequest request = new AccountRequest("Checking", 10L, BigDecimal.ZERO, "INR", null, null);
        User user = buildUser(1L);
        AccountType type = buildAccountType(10L);
        Account savedAccount = buildAccount(2L, "Checking", true);
        savedAccount.setAccountType(type);

        when(accountRepository.findByNameAndUserIdAndIsActiveFalse("Checking", 1L)).thenReturn(Optional.empty());
        when(accountRepository.existsByNameAndUserId("Checking", 1L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountTypeRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(type));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponse response = accountService.create(request, 1L);

        assertThat(response.name()).isEqualTo("Checking");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_nameChangeToExisting_throwsDuplicate() {
        Account existing = buildAccount(1L, "Savings", true);
        AccountRequest request = new AccountRequest("Checking", 10L, null, null, null, null);

        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(accountRepository.existsByNameAndUserId("Checking", 1L)).thenReturn(true);

        assertThatThrownBy(() -> accountService.update(1L, request, 1L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void update_sameName_doesNotCheckDuplicate() {
        Account existing = buildAccount(1L, "Savings", true);
        existing.setAccountType(buildAccountType(10L));
        AccountRequest request = new AccountRequest("Savings", 10L, BigDecimal.TEN, "USD", null, null);
        AccountType type = buildAccountType(10L);

        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(accountTypeRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(type));
        when(accountRepository.save(existing)).thenReturn(existing);

        accountService.update(1L, request, 1L);

        verify(accountRepository, never()).existsByNameAndUserId(anyString(), anyLong());
    }

    @Test
    void update_notFound_throwsResourceNotFound() {
        when(accountRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.update(99L, new AccountRequest("x", 10L, null, null, null, null), 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_softDeletesAccount() {
        Account account = buildAccount(1L, "Savings", true);
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        accountService.delete(1L, 1L);

        assertThat(account.getIsActive()).isFalse();
        verify(accountRepository).save(account);
    }

    @Test
    void delete_notFound_throwsResourceNotFound() {
        when(accountRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.delete(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_notFound_throwsResourceNotFound() {
        when(accountRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getById(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_found_returnsResponse() {
        Account account = buildAccount(1L, "Savings", true);
        account.setAccountType(buildAccountType(10L));
        when(accountRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getById(1L, 1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    // ── getAllByUserId ────────────────────────────────────────────────────────

    @Test
    void getAllByUserId_returnsAll() {
        Account a = buildAccount(1L, "A", true);
        a.setAccountType(buildAccountType(10L));
        when(accountRepository.findByUserIdAndIsActiveTrue(1L)).thenReturn(List.of(a));

        List<AccountResponse> result = accountService.getAllByUserId(1L);

        assertThat(result).hasSize(1);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Account buildAccount(Long id, String name, boolean active) {
        Account a = new Account();
        a.setId(id);
        a.setName(name);
        a.setIsActive(active);
        a.setBalance(BigDecimal.ZERO);
        a.setCurrency("INR");
        return a;
    }

    private AccountType buildAccountType(Long id) {
        AccountType at = new AccountType();
        at.setId(id);
        at.setName("Bank");
        return at;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("alice");
        return u;
    }
}
