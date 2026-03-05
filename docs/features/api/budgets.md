---
title: Budgets — API
feature: budgets
layer: api
package: com.minted.api.budget
routes:
  - GET/POST/PUT/DELETE /api/v1/budgets
  - GET /api/v1/budgets/summary
  - GET /api/v1/analytics/budget-summary
migrations: V0_0_6
related:
  - docs/features/api/transactions.md   (spend tracking by category)
  - docs/features/api/analytics.md      (budget-summary endpoint)
  - docs/features/web/settings.md       (Budgets tab)
---

# Budgets — API

## Overview

Per-user monthly budgets linked to a transaction category. One budget per user/month/year/category combination. Analytics endpoint computes utilization (spent vs budgeted) for the current month.

---

## Database

### V0_0_6 — `budgets`
```sql
CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    month INT NOT NULL,          -- 1–12
    year INT NOT NULL,
    category_id BIGINT,          -- nullable: null = track ALL expenses
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES transaction_categories(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_budget_month_year_cat (user_id, month, year, category_id)
);
```

---

## Business Rules

- `category_id = NULL` means the budget tracks all EXPENSE transactions (not filtered by category).
- Unique constraint prevents duplicate budgets for the same month/year/category combo per user.
- Spend calculation: queries `transactions` table for EXPENSE type transactions in the given month/year for the budget's category (or all categories if `category_id = null`).

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/budgets` | List all budgets for user |
| GET | `/api/v1/budgets?month=2&year=2026` | Filter by month and year |
| POST | `/api/v1/budgets` | Create budget |
| PUT | `/api/v1/budgets/{id}` | Update budget |
| DELETE | `/api/v1/budgets/{id}` | Delete budget |
| GET | `/api/v1/budgets/summary?month=X&year=Y` | Budget vs actual for month |
| GET | `/api/v1/analytics/budget-summary` | Current-month utilization (used by analytics page) |

---

## DTOs

| DTO | Fields |
|-----|--------|
| `BudgetRequest` | name (@NotBlank), amount (@Positive), month (1-12), year, categoryId (nullable) |
| `BudgetResponse` | id, name, amount, month, year, category (CategoryResponse or null) |
| `BudgetSummaryResponse` | budgetId, budgetName, categoryName, budgetedAmount, spentAmount, remainingAmount, utilizationPercent |

---

## Analytics Budget Summary

`GET /api/v1/analytics/budget-summary`

Returns current-month budgets with utilization. Used by the Analytics Overview page's Budget Status card.

Implementation (`AnalyticsServiceImpl.getBudgetSummary()`):
1. Fetch all budgets for the current month/year.
2. Query `transactions` for EXPENSE spend grouped by category for the same month.
3. Map category spend to each budget. Budgets with `category_id = null` sum ALL expense spend.
4. Calculate `utilizationPercent = (spentAmount / budgetedAmount) * 100`.
