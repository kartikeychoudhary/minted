# LOGGING.md — Structured Logging & Request Tracing

> **Added:** February 24, 2026

---

## Overview

The minted-api backend uses a 3-layer structured logging architecture with MDC (Mapped Diagnostic Context) enrichment. Every log line includes the request ID, authenticated user, HTTP method, and URI — enabling filtering and correlation when integrated with a log management system (ELK, Loki, Datadog, etc.).

---

## Architecture

```
Layer 1: MDC Filter + Logback Config    (infrastructure — every request)
Layer 2: Request/Response Interceptor    (HTTP boundary — timing + status)
Layer 3: Service-Level Business Logging  (domain events — CUD operations)
```

---

## Layer 1: MDC Filter + Logback Config

### MdcFilter (`common/filter/MdcFilter.java`)

A servlet filter ordered at `HIGHEST_PRECEDENCE` that runs before all other filters (including `JwtAuthFilter`). On every request it:

1. Generates a short `requestId` (first 8 chars of a UUID)
2. Captures `method`, `uri`, and `clientIp` (X-Forwarded-For aware)
3. Stores all values in SLF4J MDC
4. Clears MDC in a `finally` block (prevents thread-pool leaks)

### JwtAuthFilter MDC enrichment (`common/filter/JwtAuthFilter.java`)

After successful JWT validation, the filter adds `MDC.put("userId", username)`. This means all log lines after authentication include the authenticated user identity.

### SecurityConfig registration (`common/config/SecurityConfig.java`)

The `MdcFilter` is registered before `JwtAuthFilter` in the Spring Security filter chain:
```
MdcFilter → JwtAuthFilter → UsernamePasswordAuthenticationFilter
```

### MDC Keys Reference

| Key | Set By | Example | Filter Use |
|-----|--------|---------|------------|
| `requestId` | MdcFilter | `a3f8c1e2` | Correlate all logs in one request |
| `userId` | JwtAuthFilter | `admin` | Filter all actions by a user |
| `method` | MdcFilter | `POST` | Filter by HTTP method |
| `uri` | MdcFilter | `/api/v1/transactions` | Filter by endpoint |
| `clientIp` | MdcFilter | `192.168.1.5` | Track client origin |

### Logback Configuration (`src/main/resources/logback-spring.xml`)

