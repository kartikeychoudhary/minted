---
title: Infrastructure & Cross-Cutting Concerns — API
feature: infrastructure
layer: api
package: com.minted.api.common
routes: []
migrations: V0_0_1 (users seed), V0_0_16 (job tables), V0_0_17 (system_settings)
related:
  - docs/features/api/auth.md         (JWT filter chain)
  - docs/features/api/admin.md        (SystemSettings)
---

# Infrastructure & Cross-Cutting Concerns — API

## Overview

Shared components that span all features: project setup, environment configuration, security, error handling, data ownership, and structured logging.

---

## Project Setup

### Gradle Dependencies (`build.gradle`)
```groovy
dependencies {
    // Core
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // Database
    runtimeOnly 'com.mysql:mysql-connector-j'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

    // Encryption
    implementation 'com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5'

    // API Docs
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'

    // PDF parsing (statement feature)
    implementation 'org.apache.pdfbox:pdfbox:3.0.2'

    // Utility
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `MINTED_DB_HOST` | MySQL host | `localhost` |
| `MINTED_DB_PORT` | MySQL port | `3306` |
| `MINTED_DB_NAME` | Database name | `minted_db` |
| `MINTED_DB_USER` | DB username | `minted_user` |
| `MINTED_DB_PASSWORD` | DB password (encrypted) | `ENC(encrypted_value)` |
| `MINTED_JWT_SECRET` | JWT signing key (min 256-bit) | `base64-encoded-secret` |
| `MINTED_JWT_EXPIRATION` | Token expiry in ms | `86400000` (24h) |
| `MINTED_JASYPT_PASSWORD` | Jasypt encryption salt | `your_salt_password` |
| `MINTED_CORS_ORIGINS` | Allowed CORS origins | `http://localhost:4200` |
| `MINTED_ADMIN_PASSWORD` | Initial admin password (Jasypt encrypted) | `ENC(encrypted_value)` |

### Key `application.properties` Settings
```properties
server.port=5500
spring.jpa.hibernate.ddl-auto=validate   # Flyway manages schema — Hibernate only validates
spring.flyway.locations=classpath:db/migration
app.jwt.secret=${MINTED_JWT_SECRET}
app.jwt.expiration=${MINTED_JWT_EXPIRATION:86400000}
app.cors.allowed-origins=${MINTED_CORS_ORIGINS:http://localhost:4200}
jasypt.encryptor.algorithm=PBEWithMD5AndDES
jasypt.encryptor.iv-generator-classname=org.jasypt.iv.NoIvGenerator
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui
```

---

## Package Structure

Feature-based modules — each domain owns its controller, DTO, entity, repository, and service:

```
com.minted.api/
├── MintedApiApplication.java
├── common/
│   ├── config/       # SecurityConfig, SchedulerConfig, DataInitializer, RequestLoggingInterceptor
│   ├── exception/    # Custom exceptions + GlobalExceptionHandler
│   ├── filter/       # JwtAuthFilter, MdcFilter
│   └── util/         # JwtUtil
├── auth/             # Authentication & signup
├── user/             # User entity & profile (avatar)
├── account/          # Accounts + Account Types
├── transaction/      # Transactions + Categories
├── budget/           # Budget management
├── dashboard/        # Dashboard cards & charts
├── analytics/        # Analytics & reporting
├── recurring/        # Recurring transactions
├── notification/     # Notification system
├── job/              # Job execution framework (shared)
├── bulkimport/       # CSV bulk import
├── statement/        # Credit card statement parser
├── llm/              # LLM config, models, merchant mappings
├── friend/           # Friends list
├── split/            # Split transactions
└── admin/            # Admin management, defaults, system settings
```

**Internal module structure:**
```
<module>/
├── controller/   # REST endpoints
├── dto/          # Request/Response DTOs
├── entity/       # JPA entities
├── enums/        # Enumerations
├── job/          # Scheduled jobs (if any)
├── repository/   # Spring Data JPA repositories
└── service/      # Interface + implementation
```

---

## Security Configuration

**Public routes:** `/api/v1/auth/**`, `/swagger-ui/**`, `/api-docs/**`
**All other `/api/**`:** require valid JWT
**CSRF:** disabled (stateless)
**Session:** `STATELESS`
**CORS:** origins from `MINTED_CORS_ORIGINS` env var

**Filter chain (execution order):**
1. `MdcFilter` (`HIGHEST_PRECEDENCE`) — sets MDC keys for structured logging
2. `JwtAuthFilter` — validates Bearer token, sets `SecurityContext`
3. Spring Security filter chain

**Token details:**
- Algorithm: HMAC-SHA512
- Signing key: `MINTED_JWT_SECRET` (min 256-bit)
- Expiry: `MINTED_JWT_EXPIRATION` (default 86400000ms = 24h)

---

## Error Handling

**`GlobalExceptionHandler`** (`@RestControllerAdvice`) — all API errors return:
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "details": ["amount must be greater than 0"],
  "timestamp": "2026-02-15T10:30:00Z",
  "path": "/api/v1/transactions"
}
```

**Custom exceptions → HTTP status:**
| Exception | Status |
|-----------|--------|
| `ResourceNotFoundException` | 404 |
| `DuplicateResourceException` | 409 |
| `UnauthorizedException` | 401 |
| `ForbiddenException` | 403 |
| `BadRequestException` | 400 |
| `ForcePasswordChangeException` | 403 (special frontend flag) |

---

## Data Ownership

Every query MUST filter by the authenticated user's ID. No user should ever see another user's data.

```java
public Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return ((UserPrincipal) auth.getPrincipal()).getId();
}
```

All repositories use `findByIdAndUserId()` pattern for single-record lookups (prevents cross-user access).

---

## Structured Logging

### MDC Keys (set automatically on every request)

| Key | Set By | Example |
|-----|--------|---------|
| `requestId` | `MdcFilter` | `a3f8c1e2` |
| `userId` | `JwtAuthFilter` | `admin` |
| `method` | `MdcFilter` | `POST` |
| `uri` | `MdcFilter` | `/api/v1/transactions` |
| `clientIp` | `MdcFilter` | `192.168.1.5` |

### Log Pattern
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{requestId}] [%X{userId:-anonymous}] [%X{method} %X{uri}] %logger{36} - %msg%n
```

### Logback Profiles

| Profile | App Level | Root Level | Output |
|---------|-----------|------------|--------|
| `dev` | DEBUG | INFO | Console (human-readable) |
| `prod` | INFO | WARN | JSON (for log aggregation) |
| `default` | INFO | INFO | Console |

### Logging Policy

- **Write operations:** `log.info(...)` with entity ID and key attributes
- **Auth events:** `log.info(...)` for success, `log.warn(...)` for failures
- **Read operations:** No logging (noise reduction)
- **Analytics queries:** `log.debug(...)` only (dev profile)
- **Error paths:** `log.error(...)` with context

All service implementations use Lombok `@Slf4j`. MDC context is automatically included.

### New Service Pattern
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class NewServiceImpl implements NewService {
    public Entity create(Request request, Long userId) {
        Entity saved = repository.save(entity);
        log.info("Entity created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }
}
```

---

## Hibernate 6.x + MySQL VARCHAR Enum Pattern

For entities that store enums in VARCHAR columns (not MySQL ENUM type):
```java
@Enumerated(EnumType.STRING)
@JdbcTypeCode(Types.VARCHAR)
private MyEnum status;
```

This is required in: `BulkImport.status`, `CreditCardStatement.status`, `SplitTransaction.splitType`, `Notification.type`.
