---
title: Transactions & Categories — API
feature: transactions
layer: api
package: com.minted.api.transaction
routes:
  - GET/POST/PUT/DELETE /api/v1/transactions
  - DELETE /api/v1/transactions/bulk
  - PUT    /api/v1/transactions/bulk/category
  - GET/POST/PUT/DELETE /api/v1/categories
migrations: V0_0_4, V0_0_5, V0_0_35
related:
  - docs/features/api/analytics.md    (analytics queries filter on transactions)
  - docs/features/api/splits.md       (isSplit flag on TransactionResponse)
  - docs/features/api/import.md       (bulk import creates transactions)
  - docs/features/web/transactions.md (AG Grid frontend)
---

# Transactions & Categories — API

## Overview

Core feature. Transactions are INCOME, EXPENSE, or TRANSFER entries linked to an account and category. Categories are user-defined with type, icon, and color. Transactions support bulk operations and an `excludeFromAnalysis` flag.

---

## Database

### V0_0_4 — `transaction_categories`
```sql
CREATE TABLE transaction_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME','EXPENSE','TRANSFER') NOT NULL,
    icon VARCHAR(50),       -- PrimeNG: "pi pi-shopping-cart"
    color VARCHAR(7),
    parent_id BIGINT,       -- nullable self-reference for sub-categories
    user_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES transaction_categories(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### V0_0_5 — `transactions`
```sql
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    type ENUM('INCOME','EXPENSE','TRANSFER') NOT NULL,
    description VARCHAR(500),
    notes TEXT,
    transaction_date DATE NOT NULL,
    account_id BIGINT NOT NULL,
    to_account_id BIGINT,              -- TRANSFER destination
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    tags VARCHAR(500),                 -- comma-separated
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (to_account_id) REFERENCES accounts(id),
    FOREIGN KEY (category_id) REFERENCES transaction_categories(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_user_date (user_id, transaction_date),
    INDEX idx_user_account (user_id, account_id)
);
```

### V0_0_35 — `exclude_from_analysis`
```sql
ALTER TABLE transactions ADD COLUMN exclude_from_analysis BOOLEAN DEFAULT FALSE;
```

---

## Business Rules

- **Balance updates:** Creating a transaction adjusts `accounts.balance` (INCOME adds, EXPENSE/TRANSFER subtracts). Deleting reverses the change.
- **TRANSFER:** Requires `toAccountId`. Subtracts from `accountId`, adds to `toAccountId`.
- **`isSplit` flag:** `TransactionResponse.isSplit` is a computed boolean — `TransactionServiceImpl` queries `SplitTransactionRepository.findSourceTransactionIdsByUserId()` and checks `splitIds.contains(id)` in every read method and `update()`.
- **Exclude from analysis:** `excludeFromAnalysis = true` causes transaction to be skipped by all analytics queries.
- **Data ownership:** All queries filter by `user_id`.

---

## Transaction Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/transactions` | Paginated + filtered list |
| GET | `/api/v1/transactions/{id}` | Get single transaction |
| POST | `/api/v1/transactions` | Create transaction |
| PUT | `/api/v1/transactions/{id}` | Update transaction |
| DELETE | `/api/v1/transactions/{id}` | Delete transaction |
| DELETE | `/api/v1/transactions/bulk` | Bulk delete `{ "ids": [1,2,3] }` |
| PUT | `/api/v1/transactions/bulk/category` | Bulk update category `{ "ids": [...], "categoryId": N }` |

### Query Parameters (GET `/transactions`)

| Param | Type | Description |
|-------|------|-------------|
| `page` | int | 0-indexed page (default 0) |
| `size` | int | Page size (default 20) |
| `startDate` | yyyy-MM-dd | Filter from date |
| `endDate` | yyyy-MM-dd | Filter to date |
| `period` | String | LAST_WEEK / LAST_MONTH / THIS_MONTH / LAST_3_MONTHS / LAST_6_MONTHS / LAST_YEAR / CUSTOM |
| `accountId` | Long | Filter by account |
| `categoryId` | Long | Filter by category |
| `type` | String | INCOME / EXPENSE / TRANSFER |
| `search` | String | Search in description |
| `sortBy` | String | Field name (default: transactionDate) |
| `sortDir` | String | ASC / DESC (default: DESC) |

---

## Category Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/categories` | List all active categories |
| GET | `/api/v1/categories?type=EXPENSE` | Filter by type |
| POST | `/api/v1/categories` | Create category |
| PUT | `/api/v1/categories/{id}` | Update category |
| DELETE | `/api/v1/categories/{id}` | Soft delete category |

---

## DTOs

### Transaction
| DTO | Key Fields |
|-----|-----------|
| `TransactionRequest` | amount (@Positive), type, description, transactionDate, accountId, toAccountId, categoryId, notes, tags, isRecurring, excludeFromAnalysis |
| `TransactionResponse` | id, amount, type, description, transactionDate, account (AccountResponse), toAccount, category (CategoryResponse), isSplit, excludeFromAnalysis, createdAt |

### Category
| DTO | Key Fields |
|-----|-----------|
| `TransactionCategoryRequest` | name (@NotBlank), type (@NotNull), icon, color, parentId |
| `TransactionCategoryResponse` | id, name, type, icon, color, parentId, isActive |

---

## Default Categories

Seeded from `default_categories` table on new user registration via `DefaultListsService`. Current defaults (V0_0_8, V0_0_30):
- INCOME: Salary, Freelance, Interest
- EXPENSE: Food & Dining, Groceries, Transport, Utilities, Entertainment, Shopping, Health, Education, Rent, EMI
- TRANSFER: Transfer
