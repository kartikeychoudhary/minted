package com.minted.api.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    // 64-char secret -> 512-bit key (satisfies HMAC-SHA512 requirement)
    private static final String SECRET = "minted-test-secret-key-that-is-at-least-64-characters-long-1234";
    private static final long EXPIRATION = 86_400_000L; // 24h

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", EXPIRATION);
    }

    // ── generateToken ────────────────────────────────────────────────────────

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("alice");
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void generateToken_withClaims_returnsNonNullToken() {
        Map<String, Object> claims = Map.of("role", "USER");
        String token = jwtUtil.generateToken("alice", claims);
        assertThat(token).isNotNull().isNotEmpty();
    }

    // ── extractUsername ──────────────────────────────────────────────────────

    @Test
    void extractUsername_returnsCorrectSubject() {
        String token = jwtUtil.generateToken("bob");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("bob");
    }

    // ── extractExpiration ────────────────────────────────────────────────────

    @Test
    void extractExpiration_returnsDateInFuture() {
        String token = jwtUtil.generateToken("carol");
        assertThat(jwtUtil.extractExpiration(token)).isInTheFuture();
    }

    // ── validateToken(token, userDetails) ────────────────────────────────────

    @Test
    void validateToken_withMatchingUsername_returnsTrue() {
        String token = jwtUtil.generateToken("dave");
        UserDetails userDetails = buildUserDetails("dave");
        assertThat(jwtUtil.validateToken(token, userDetails)).isTrue();
    }

    @Test
    void validateToken_withDifferentUsername_returnsFalse() {
        String token = jwtUtil.generateToken("dave");
        UserDetails userDetails = buildUserDetails("eve");
        assertThat(jwtUtil.validateToken(token, userDetails)).isFalse();
    }

    // ── validateToken(token) ─────────────────────────────────────────────────

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("frank");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_malformedToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("not.a.jwt")).isFalse();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        // Override expiration to -1ms so token is already expired
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        String expiredToken = jwtUtil.generateToken("grace");
        assertThat(jwtUtil.validateToken(expiredToken)).isFalse();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private UserDetails buildUserDetails(String username) {
        return new User(username, "password", Collections.emptyList());
    }
}
