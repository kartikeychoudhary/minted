---
title: Admin — Web
feature: admin
layer: web
module: AdminModule (lazy, /admin, adminGuard)
components:
  - UserManagementComponent
  - JobsListComponent
  - JobDetailComponent
  - ServerSettingsComponent
related:
  - docs/features/api/admin.md
  - docs/features/web/layout.md   (admin sidebar section)
---

# Admin — Web

## Overview

Role-protected admin panel for user management, job monitoring, schedule management, default lists, and feature toggles.

---

## Module

`AdminModule` — lazy-loaded at `/admin`. Protected by `adminGuard` (checks `user.role === 'ADMIN'`, redirects non-admins to `/dashboard`).

**Default redirect:** `/admin` → `/admin/users`

---

## Components

### UserManagementComponent (`/admin/users`)

**Signup Settings Card** — `p-toggleswitch` for enabling/disabling public registration (loads `getSetting('SIGNUP_ENABLED')`).

**Users AG Grid** (rowHeight: 56, minted theme):
| Column | Details |
|--------|---------|
| Username | Bold |
| Display Name | |
| Email | |
| Role | Badge: ADMIN (accent), USER (default) |
| Status | Badge: Active (green), Disabled (red) |
| Password | "MUST CHANGE" badge if `forcePasswordChange=true` |
| Created | Formatted date |
| Actions | Toggle, Reset Password (key icon), Delete (trash icon) |

Actions use `data-action` attributes with `onCellClicked` handler.

**"New User" button** — Opens create dialog.

**Dialogs:**
- **Create User:** Username, Password (`p-password`), Display Name, Email, Role (`p-select`: USER/ADMIN). Banner: password change required on first login
- **Reset Password:** New Password (`p-password`). Banner: forced change on next login
- **Delete User:** PrimeNG ConfirmDialog with cascading data deletion warning

---

### JobsListComponent (`/admin/jobs`)

AG Grid with minted theme listing all job executions.

| Column | Details |
|--------|---------|
| ID | |
| Job Name | |
| Status | Badge: COMPLETED (green), FAILED (red), RUNNING (blue), PENDING (gray) |
| Trigger Type | SCHEDULED / MANUAL |
| Started | Formatted |
| Ended | Formatted |
| Steps | Progress display |
| Actions | View detail |

- Pagination: 15/30/50 rows per page
- "Run Recurring Txn Job" button for manual trigger
- "Refresh" button
- Row click → `/admin/jobs/{id}`

---

### JobDetailComponent (`/admin/jobs/:id`)

- Header: Job ID, status badge, job name, trigger type
- Metadata cards: Started, Ended, Duration, Steps Passed
- Error message alert (if FAILED)

**Execution Steps Timeline:**
- Colored activity bar per step status
- Step order badge, name, duration, timestamp
- Result context JSON in code block
- Per-step error messages

---

### ServerSettingsComponent (`/admin/settings`)

Three sections:

**A. Automated Jobs (Schedules)**
- Cards per schedule: job name, description, cron expression, last run time
- Enable/disable toggle per schedule
- Active/Disabled status badge

**B. Feature Toggles** (after statements feature)
- "Credit Card Statement Parser" (`p-toggleSwitch` for `CREDIT_CARD_PARSER_ENABLED`)
- "Share Admin LLM Key" (`p-toggleSwitch` for `ADMIN_LLM_KEY_SHARED`)

**C. LLM Models Management** (after statements feature)
- AG Grid: Name, Model Key, Provider, Status, Default (star icon), Actions
- Actions: Edit (dialog), Toggle active, Delete (confirmation)
- "Add Model" button → dialog with Name, Provider (locked: GEMINI), Model Key, Description, Set as Default checkbox

**D. Default Categories**
- AG Grid: Name, Type (INCOME/EXPENSE), Icon columns
- Add modal: name, type dropdown, icon class input
- Delete with ConfirmationService

**E. Default Account Types**
- AG Grid: Name column
- Add modal: name input
- Delete with ConfirmationService

---

## Service (`core/services/admin.service.ts`)

**User Management:**
- `getUsers()`, `getUserById(id)`, `createUser(request)`, `toggleUserActive(id)`, `deleteUser(id)`, `resetPassword(id, request)`

**System Settings:**
- `getSetting(key)`, `updateSetting(key, value)`

**Jobs:**
- `getJobs(page, size)`, `getJobExecution(id)`, `triggerJob(jobName)`

**Schedules:**
- `getSchedules()`, `updateSchedule(id, cronExpression, enabled)`

**Default Lists:**
- `getDefaultCategories()`, `createDefaultCategory()`, `deleteDefaultCategory(id)`
- `getDefaultAccountTypes()`, `createDefaultAccountType()`, `deleteDefaultAccountType(id)`

---

## Sidebar Integration

`buildNavigation(role)` in `sidebar.ts` conditionally adds admin items (under separate "Admin" section header):
- Users → `/admin/users`
- Server Jobs → `/admin/jobs`
- Server Settings → `/admin/settings`

Only shown when `role === 'ADMIN'`.
