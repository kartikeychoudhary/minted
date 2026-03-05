---
title: Recurring Transactions — Web
feature: recurring
layer: web
module: RecurringModule (lazy, /recurring)
components:
  - RecurringListComponent
related:
  - docs/features/api/recurring.md
---

# Recurring Transactions — Web

## Overview

Manage scheduled monthly transaction templates with inline form, active schedules table, and summary cards.

---

## Module

`RecurringModule` — lazy-loaded at `/recurring`.

---

## RecurringListComponent

Three-section layout on one page:

### 1. Form Card — "New Recurring Transaction"
- Row 1: Transaction Name (span-2), Amount (`p-inputnumber`, INR), Type Toggle (Expense/Income)
- Row 2: Category (`p-select`), Account (`p-select`), Frequency (fixed "Monthly"), Day of Month (`p-inputnumber`, 1–31)
- Row 3: Start Date (`p-datepicker`), End Date (optional `p-datepicker`), Submit button
- **Type toggle:** Custom CSS buttons — `active-expense` (red) / `active-income` (green)
- **Edit mode:** Shows Cancel + Update buttons instead of Submit

### 2. Active Schedules Table — `p-table`
| Column | Details |
|--------|---------|
| Transaction Name | Icon + name + account |
| Category | Styled badge |
| Amount | Colored by type |
| Next Date | Date + countdown |
| Actions | Edit, Pause/Resume (toggle), Delete |

- Paused rows: 0.6 opacity, PAUSED badge, play icon for resume
- Search filter input in header
- Paginator: 5/10/20 rows per page
- Delete: PrimeNG confirm dialog

### 3. Summary Cards (3-col grid)
- Est. Monthly Expenses (red icon, white bg)
- Est. Monthly Income (green icon, white bg)
- Scheduled Net Flux (white icon, dark green bg)

---

## Service

**`RecurringTransactionService` (`core/services/recurring.service.ts`)**
- Standard CRUD + `toggleStatus(id)` + `getSummary()`
- Uses `AccountService.getAll()` + `CategoryService.getAll()` for dropdown data

---

## Models

```typescript
interface RecurringTransaction {
  id: number;
  name: string;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  category: TransactionCategory;
  account: Account;
  frequency: 'MONTHLY';
  dayOfMonth: number;
  startDate: string;
  endDate: string | null;
  nextExecutionDate: string;
  status: 'ACTIVE' | 'PAUSED';
}

interface RecurringSummary {
  monthlyExpenses: number;
  monthlyIncome: number;
}
```
