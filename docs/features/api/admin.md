---
title: Admin — API
feature: admin
layer: api
package: com.minted.api.admin
routes:
  - GET/POST /api/v1/admin/users
  - GET/PUT/DELETE /api/v1/admin/users/{id}
  - PUT /api/v1/admin/users/{id}/toggle
  - PUT /api/v1/admin/users/{id}/reset-password
  - GET/PUT /api/v1/admin/settings/{key}
  - GET /api/v1/admin/jobs
  - GET /api/v1/admin/jobs/{id}
  - POST /api/v1/admin/jobs/{jobName}/trigger
  - GET /api/v1/admin/schedules
  - PUT /api/v1/admin/schedules/{id}
  - GET/POST/DELETE /api/v1/admin/defaults/categories
  - GET/POST/DELETE /api/v1/admin/defaults/account-types
migrations: V0_0_16 (job tables), V0_0_17 (system_settings), V0_0_27 (statement settings seed), V0_0_30 (EMI default category)
related:
  - docs/features/api/auth.md          (signup-enabled system setting)
  - docs/features/api/recurring.md     (RecurringTransactionJob)
  - docs/features/api/import.md        (BulkImportJob)
  - docs/features/api/statements.md    (LLM models, shared API key setting)
  - docs/features/web/admin.md
---

# Admin — API

## Overview

Admin-only features: user management, system settings (key-value store), job scheduling & execution history, and default seeding lists for new users.

All `/admin/**` endpoints require the `ADMIN` role.

---

## User Management

### Business Rules
- **Create user:** validates username uniqueness + password strength (min 8, one uppercase, one digit), creates with `forcePasswordChange=true`, seeds default account types + categories via `DefaultListsService`
- **Toggle active:** flips `isActive`. Prevents admin from deactivating their own account
- **Delete user:** cascading delete of ALL user data (transactions, recurring, budgets, accounts, account types, categories, bulk imports, dashboard cards), then deletes user. Prevents self-deletion
- **Reset password:** validates strength, encodes, sets `forcePasswordChange=true`

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/admin/users` | List all users |
| GET | `/api/v1/admin/users/{id}` | Get user by ID |
| POST | `/api/v1/admin/users` | Create new user (201) |
| PUT | `/api/v1/admin/users/{id}/toggle` | Toggle user active/inactive |
| DELETE | `/api/v1/admin/users/{id}` | Delete user + all data (204) |
| PUT | `/api/v1/admin/users/{id}/reset-password` | Reset user password |

### DTOs

| DTO | Type | Fields |
|-----|------|--------|
| `AdminUserResponse` | record | id, username, displayName, email, isActive, forcePasswordChange, currency, role, createdAt, updatedAt. Factory: `from(User)` |
| `CreateUserRequest` | record | username (@NotBlank, 3–50), password (@NotBlank, min 8), displayName, email (@Email), role |
| `ResetPasswordRequest` | record | newPassword (@NotBlank, min 8) |

---

## System Settings

Key-value store for app-wide configuration flags.

**Table:** `system_settings`
| Field | Type | Notes |
|-------|------|-------|
| settingKey | VARCHAR(100) | Unique |
| settingValue | VARCHAR(500) | |
| description | VARCHAR(255) | |

**Known keys:**
| Key | Default | Purpose |
|-----|---------|---------|
| `SIGNUP_ENABLED` | `false` | Controls public registration |
| `CREDIT_CARD_PARSER_ENABLED` | `true` | Controls statement parsing feature |
| `ADMIN_LLM_KEY_SHARED` | `false` | Whether admin's LLM API key is shared with all users |

**Service convenience methods:**
- `isSignupEnabled()` — `"true".equalsIgnoreCase(getValue("SIGNUP_ENABLED"))`

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/admin/settings/{key}` | Get setting by key |
| PUT | `/api/v1/admin/settings/{key}` | Update setting value |

### DTOs

| DTO | Fields |
|-----|--------|
| `SystemSettingResponse` | id, settingKey, settingValue, description. Factory: `from(SystemSetting)` |
| `UpdateSettingRequest` | value (@NotBlank) |