**Log pattern (human-readable):**
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{requestId}] [%X{userId:-anonymous}] [%X{method} %X{uri}] %logger{36} - %msg%n
```

**Profiles:**

| Profile | App Level | Root Level | Format |
|---------|-----------|------------|--------|
| `dev` | DEBUG | INFO | Console (human-readable) |
| `prod` | INFO | WARN | JSON (for log aggregation) |
| `default` | INFO | INFO | Console (human-readable) |

**Example console output:**
```
2026-02-24 14:30:22.123 [http-nio-5500-exec-1] INFO  [a3f8c1e2] [admin] [POST /api/v1/transactions] c.m.a.t.s.TransactionServiceImpl - Transaction created: id=42, type=EXPENSE, amount=150.00
```

**Example JSON output (prod):**
```json
{"timestamp":"2026-02-24T14:30:22.123","level":"INFO","requestId":"a3f8c1e2","userId":"admin","method":"POST","uri":"/api/v1/transactions","clientIp":"192.168.1.5","logger":"c.m.a.t.s.TransactionServiceImpl","message":"Transaction created: id=42, type=EXPENSE, amount=150.00"}
```

---

## Layer 2: Request/Response Interceptor

### RequestLoggingInterceptor (`common/config/RequestLoggingInterceptor.java`)

A Spring `HandlerInterceptor` + `WebMvcConfigurer` that intercepts all `/api/**` requests and logs:

- **Entry:** `>> POST /api/v1/transactions`
- **Exit:** `<< POST /api/v1/transactions status=200 time=42ms`

This provides a unified view of request timing and HTTP status without needing per-controller logging.

---

## Layer 3: Service-Level Business Event Logging

All service implementations use Lombok `@Slf4j` for targeted business event logging. The policy:

- **Write operations (create/update/delete):** `log.info(...)` with entity ID and key attributes
- **Auth events:** `log.info(...)` for success, `log.warn(...)` for failure
- **Read operations:** No logging (too noisy, adds no value)
- **Error paths:** `log.error(...)` on catch blocks with context

### Services with logging

| Service | Events Logged |
|---------|---------------|
| `AuthServiceImpl` | Login success/failure, password change, user registration |
| `AccountServiceImpl` | Account created/updated/deleted |
| `AccountTypeServiceImpl` | AccountType created/updated/deleted |
| `TransactionServiceImpl` | Transaction created/updated/deleted (with type + amount) |
| `TransactionCategoryServiceImpl` | Category created/updated/deleted |
| `BudgetServiceImpl` | Budget created/updated/deleted |
| `RecurringTransactionServiceImpl` | Recurring transaction created/updated/deleted |
| `NotificationServiceImpl` | Bulk markAllAsRead/dismissAllRead with counts |
| `DashboardCardServiceImpl` | Card created/updated/deleted |
| `UserProfileServiceImpl` | Profile updated |
| `LlmConfigServiceImpl` | LLM config saved |
| `MerchantMappingServiceImpl` | Mapping created/updated/deleted |
| `SystemSettingServiceImpl` | Setting changed (key + value) |
| `DefaultListsServiceImpl` | Default category/account type created/deleted |
| `AnalyticsServiceImpl` | Debug-level summary query logging |
| `BulkImportServiceImpl` | (Already had @Slf4j — existing error logging enhanced by MDC) |
| `CreditCardStatementServiceImpl` | (Already had @Slf4j — existing error logging enhanced by MDC) |

---

## Files

### New files (3)

| File | Purpose |
|------|---------|
| `common/filter/MdcFilter.java` | MDC setup/cleanup on every request |
| `common/config/RequestLoggingInterceptor.java` | HTTP entry/exit timing logs |
| `src/main/resources/logback-spring.xml` | Log pattern, profiles, JSON format |

### Modified files (17)

| File | Change |
|------|--------|
| `common/filter/JwtAuthFilter.java` | `MDC.put("userId")` after JWT validation |
| `common/config/SecurityConfig.java` | Register MdcFilter before JwtAuthFilter |
| `auth/service/AuthServiceImpl.java` | `@Slf4j` + auth event logging |
| `account/service/AccountServiceImpl.java` | `@Slf4j` + CRUD logging |
| `account/service/AccountTypeServiceImpl.java` | `@Slf4j` + CRUD logging |
| `transaction/service/TransactionServiceImpl.java` | `@Slf4j` + CRUD logging |
| `transaction/service/TransactionCategoryServiceImpl.java` | `@Slf4j` + CRUD logging |
| `budget/service/BudgetServiceImpl.java` | `@Slf4j` + CRUD logging |
| `recurring/service/RecurringTransactionServiceImpl.java` | `@Slf4j` + CRUD logging |
| `notification/service/NotificationServiceImpl.java` | `@Slf4j` + bulk operation logging |
| `dashboard/service/DashboardCardServiceImpl.java` | `@Slf4j` + CRUD logging |
| `user/service/UserProfileServiceImpl.java` | `@Slf4j` + profile update logging |
| `llm/service/LlmConfigServiceImpl.java` | Config save logging (already had @Slf4j) |
| `llm/service/MerchantMappingServiceImpl.java` | `@Slf4j` + CRUD logging |
| `admin/service/SystemSettingServiceImpl.java` | `@Slf4j` + setting change logging |
| `admin/service/DefaultListsServiceImpl.java` | `@Slf4j` + CRUD logging |
| `analytics/service/AnalyticsServiceImpl.java` | `@Slf4j` + debug-level query logging |

---

## Adding Logging to New Services

When creating a new service, follow this pattern:

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewServiceImpl implements NewService {

    public Entity create(Request request, Long userId) {
        // ... business logic ...
        Entity saved = repository.save(entity);
        log.info("Entity created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    public void delete(Long id, Long userId) {
        // ... business logic ...
        repository.delete(entity);
        log.info("Entity deleted: id={}", id);
    }
}
```

The MDC context (`requestId`, `userId`, `method`, `uri`) is automatically included in every log line — no manual work needed.

---

## Future Enhancements

- **File appender:** Add rolling file output for log persistence
- **Log aggregation:** JSON prod output is ready for Filebeat/Fluentd → ELK/Loki
- **Correlation ID propagation:** Pass `requestId` to async tasks (CompletableFuture) via MDC context copy
- **Audit log table:** Persist critical business events to a database audit table
