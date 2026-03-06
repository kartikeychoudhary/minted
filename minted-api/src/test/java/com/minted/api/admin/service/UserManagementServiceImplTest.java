package com.minted.api.admin.service;

import com.minted.api.account.repository.AccountRepository;
import com.minted.api.account.repository.AccountTypeRepository;
import com.minted.api.admin.dto.AdminUserResponse;
import com.minted.api.admin.dto.CreateUserRequest;
import com.minted.api.admin.dto.ResetPasswordRequest;
import com.minted.api.admin.repository.DefaultAccountTypeRepository;
import com.minted.api.admin.repository.DefaultCategoryRepository;
import com.minted.api.budget.repository.BudgetRepository;
import com.minted.api.bulkimport.repository.BulkImportRepository;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.dashboard.repository.DashboardCardRepository;
import com.minted.api.notification.service.NotificationHelper;
import com.minted.api.recurring.repository.RecurringTransactionRepository;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.transaction.repository.TransactionRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DefaultCategoryRepository defaultCategoryRepository;
    @Mock private DefaultAccountTypeRepository defaultAccountTypeRepository;
    @Mock private TransactionCategoryRepository transactionCategoryRepository;
    @Mock private AccountTypeRepository accountTypeRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private RecurringTransactionRepository recurringTransactionRepository;
    @Mock private BulkImportRepository bulkImportRepository;
    @Mock private DashboardCardRepository dashboardCardRepository;
    @Mock private NotificationHelper notificationHelper;

    @InjectMocks
    private UserManagementServiceImpl userManagementService;

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(buildUser(1L, "alice")));

        List<AdminUserResponse> result = userManagementService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("alice");
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L, "alice")));

        AdminUserResponse response = userManagementService.getUserById(1L);

        assertThat(response.username()).isEqualTo("alice");
    }

    @Test
    void getUserById_notFound_throwsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userManagementService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_duplicateUsername_throwsBadRequest() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userManagementService.createUser(
                new CreateUserRequest("alice", "Password1", "Alice", "alice@example.com", "USER")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void createUser_weakPassword_throwsBadRequest() {
        when(userRepository.existsByUsername("bob")).thenReturn(false);

        assertThatThrownBy(() -> userManagementService.createUser(
                new CreateUserRequest("bob", "weak", "Bob", "bob@example.com", "USER")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Password");
    }

    @Test
    void createUser_success_seedsDefaultData() {
        User saved = buildUser(1L, "alice");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(defaultAccountTypeRepository.findAll()).thenReturn(List.of());
        when(defaultCategoryRepository.findAll()).thenReturn(List.of());

        AdminUserResponse response = userManagementService.createUser(
                new CreateUserRequest("alice", "Password1", "Alice", "alice@example.com", "USER"));

        assertThat(response.username()).isEqualTo("alice");
        verify(notificationHelper).notify(eq(1L), any(), anyString(), anyString());
    }

    // ── toggleUserActive ──────────────────────────────────────────────────────

    @Test
    void toggleUserActive_ownAccount_throwsBadRequest() {
        User user = buildUser(1L, "alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userManagementService.toggleUserActive(1L, "alice"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("own account");
    }

    @Test
    void toggleUserActive_success_flipsIsActive() {
        User user = buildUser(1L, "bob");
        user.setIsActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        AdminUserResponse response = userManagementService.toggleUserActive(1L, "alice");

        assertThat(user.getIsActive()).isFalse();
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Test
    void deleteUser_ownAccount_throwsBadRequest() {
        User user = buildUser(1L, "alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userManagementService.deleteUser(1L, "alice"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("own account");
    }

    @Test
    void deleteUser_success_deletesAllData() {
        User user = buildUser(2L, "bob");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserId(2L)).thenReturn(List.of());
        when(recurringTransactionRepository.findByUserId(2L)).thenReturn(List.of());
        when(budgetRepository.findByUserId(2L)).thenReturn(List.of());
        when(accountRepository.findByUserId(2L)).thenReturn(List.of());
        when(accountTypeRepository.findByUserId(2L)).thenReturn(List.of());
        when(transactionCategoryRepository.findByUserId(2L)).thenReturn(List.of());
        when(bulkImportRepository.findByUserIdOrderByCreatedAtDesc(2L)).thenReturn(List.of());
        when(dashboardCardRepository.findByUserIdOrderByPositionOrderAsc(2L)).thenReturn(List.of());

        userManagementService.deleteUser(2L, "alice");

        verify(userRepository).delete(user);
    }

    // ── resetPassword ─────────────────────────────────────────────────────────

    @Test
    void resetPassword_weakPassword_throwsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser(1L, "alice")));

        assertThatThrownBy(() -> userManagementService.resetPassword(1L, new ResetPasswordRequest("weak")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Password");
    }

    @Test
    void resetPassword_success() {
        User user = buildUser(1L, "alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass1")).thenReturn("hashed");

        userManagementService.resetPassword(1L, new ResetPasswordRequest("NewPass1"));

        assertThat(user.getForcePasswordChange()).isTrue();
        verify(userRepository).save(user);
    }

    // helpers

    private User buildUser(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setPassword("hashed");
        u.setIsActive(true);
        u.setForcePasswordChange(false);
        u.setRole("USER");
        return u;
    }
}
