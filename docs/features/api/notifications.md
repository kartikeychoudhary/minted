---
title: Notifications — API
feature: notifications
layer: api
package: com.minted.api.notification
routes:
  - GET    /api/v1/notifications
  - GET    /api/v1/notifications/unread-count
  - PUT    /api/v1/notifications/{id}/read
  - PUT    /api/v1/notifications/read-all
  - DELETE /api/v1/notifications/{id}
  - DELETE /api/v1/notifications/read
migrations: V0_0_22
related:
  - docs/features/api/auth.md         (login/signup fire notifications)
  - docs/features/api/import.md       (job completion notifications)
  - docs/features/api/statements.md   (LLM parse notifications)
  - docs/features/api/splits.md       (settlement notifications)
  - docs/features/web/notifications.md
---

# Notifications — API

## Overview

Per-user in-app notification system. Any backend service can create notifications via `NotificationHelper`. Dismissed notifications are hard-deleted. Frontend polls unread count every 30s.

---

## Database

### V0_0_22 — `notifications`
```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(30) NOT NULL,        -- INFO, SUCCESS, WARNING, ERROR, SYSTEM
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

`type` uses `VARCHAR(30)` (not MySQL ENUM). Entity uses `@Enumerated(EnumType.STRING)` + `@JdbcTypeCode(Types.VARCHAR)` (required for Hibernate 6.x + MySQL VARCHAR columns).

---

## NotificationType Enum

`com.minted.api.notification.enums.NotificationType`

Values: `INFO`, `SUCCESS`, `WARNING`, `ERROR`, `SYSTEM`

---

## NotificationHelper (Shared Utility)

**File:** `com.minted.api.notification.service.NotificationHelper` — Concrete `@Component` (not interface+impl).

```java
// Inject and use in any service
@Autowired
private NotificationHelper notificationHelper;

notificationHelper.notify(userId, NotificationType.SUCCESS, "Title", "Message body");
notificationHelper.notify(userId, NotificationType.INFO, "Title", "Message", actionUrl);
notificationHelper.notifyAll(NotificationType.SYSTEM, "Title", "Broadcast message");
```

**Key design decisions:**
- `@Transactional(propagation = Propagation.REQUIRES_NEW)` — runs in its own transaction; failures never roll back the caller
- Exceptions are caught + logged but **never rethrown** — notifications are side-effects, not critical path

---

## Repository Methods

| Method | Return | Purpose |
|--------|--------|---------|
| `findByUserIdOrderByCreatedAtDesc(userId, Pageable)` | `Page<Notification>` | Paginated list |
| `countByUserIdAndIsReadFalse(userId)` | `long` | Badge count |
| `findByIdAndUserId(id, userId)` | `Optional<Notification>` | Ownership check |
| `markAllAsReadByUserId(userId)` | `int` | `@Modifying` bulk update |
| `deleteByIdAndUserId(id, userId)` | `int` | `@Modifying` hard delete |
| `deleteAllReadByUserId(userId)` | `int` | `@Modifying` bulk delete |

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/notifications?page=&size=` | Paginated list (ordered newest first) |
| GET | `/api/v1/notifications/unread-count` | Count for header badge |
| PUT | `/api/v1/notifications/{id}/read` | Mark single notification as read |
| PUT | `/api/v1/notifications/read-all` | Mark all as read (bulk) |
| DELETE | `/api/v1/notifications/{id}` | Hard delete single notification |
| DELETE | `/api/v1/notifications/read` | Hard delete all read notifications |

---

## DTOs

| DTO | Type | Fields |
|-----|------|--------|
| `NotificationResponse` | record | id, type (String), title, message, isRead, createdAt. Factory: `from(Notification)` |
