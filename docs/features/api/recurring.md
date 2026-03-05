---
title: Recurring Transactions — API
feature: recurring
layer: api
package: com.minted.api.recurring
routes:
  - GET/POST/PUT/DELETE /api/v1/recurring-transactions
  - PATCH /api/v1/recurring-transactions/{id}/toggle
  - GET   /api/v1/recurring-transactions/summary
migrations: V0_0_9 (recurring_transactions table)
jobs: RecurringTransactionJob (daily 1 AM)
related:
  - docs/features/api/admin.md        (job scheduling, manual trigger)
  - docs/features/web/recurring.md
---

# Recurring Transactions — API

## Overview

Scheduled transaction templates. The `RecurringTransactionJob` runs daily at 1 AM and creates actual `Transaction` records for all active recurring transactions due today.

---

## Database

`recurring_transactions` table — key columns:
| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | PK |
| name | VARCHAR(200) | Required |
| amount | DECIMAL(15,2) | Required |
| type | VARCHAR(30) | INCOME / EXPENSE |
| category_id | BIGINT | FK → transaction_categories |
| account_id | BIGINT | FK → accounts |
| frequency | VARCHAR(30) | MONTHLY (currently only option) |
| day_of_month | INT | 1–31 |
| start_date | DATE | First execution date |
| end_date | DATE | nullable — no end if null |
| next_execution_date | DATE | Updated after each run |
| status | VARCHAR(30) | ACTIVE / PAUSED |
| user_id | BIGINT | FK → users |

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/recurring-transactions` | List all for user |
| GET | `/api/v1/recurring-transactions/{id}` | Get by ID |
| POST | `/api/v1/recurring-transactions` | Create |
| PUT | `/api/v1/recurring-transactions/{id}` | Update |
| DELETE | `/api/v1/recurring-transactions/{id}` | Delete |
| PATCH | `/api/v1/recurring-transactions/{id}/toggle` | Toggle ACTIVE ↔ PAUSED |
| GET | `/api/v1/recurring-transactions/summary` | Monthly income/expense summary |

---

## Job: RecurringTransactionJob

**File:** `com.minted.api.recurring.job.RecurringTransactionJob`
**Cron:** `0 0 1 * * ?` (1 AM daily)
**Registration:** `@PostConstruct init()` reads config from DB, calls `jobSchedulerService.registerJob()`

**Steps:**
1. **Fetch** — Query `RecurringTransaction` where `status=ACTIVE AND nextExecutionDate <= today`
2. **Process** — For each due transaction: create `Transaction` entity, update `account.balance`, set `nextExecutionDate = nextExecutionDate + 1 month`
3. **Update Config** — Set `lastRunAt` on `job_schedule_configs`

Error handling: per-transaction try/catch; step status set to FAILED only if ALL transactions fail.

---

## DTOs

| DTO | Key Fields |
|-----|-----------|
| `RecurringTransactionRequest` | name, amount, type, categoryId, accountId, frequency, dayOfMonth, startDate, endDate |
| `RecurringTransactionResponse` | id, name, amount, type, category, account, frequency, dayOfMonth, startDate, endDate, nextExecutionDate, status |
| `RecurringSummary` | monthlyExpenses, monthlyIncome |
