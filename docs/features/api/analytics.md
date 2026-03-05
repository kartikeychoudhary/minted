---
title: Analytics & Dashboard — API
feature: analytics
layer: api
package: com.minted.api.analytics, com.minted.api.dashboard, com.minted.api.dashboardconfig
routes:
  - GET /api/v1/analytics/summary
  - GET /api/v1/analytics/category-wise
  - GET /api/v1/analytics/trend
  - GET /api/v1/analytics/budget-summary
  - GET/POST/PUT/DELETE /api/v1/dashboard/cards
  - GET /api/v1/dashboard/cards/{id}/data
  - GET/PUT /api/v1/dashboard-config
migrations: V0_0_7, V0_0_36
related:
  - docs/features/api/transactions.md   (source data)
  - docs/features/api/budgets.md        (budget-summary)
  - docs/features/web/dashboard.md
  - docs/features/web/analytics.md
---

# Analytics & Dashboard — API

## Overview

Two subsystems:
1. **Analytics:** Read-only aggregate queries over `transactions` with date range, account, and category exclusion filters.
2. **Dashboard Config:** Per-user settings for chart color palette and excluded categories.

---

## Analytics Endpoints

All analytics endpoints accept:
- `startDate` / `endDate` (yyyy-MM-dd) — date range filter
- `accountId` (optional Long) — filter to a single account
- Excluded categories are applied automatically from `DashboardConfig`

| Method | Path | Returns |
|--------|------|---------|
| GET | `/api/v1/analytics/summary` | `{ totalIncome, totalExpense, netBalance, transactionCount }` |
| GET | `/api/v1/analytics/category-wise` | Array of `{ categoryName, amount, transactionCount }` |
| GET | `/api/v1/analytics/trend` | Array of `{ month, income, expense }` (6 months) |
| GET | `/api/v1/analytics/budget-summary` | Array of `BudgetSummaryResponse` for current month |

### Account Filter (v1.0.3)
All three main analytics endpoints (`summary`, `category-wise`, `trend`) accept `?accountId=N`. Implemented via JPQL pattern:
```java
(:accountId IS NULL OR t.account.id = :accountId)
```
A sentinel `List.of(-1L)` prevents empty `NOT IN` clause issues when no categories are excluded.

### Excluded Category Filtering
`AnalyticsServiceImpl` fetches excluded category IDs from `DashboardConfigService.getExcludedCategoryIds(userId)` and uses dedicated `*ExcludingCategories` repository query methods when exclusions are configured.

Additionally, transactions with `excludeFromAnalysis = true` are always excluded from analytics via:
```sql
AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL)
```

---

## Dashboard Cards

Configurable chart cards displayed on the home page.

### V0_0_7 — `dashboard_cards`
```sql
CREATE TABLE dashboard_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    chart_type ENUM('BAR','LINE','PIE','DOUGHNUT','AREA','STACKED_BAR') NOT NULL,
    x_axis_measure VARCHAR(50) NOT NULL,
    y_axis_measure VARCHAR(50) NOT NULL,
    filters JSON,
    position_order INT DEFAULT 0,
    width ENUM('HALF','FULL') DEFAULT 'HALF',
    user_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Dashboard Card Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/dashboard/cards` | Get all user dashboard cards |
| POST | `/api/v1/dashboard/cards` | Create card |
| PUT | `/api/v1/dashboard/cards/{id}` | Update card |
| DELETE | `/api/v1/dashboard/cards/{id}` | Delete card |
| PUT | `/api/v1/dashboard/cards/reorder` | Reorder cards |
| GET | `/api/v1/dashboard/cards/{id}/data?startDate=&endDate=` | Get chart data |

**Available X-axis:** `day`, `week`, `month`, `year`, `category`, `account`, `type`
**Available Y-axis:** `total_amount`, `count`, `average_amount`

---

## Dashboard Configuration

Per-user settings for chart colors and excluded categories.

### V0_0_36 — `dashboard_configurations`
```sql
CREATE TABLE dashboard_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    excluded_category_ids TEXT,   -- comma-separated category IDs
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Dashboard Config Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/dashboard-config` | Get user's config |
| PUT | `/api/v1/dashboard-config` | Save config |

### DTOs
| DTO | Fields |
|-----|--------|
| `DashboardConfigRequest` | excludedCategoryIds (List<Long>) |
| `DashboardConfigResponse` | excludedCategoryIds (List<Long>), static factory `from()` that parses comma-separated string |

> **Note:** Chart color palette is stored client-side in `localStorage` by the frontend `DashboardConfigService`. It is NOT persisted to the backend.
