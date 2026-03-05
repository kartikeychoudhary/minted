---
title: Transactions — Web
feature: transactions
layer: web
module: TransactionsModule (lazy, /transactions)
components:
  - TransactionListComponent
  - TransactionFormDialogComponent
  - TransactionFilterComponent
related:
  - docs/features/api/transactions.md
  - docs/features/web/splits.md    (inline split dialog from transactions)
---

# Transactions — Web

## Overview

Full transactions page with AG Grid, filter bar, add/edit dialog, and inline split functionality.

---

## Module

`TransactionsModule` — lazy-loaded at `/transactions`.

---

## Filter Bar

- Date range picker (`p-calendar` with range selection)
- Quick filters: "Last Week", "Last Month", "This Month", "Last 3 Months", "Custom" (`p-selectButton`)
- Account dropdown (`p-dropdown`, multi-select)
- Category dropdown (`p-dropdown`, multi-select)
- Type toggle: All / Income / Expense / Transfer (`p-selectButton`)
- Search input (`pInputText` with debounce)
- "Add Transaction" button (`p-button`)

---

## AG Grid Table

Theme: `themeQuartz.withParams()` with `--minted-*` CSS vars (same pattern across all grids).

**Columns:**
| Column | Details |
|--------|---------|
| Date | Formatted date |
| Description | |
| Category | Icon + color badge |
| Account | |
| Type | INCOME / EXPENSE / TRANSFER badge |
| Amount | Colored: green = income, red = expense; right-aligned with CurrencyService formatting |
| Actions | Edit (pencil), Split (users icon), Delete (trash with confirm) |

- Server-side pagination
- Sortable columns
- Row click → opens edit dialog

**Split button in actions column:**
- `isSplit === true`: accent border + `accent-subtle` background, "Already split" tooltip
- `isSplit === false`: default styling, "Split" tooltip

---

## Transaction Form Dialog

`p-dialog` (640px):
- **Type** (radio/toggle): INCOME / EXPENSE / TRANSFER
- **Amount** (`p-inputnumber`, > 0)
- **Description** (text input)
- **Date** (`p-calendar`)
- **Account** (dropdown)
- **Category** (dropdown, filtered by selected type)
- **To Account** (dropdown, shown only for TRANSFER)
- **Notes** (textarea)
- **Tags** (chips input)
- Save / Cancel buttons
- Validation: Amount > 0, Date required, Account required, Category required

---

## Inline Split Dialog

Opens from the Split action button. Pre-filled with transaction data.

**Split type selector (3 modes):**
- **Equal** (`pi-equals`): auto-divides total evenly; remainder to payer (first entry)
- **Unequal** (`pi-chart-pie`): manual amount entry per friend
- **By Share** (`pi-percentage`): percentage input with live amount calculation

**Features:**
- "Me" entry (payer) — cannot be removed
- Add/remove friends from split
- Validation: 2+ participants, split total must match transaction total (±0.01 tolerance)
- Calls `SplitService.create()` with `SplitTransactionRequest`
- On hide: resets form, entries, split type, available friends list

---

## Models (`core/models/transaction.model.ts`)

```typescript
interface Transaction {
  id: number;
  amount: number;
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER';
  description: string;
  notes: string;
  transactionDate: string;
  account: Account;
  toAccount: Account | null;
  category: TransactionCategory;
  isRecurring: boolean;
  isSplit: boolean;
  tags: string[];
  createdAt: string;
}

interface TransactionFilter {
  page: number;
  size: number;
  startDate?: string;
  endDate?: string;
  accountId?: number;
  categoryId?: number;
  type?: string;
  search?: string;
  period?: 'LAST_WEEK' | 'LAST_MONTH' | 'THIS_MONTH' | 'LAST_3_MONTHS' | 'CUSTOM';
  sortBy?: string;
  sortDir?: 'ASC' | 'DESC';
}

interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```
