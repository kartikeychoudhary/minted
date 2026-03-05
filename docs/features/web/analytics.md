---
title: Analytics — Web
feature: analytics
layer: web
module: AnalyticsModule (lazy, /analytics)
components:
  - AnalyticsOverviewComponent
related:
  - docs/features/api/analytics.md
  - docs/features/api/budgets.md
  - docs/features/web/dashboard.md
---

# Analytics — Web

## Overview

Single-page analytics overview with summary stats, spending by category, 6-month trend chart, recent transactions, recurring payments preview, and budget utilization.

---

## Module

`AnalyticsModule` — lazy-loaded at `/analytics`.

---

## AnalyticsOverviewComponent

Unified overview page combining multiple data sources.

### Sections

1. **Summary Stats Row** — Total Income, Total Expense, Net Balance, Transaction Count from `/analytics/summary`
2. **Spending Chart** — PrimeNG `p-chart` (bar/doughnut) from `/analytics/category-wise`
3. **6-Month Trend** — PrimeNG line chart from `/analytics/trend`
4. **Recent Transactions** — Last 5–10 transactions (styled list)
5. **Recurring Payments** — Active recurring transactions preview
6. **Budget Status Card** — Budget utilization from `/analytics/budget-summary` (progress bars per category)

### Filters (same as dashboard)
- Account filter (`p-select`) — passes `accountId` to all endpoints
- Period selector — date range applied to all queries

---

## Services

**`AnalyticsService` (`core/services/analytics.service.ts`)**

| Method | Endpoint | Returns |
|--------|----------|---------|
| `getSummary(params)` | GET `/analytics/summary` | `{ totalIncome, totalExpense, netBalance, transactionCount }` |
| `getCategoryWise(params)` | GET `/analytics/category-wise` | `{ categoryName, amount, transactionCount }[]` |
| `getTrend(params)` | GET `/analytics/trend` | `{ month, income, expense }[]` (6 months) |
| `getBudgetSummary()` | GET `/analytics/budget-summary` | `BudgetSummaryResponse[]` |

All methods accept optional `startDate`, `endDate`, `accountId` query params.

---

## Models

```typescript
interface AnalyticsSummary {
  totalIncome: number;
  totalExpense: number;
  netBalance: number;
  transactionCount: number;
}

interface CategoryWiseData {
  categoryName: string;
  amount: number;
  transactionCount: number;
}

interface TrendData {
  month: string;
  income: number;
  expense: number;
}

interface BudgetSummaryResponse {
  budgetId: number;
  budgetName: string;
  categoryName: string;
  budgetedAmount: number;
  spentAmount: number;
  remainingAmount: number;
  utilizationPercent: number;
}
```
