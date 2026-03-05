---
title: Accounts & Account Types — API
feature: accounts
layer: api
package: com.minted.api.account
routes:
  - GET/POST/PUT/DELETE /api/v1/accounts
  - GET/POST/PUT/DELETE /api/v1/account-types
  - PATCH /api/v1/account-types/{id}/toggle
migrations: V0_0_2, V0_0_3
related:
  - docs/features/api/transactions.md   (account FK on transactions)
  - docs/features/web/settings.md       (Accounts + Account Types tabs)
---

# Accounts & Account Types — API

## Overview

Accounts represent the user's actual financial accounts (bank, credit card, wallet). Account Types are user-defined categories for grouping accounts (e.g., "Bank Account", "Credit Card"). Both support soft delete — deleted records remain in the DB with `is_active = false`.

---

## Database

### V0_0_2 — `account_types`
```sql
CREATE TABLE account_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(50),           -- PrimeNG icon class: "pi pi-building-columns"
    user_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### V0_0_3 — `accounts`
```sql
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    account_type_id BIGINT NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'INR',
    color VARCHAR(7),
    icon VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_type_id) REFERENCES account_types(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## Business Rules

- **Account soft delete:** `DELETE /accounts/{id}` sets `is_active = false`. `getAllByUserId()` only returns `is_active = true` records.
- **Account restore on create:** `AccountServiceImpl.create()` checks for soft-deleted accounts with the same name — restores them instead of creating duplicates.
- **Account Type soft delete:** Same pattern. Soft-deleted types shown in Settings UI with strikethrough + "Undo" button. `toggleActive()` endpoint restores them.
- **Balance updates:** Creating INCOME/EXPENSE/TRANSFER transactions adjusts `accounts.balance` via `AccountService.updateBalance()`. Deleting a transaction reverses the balance change.
- **Data ownership:** All queries filter by `user_id = :userId` from the JWT security context.

---

## Endpoints

### Account Types

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/account-types` | List all active account types for user |
| POST | `/api/v1/account-types` | Create new account type |
| PUT | `/api/v1/account-types/{id}` | Update name/description/icon |
| DELETE | `/api/v1/account-types/{id}` | Soft delete (sets `is_active = false`) |
| PATCH | `/api/v1/account-types/{id}/toggle` | Toggle active/inactive (restore) |

### Accounts

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/accounts` | List all active accounts for user |
| GET | `/api/v1/accounts/{id}` | Get account by ID |
| POST | `/api/v1/accounts` | Create account (or restore soft-deleted) |
| PUT | `/api/v1/accounts/{id}` | Update account |
| DELETE | `/api/v1/accounts/{id}` | Soft delete account |

---

## DTOs

### AccountType
| DTO | Fields |
|-----|--------|
| `AccountTypeRequest` | name (@NotBlank, max 50), description, icon |
| `AccountTypeResponse` | id, name, description, icon, isActive, createdAt |

### Account
| DTO | Fields |
|-----|--------|
| `AccountRequest` | name (@NotBlank, max 100), accountTypeId (@NotNull), balance, currency (3-char), color, icon |
| `AccountResponse` | id, name, accountType (AccountTypeResponse), balance, currency, color, icon, isActive |

---

## Default Data

On new user registration, `DefaultListsService` seeds account types from `default_account_types` table (managed by admin). Current defaults:
- Bank Account (`pi pi-building-columns`)
- Credit Card (`pi pi-credit-card`)
- Wallet (`pi pi-wallet`)
- Investment (`pi pi-chart-line`)
