---
title: Splits & Friends — API
feature: splits
layer: api
package: com.minted.api.friend, com.minted.api.split
routes:
  - GET/POST/PUT/DELETE /api/v1/friends
  - POST/DELETE /api/v1/friends/{id}/avatar
  - GET/POST/PUT/DELETE /api/v1/splits
  - GET /api/v1/splits/summary
  - GET /api/v1/splits/balances
  - POST /api/v1/splits/settle
  - GET /api/v1/splits/friend/{friendId}/shares
migrations: V0_0_31 (friends, split_transactions, split_shares), V0_0_39 (avatar columns for friends)
related:
  - docs/features/api/transactions.md   (isSplit flag on TransactionResponse)
  - docs/features/api/notifications.md  (settlement notifications)
  - docs/features/web/splits.md
---

# Splits & Friends — API

## Overview

Two sub-features: **Friends** (user's contact list for splits) and **Split Transactions** (divide an expense among friends). Friends support soft-delete and avatar images. Splits support EQUAL, UNEQUAL, and SHARE-based division.

---

## Database

### V0_0_31 — `friends`, `split_transactions`, `split_shares`

**`friends`**
| Column | Notes |
|--------|-------|
| id | BIGINT PK |
| user_id | FK → users (CASCADE) |
| name | VARCHAR — unique per user (`uk_user_friend_name`) |
| email | nullable |
| phone | nullable |
| avatar_color | VARCHAR default `#6366f1` |
| is_active | BOOLEAN — soft delete |

**`split_transactions`**
| Column | Notes |
|--------|-------|
| user_id | FK → users (CASCADE) |
| transaction_id | FK → transactions (SET NULL) — nullable, source transaction |
| description | VARCHAR |
| category_name | VARCHAR — stored as string, not FK |
| total_amount | DECIMAL(15,2) |
| split_type | VARCHAR(30) — EQUAL / UNEQUAL / SHARE |
| transaction_date | DATE |
| is_settled | BOOLEAN — denormalized aggregate |

**`split_shares`**
| Column | Notes |
|--------|-------|
| split_transaction_id | FK → split_transactions (CASCADE) |
| friend_id | FK → friends (SET NULL) — `null` means "Me" (authenticated user) |
| share_amount | DECIMAL(15,2) |
| share_percentage | DECIMAL nullable |
| is_payer | BOOLEAN |
| is_settled | BOOLEAN |
| settled_at | TIMESTAMP nullable |

### V0_0_39 — Avatar columns for `friends`
Adds: `avatar_data` (LONGBLOB), `avatar_content_type` (VARCHAR 50), `avatar_file_size` (INT), `avatar_updated_at` (TIMESTAMP NULL)

---

## Business Rules

**Friend soft delete:** `DELETE /friends/{id}` sets `isActive=false`. `create()` checks for soft-deleted friend with same name first (restores instead of duplicating).

**SplitType behavior:**
- `EQUAL` — divides `totalAmount` evenly; remainder goes to first share
- `UNEQUAL` — share amounts provided explicitly, must sum to `totalAmount`
- `SHARE` — share percentages provided, amounts calculated from percentage

**Settlement:** `settleFriend()` marks all unsettled shares for a friend as settled. If all shares are settled, `is_settled` on the parent `split_transaction` is set to `true`. Fires notification via `NotificationHelper`.

**Transaction integration:** `TransactionResponse.isSplit` field — `TransactionServiceImpl` queries `SplitTransactionRepository.findSourceTransactionIdsByUserId()` to build a `Set<Long>` of source transaction IDs.

---

## Endpoints

### Friends (`/api/v1/friends`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/friends` | List all active friends |
| GET | `/api/v1/friends/{id}` | Get friend by ID |
| POST | `/api/v1/friends` | Create friend (or restore soft-deleted) |
| PUT | `/api/v1/friends/{id}` | Update friend |
| DELETE | `/api/v1/friends/{id}` | Soft delete friend |
| POST | `/api/v1/friends/{id}/avatar` | Upload avatar (multipart, max 2MB, image/*) |
| DELETE | `/api/v1/friends/{id}/avatar` | Remove avatar |

### Splits (`/api/v1/splits`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/splits` | List all splits for user |
| GET | `/api/v1/splits/{id}` | Get split by ID |
| POST | `/api/v1/splits` | Create split |
| PUT | `/api/v1/splits/{id}` | Update split (clears + rebuilds shares via orphanRemoval) |
| DELETE | `/api/v1/splits/{id}` | Delete split |
| GET | `/api/v1/splits/summary` | Total owed to user / user owes (JPQL aggregation) |
| GET | `/api/v1/splits/balances` | Net balance per friend (GROUP BY) |
| POST | `/api/v1/splits/settle` | Settle all unsettled shares with a friend |
| GET | `/api/v1/splits/friend/{friendId}/shares` | Export unsettled shares for a friend |

---

## DTOs

### Friend DTOs
| DTO | Fields |
|-----|--------|
| `FriendRequest` | name (@NotBlank), email, phone, avatarColor |
| `FriendResponse` | id, name, email, phone, avatarColor, isActive, avatarBase64 (data URI or null) |

### Split DTOs
| DTO | Fields |
|-----|--------|
| `SplitTransactionRequest` | description, categoryName, totalAmount, splitType, transactionDate, transactionId (nullable), shares |
| `SplitShareRequest` | friendId (null = Me), shareAmount, sharePercentage, isPayer |
| `SplitTransactionResponse` | id, description, categoryName, totalAmount, splitType, transactionDate, isSettled, shares |
| `SplitShareResponse` | id, friend (FriendResponse or null), shareAmount, sharePercentage, isPayer, isSettled, settledAt |
| `BalanceSummaryResponse` | totalOwedToUser, totalUserOwes, netBalance |
| `FriendBalanceResponse` | friend (FriendResponse), netBalance, unsettledCount |
