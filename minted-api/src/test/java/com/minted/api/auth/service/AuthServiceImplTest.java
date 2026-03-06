package com.minted.api.auth.service;

import com.minted.api.account.repository.AccountTypeRepository;
import com.minted.api.admin.repository.DefaultAccountTypeRepository;
import com.minted.api.admin.repository.DefaultCategoryRepository;
import com.minted.api.admin.service.SystemSettingService;
import com.minted.api.auth.dto.*;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.common.exception.UnauthorizedException;
import com.minted.api.common.util.JwtUtil;
import com.minted.api.notification.service.NotificationHelper;
import com.minted.api.transaction.repository.TransactionCategoryRepository;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private SystemSettingService systemSettingService;
    @Mock private NotificationHelper notificationHelper;
    @Mock private DefaultCategoryRepository defaultCategoryRepository;
    @Mock private DefaultAccountTypeRepository defaultAccountTypeRepository;
    @Mock private TransactionCategoryRepository transactionCategoryRepository;
    @Mock private AccountTypeRepository accountTypeRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86_400_000L);
    }

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    void login_success() {
        User user = buildActiveUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("alice")).thenReturn("token");

        LoginResponse response = authService.login(new LoginRequest("alice", "Password1"));

        assertThat(response.token()).isEqualTo("token");
        assertThat(response.user().username()).isEqualTo("alice");
    }

    @Test
    void login_userNotFound_throwsUnauthorized() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown", "x")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_inactiveUser_throwsUnauthorized() {
        User user = buildActiveUser();
        user.setIsActive(false);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "Password1")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void login_badPassword_throwsUnauthorized() {
        User user = buildActiveUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("alice", "wrong")))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ── refreshToken ─────────────────────────────────────────────────────────

    @Test
    void refreshToken_success() {
        User user = buildActiveUser();
        when(jwtUtil.extractUsername("old-token")).thenReturn("alice");
        when(jwtUtil.validateToken("old-token")).thenReturn(true);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("alice")).thenReturn("new-token");

        LoginResponse response = authService.refreshToken(new RefreshTokenRequest("old-token"));

        assertThat(response.token()).isEqualTo("new-token");
    }

    @Test
    void refreshToken_invalidToken_throwsUnauthorized() {
        when(jwtUtil.extractUsername(anyString())).thenThrow(new RuntimeException("bad token"));

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest("bad")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refreshToken_expiredToken_throwsUnauthorized() {
        when(jwtUtil.extractUsername("expired")).thenReturn("alice");
        when(jwtUtil.validateToken("expired")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest("expired")))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ── changePassword ───────────────────────────────────────────────────────

    @Test
    void changePassword_success() {
        User user = buildActiveUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1", "hashed")).thenReturn(true);
        when(passwordEncoder.matches("NewPass2", "hashed")).thenReturn(false); // not same as old
        when(passwordEncoder.encode("NewPass2")).thenReturn("new-hashed");

        authService.changePassword("alice", new ChangePasswordRequest("OldPass1", "NewPass2", "NewPass2"));

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("new-hashed");
        assertThat(user.getForcePasswordChange()).isFalse();
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsBadRequest() {
        User user = buildActiveUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() ->
                authService.changePassword("alice", new ChangePasswordRequest("wrong", "NewPass2", "NewPass2")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    @Test
    void changePassword_newPasswordMismatch_throwsBadRequest() {
        User user = buildActiveUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1", "hashed")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.changePassword("alice", new ChangePasswordRequest("OldPass1", "NewPass2", "Different")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    void changePassword_sameAsOld_throwsBadRequest() {
        User user = buildActiveUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1", "hashed")).thenReturn(true);
        when(passwordEncoder.matches("OldPass1", "hashed")).thenReturn(true); // new same as old

        assertThatThrownBy(() ->
                authService.changePassword("alice", new ChangePasswordRequest("OldPass1", "OldPass1", "OldPass1")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void changePassword_weakPassword_throwsBadRequest() {
        User user = buildActiveUser();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1", "hashed")).thenReturn(true);
        when(passwordEncoder.matches("weak", "hashed")).thenReturn(false);

        assertThatThrownBy(() ->
                authService.changePassword("alice", new ChangePasswordRequest("OldPass1", "weak", "weak")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least 8 characters");
    }

    // ── signup ───────────────────────────────────────────────────────────────

    @Test
    void signup_disabled_throwsBadRequest() {
        when(systemSettingService.isSignupEnabled()).thenReturn(false);

        assertThatThrownBy(() ->
                authService.signup(new SignupRequest("alice", "Password1", "Password1", "Alice", "a@b.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void signup_usernameTaken_throwsBadRequest() {
        when(systemSettingService.isSignupEnabled()).thenReturn(true);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() ->
                authService.signup(new SignupRequest("alice", "Password1", "Password1", "Alice", "a@b.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void signup_passwordMismatch_throwsBadRequest() {
        when(systemSettingService.isSignupEnabled()).thenReturn(true);
        when(userRepository.existsByUsername("alice")).thenReturn(false);

        assertThatThrownBy(() ->
                authService.signup(new SignupRequest("alice", "Password1", "Different1", "Alice", "a@b.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    void signup_weakPassword_throwsBadRequest() {
        when(systemSettingService.isSignupEnabled()).thenReturn(true);
        when(userRepository.existsByUsername("alice")).thenReturn(false);

        assertThatThrownBy(() ->
                authService.signup(new SignupRequest("alice", "weak", "weak", "Alice", "a@b.com")))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least 8 characters");
    }

    @Test
    void signup_success() {
        when(systemSettingService.isSignupEnabled()).thenReturn(true);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(defaultAccountTypeRepository.findAll()).thenReturn(Collections.emptyList());
        when(defaultCategoryRepository.findAll()).thenReturn(Collections.emptyList());

        User savedUser = buildActiveUser();
        savedUser.setId(1L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("alice")).thenReturn("token");

        LoginResponse response = authService.signup(
                new SignupRequest("alice", "Password1", "Password1", "Alice", "a@b.com"));

        assertThat(response.token()).isEqualTo("token");
        verify(notificationHelper).notify(eq(1L), any(), anyString(), anyString());
    }

    // ── isSignupEnabled ───────────────────────────────────────────────────────

    @Test
    void isSignupEnabled_delegatesToSystemSetting() {
        when(systemSettingService.isSignupEnabled()).thenReturn(true);
        assertThat(authService.isSignupEnabled()).isTrue();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private User buildActiveUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("hashed");
        user.setDisplayName("Alice");
        user.setEmail("alice@example.com");
        user.setIsActive(true);
        user.setForcePasswordChange(false);
        user.setCurrency("INR");
        user.setRole("USER");
        return user;
    }
}