---

## Job Scheduling & Execution Framework

### Architecture

```
SchedulerConfig (@EnableScheduling)
  └── ThreadPoolTaskScheduler (pool=5, prefix="JobTaskScheduler-")
        └── JobSchedulerService (ConcurrentHashMap of ScheduledFuture<?>)
              └── Registered Runnable tasks (RecurringTransactionJob, BulkImportJob, ...)

JobExecution → JobStepExecution (1:N, cascade ALL, ordered by stepOrder)
JobExecution → JobScheduleConfig (N:1, optional FK)
```

### Database Tables (V0_0_16)

| Table | Purpose |
|-------|---------|
| `job_schedule_configs` | Cron per job name (unique), enabled flag, lastRunAt |
| `job_executions` | One row per run — status, triggerType, start/end times, step progress |
| `job_step_executions` | One row per step — status, order, contextJson, errorMessage |

### Enums

| Enum | Values |
|------|--------|
| `JobStatus` | RUNNING, COMPLETED, FAILED |
| `JobStepStatus` | PENDING, RUNNING, COMPLETED, FAILED, SKIPPED |
| `JobTriggerType` | SCHEDULED, MANUAL |

### Services

**`JobSchedulerService`:**
- `registerJob(name, task, cron, enabled)` — called at `@PostConstruct` by each job
- `rescheduleJob(name, cron, enabled)` — called when admin updates a schedule
- `triggerJob(name)` — executes registered task immediately in a new thread

**`JobExecutionService`:**
- `getAllJobExecutions(pageable)` — paginated list ordered by startTime DESC
- `getJobExecutionById(id)` — single execution with steps
- `triggerJobManually(jobName)` — validates config exists, delegates to scheduler
- `getAllScheduleConfigs()` / `updateScheduleConfig(id, request)` — schedule CRUD

### Implemented Jobs

| Job | Cron | Purpose |
|-----|------|---------|
| `RecurringTransactionJob` | `0 0 1 * * ?` (1 AM daily) | Process due recurring transactions |
| `BulkImportJob` | `0 */5 * * * ?` (every 5 min) | Sweep stuck CSV imports |

### Adding a New Job

1. Create class implementing `Runnable` in `<feature>/job/` (e.g., `bulkimport/job/BulkImportJob.java`)
2. Inject `JobExecutionRepository`, `JobScheduleConfigRepository`, `JobSchedulerService` from `job` module
3. In `@PostConstruct`, call `jobSchedulerService.registerJob(JOB_NAME, this, cron, enabled)`
4. In `run()`, create `JobExecution`, add `JobStepExecution` entries, handle errors per-step
5. Add seed row to a Flyway migration for `job_schedule_configs`

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/admin/jobs?page=&size=` | List job executions (paginated) |
| GET | `/api/v1/admin/jobs/{id}` | Get execution details with steps |
| POST | `/api/v1/admin/jobs/{jobName}/trigger` | Manually trigger a job (202 Accepted) |
| GET | `/api/v1/admin/schedules` | List all schedule configs |
| PUT | `/api/v1/admin/schedules/{id}` | Update cron expression and enabled flag |

---

## Default Lists (New User Seeding)

Admin manages the master lists that are copied to every new user on registration via `DefaultListsService`.

### Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/admin/defaults/categories` | List default categories |
| POST | `/api/v1/admin/defaults/categories` | Create default category |
| DELETE | `/api/v1/admin/defaults/categories/{id}` | Delete default category |
| GET | `/api/v1/admin/defaults/account-types` | List default account types |
| POST | `/api/v1/admin/defaults/account-types` | Create default account type |
| DELETE | `/api/v1/admin/defaults/account-types/{id}` | Delete default account type |

### Seeded Defaults

**Account Types:**
- Bank Account (`pi pi-building-columns`)
- Credit Card (`pi pi-credit-card`)
- Wallet (`pi pi-wallet`)
- Investment (`pi pi-chart-line`)

**Categories (sample):** Food, Transport, Utilities, Entertainment, Healthcare, Shopping, Salary, Rent, EMI, and others — seeded via `default_categories` table.
