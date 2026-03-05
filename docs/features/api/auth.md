---
title: Authentication — API
feature: auth
layer: api
package: com.minted.api.auth
routes:
  - POST /api/v1/auth/login
  - POST /api/v1/auth/refresh
  - PUT  /api/v1/auth/change-password
  - POST /api/v1/auth/signup
  - GET  /api/v1/auth/signup-enabled
migrations: V0_0_1
related:
  - docs/features/api/admin.md      (user management, signup toggle)
  - docs/features/web/auth.md       (frontend login/signup components)
---

# Authentication — API

## Overview

JWT-based stateless authentication. Default admin user created via Flyway seed. Public signup is admin-toggleable. Force-password-change flow on first login.

---

## Database

### V0_0_1 — `users` table
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,         -- BCrypt
    display_name VARCHAR(100),
    email VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    force_password_change BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- Default admin (password: "admin", must change on first login)
INSERT INTO users (username, password, display_name, force_password_change)
VALUES ('admin', '$2a$10$BCRYPT_HASH', 'Administrator', TRUE);
```

---

## JWT Flow

1. `POST /auth/login` → validates BCrypt password → returns `{ token, refreshToken, forcePasswordChange }`
2. If `forcePasswordChange = true`, frontend redirects to change-password.
3. All subsequent requests: `Authorization: Bearer <token>` header.
4. `JwtAuthFilter` validates token on every request, adds `userId` to SLF4J MDC.

**Token signing:** HMAC-SHA512, key from `MINTED_JWT_SECRET` env var (min 256-bit), expiry from `MINTED_JWT_EXPIRATION` (default 86400000ms = 24h).

---

## Security Configuration

- **Public routes:** `/api/v1/auth/**`, `/swagger-ui/**`, `/api-docs/**`
- **All other `/api/**`:** require valid JWT
- CSRF: disabled (stateless)
- Session: `STATELESS`
- CORS: origins from `MINTED_CORS_ORIGINS` env var
- **Filter chain:** `MdcFilter (HIGHEST_PRECEDENCE)` → `JwtAuthFilter` → Spring Security

---

## Public Signup Flow

1. Frontend checks `GET /auth/signup-enabled`.
2. User submits `POST /auth/signup`.
3. Backend validates: signup enabled, username unique, passwords match, strength rules.
4. Creates user (`role=USER`, `forcePasswordChange=false`, `isActive=true`), seeds default account types + categories via `DefaultListsService`.
5. Returns `LoginResponse` — frontend auto-logs in.

Password strength regex: min 8 chars, at least one uppercase, one digit.

---

## Endpoints

### POST `/api/v1/auth/login`
```json
// Request
{ "username": "admin", "password": "admin" }

// Response 200
{
  "token": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": { "id": 1, "username": "admin", "displayName": "Administrator", "forcePasswordChange": true }
}

// Error 401
{ "status": 401, "error": "UNAUTHORIZED", "message": "Invalid username or password" }
```

### POST `/api/v1/auth/signup`
```json
// Request
{ "username": "alice", "password": "Pass1234", "confirmPassword": "Pass1234", "displayName": "Alice", "email": "alice@example.com" }

// Response 200 — same as login response
```

### PUT `/api/v1/auth/change-password`
```json
// Request
{ "currentPassword": "admin", "newPassword": "NewPass1", "confirmPassword": "NewPass1" }
// Response 200: { "message": "Password changed successfully" }
// Sets force_password_change = false
```

### GET `/api/v1/auth/signup-enabled`
```json
// Response 200
{ "enabled": false }
```

---

## DTOs

| DTO | Fields |
|-----|--------|
| `LoginRequest` | username, password |
| `LoginResponse` | token, refreshToken, tokenType, expiresIn, user (UserResponse) |
| `ChangePasswordRequest` | currentPassword, newPassword, confirmPassword |
| `SignupRequest` | username, password, confirmPassword, displayName, email |
| `UserResponse` | id, username, displayName, email, forcePasswordChange, role, avatarBase64 |

---

## Custom Exceptions

| Exception | HTTP | When |
|-----------|------|------|
| `UnauthorizedException` | 401 | Bad credentials |
| `ForcePasswordChangeException` | 403 | Token valid but password change required |
| `BadRequestException` | 400 | Validation failures (passwords don't match, weak password) |
| `DuplicateResourceException` | 409 | Username already exists |
