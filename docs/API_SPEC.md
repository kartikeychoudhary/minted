# API_SPEC.md — Minted REST API Contract

> Base URL: `http://localhost:5500/api/v1`
> All responses use JSON. All dates use `yyyy-MM-dd` format. All timestamps use ISO 8601.

---

## 1. Common Response Wrapper

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "totalElements": 195,
    "totalPages": 10,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

### Error Response
```json
{
  "success": false,
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "details": ["amount must be greater than 0"],
  "timestamp": "2026-02-15T10:30:00Z",
  "path": "/api/v1/transactions"
}
```

---

## 2. Authentication

### POST `/auth/login`
**Request:**
```json
{
  "username": "admin",
  "password": "admin"
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "username": "admin",
      "displayName": "Administrator",
      "email": null,
      "forcePasswordChange": true
    }
  }
}
```

**Error (401):**
```json
{
  "success": false,
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid username or password"
}
```

### POST `/auth/refresh`
**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response (200):** Same as login response with new tokens.

### PUT `/auth/change-password`
**Headers:** `Authorization: Bearer <token>`

**Request:**
```json
{
  "currentPassword": "admin",
  "newPassword": "MyNewP@ss1",
  "confirmPassword": "MyNewP@ss1"
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

### POST `/auth/signup`
**Public endpoint.** Creates a new user account (only works when signup is enabled).

**Request:**
```json
{
  "username": "johndoe",
  "password": "MyP@ssw0rd",
  "confirmPassword": "MyP@ssw0rd",
  "displayName": "John Doe",
  "email": "john@example.com"
}
```

**Response (201):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 2,
      "username": "johndoe",
      "displayName": "John Doe",
      "email": "john@example.com",
      "forcePasswordChange": false
    }
  }
}
```

**Errors:**
- `400` — "Public registration is currently disabled"
- `400` — "Username already taken"
- `400` — "Passwords do not match"
- `400` — Password strength validation failure

### GET `/auth/signup-enabled`
**Public endpoint.** Returns whether public registration is enabled.

**Response (200):**
```json
{
  "success": true,
  "data": true
}
```

---

## 3. Account Types

### GET `/account-types`
**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Bank Account",
      "description": "Savings and current bank accounts",
      "icon": "fa-building-columns",
      "isActive": true,
      "isDefault": false,
      "createdAt": "2026-02-16T10:00:00",
      "updatedAt": "2026-02-16T10:00:00"
    }
  ]
}
```
**Note:** Soft-deleted types (`isActive: false`) are included in this response. Filter client-side or use `GET /account-types/active` for active-only.

### POST `/account-types`
**Request:**
```json
{
  "name": "UPI Wallet",
  "description": "UPI-based digital wallets",
  "icon": "fa-mobile-screen"
}
```

### PUT `/account-types/{id}`
Same body as POST.

### DELETE `/account-types/{id}`
Soft delete — sets `isActive = false`. Default account types cannot be deleted (returns 400).

Soft-deleted types remain in `GET /account-types` response with `isActive: false`. Use `PATCH /account-types/{id}/toggle` to restore.

### PATCH `/account-types/{id}/toggle`
Toggles `isActive` status. Used to restore soft-deleted account types.

---

## 4. Accounts

### GET `/accounts`
**Query Params:** `?includeInactive=false`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "HDFC Savings",
      "accountType": {
        "id": 1,
        "name": "Bank Account",
        "icon": "fa-building-columns"
      },
      "balance": 125000.50,
      "currency": "INR",
      "color": "#2196F3",
      "icon": "fa-piggy-bank",
      "isActive": true
    }
  ]
}
```

### POST `/accounts`
**Request:**
```json
{
  "name": "HDFC Savings",
  "accountTypeId": 1,
  "balance": 125000.50,
  "currency": "INR",
  "color": "#2196F3",
  "icon": "fa-piggy-bank"
}
```

### PUT `/accounts/{id}`
Same body as POST.

### DELETE `/accounts/{id}`
Soft delete — sets `isActive = false`. Account remains in DB but is excluded from `GET /accounts` responses.

### POST `/accounts` — Restore Behavior
If an account with the same name was previously soft-deleted, creating a new account with that name **restores** the soft-deleted record (updates its fields and sets `isActive = true`) instead of creating a duplicate.

---

## 5. Transaction Categories

### GET `/categories`
**Query Params:** `?type=EXPENSE&includeInactive=false`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 4,
      "name": "Food & Dining",
      "type": "EXPENSE",
      "icon": "fa-utensils",
      "color": "#FF5722",
      "parentId": null,
      "isActive": true,
      "children": []
    }
  ]
}
```

### POST `/categories`
**Request:**
```json
{
  "name": "Snacks",
  "type": "EXPENSE",
  "icon": "fa-cookie",
  "color": "#FF8A65",
  "parentId": 4
}
```

---

## 6. Transactions

### GET `/transactions`
**Query Params:**
```
?page=0
&size=20
&period=LAST_MONTH
&startDate=2026-01-01
&endDate=2026-01-31
&accountId=1
&categoryId=4
&type=EXPENSE
&search=lunch
&sortBy=transactionDate
&sortDir=DESC
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 101,
        "amount": 450.00,
        "type": "EXPENSE",
        "description": "Lunch at cafe",
        "notes": "Team lunch",
        "transactionDate": "2026-02-15",
        "account": {
          "id": 2,
          "name": "HDFC Credit Card",
          "accountType": { "id": 2, "name": "Credit Card" }
        },
        "toAccount": null,
        "category": {
          "id": 4,
          "name": "Food & Dining",
          "icon": "fa-utensils",
          "color": "#FF5722"
        },
        "isRecurring": false,
        "isSplit": false,
        "tags": ["team", "work"],
        "createdAt": "2026-02-15T10:30:00Z"
      }
    ],
    "totalElements": 195,
    "totalPages": 10,
    "size": 20,
    "number": 0
  }
}
```

### POST `/transactions`
**Request:**
```json
{
  "amount": 450.00,
  "type": "EXPENSE",
  "description": "Lunch at cafe",
  "notes": "Team lunch",
  "transactionDate": "2026-02-15",
  "accountId": 2,
  "toAccountId": null,
  "categoryId": 4,
  "isRecurring": false,
  "tags": ["team", "work"]
}
```

### PUT `/transactions/{id}`
Same body as POST.

### DELETE `/transactions/{id}`
Hard delete with confirmation.

### DELETE `/transactions/bulk`
Bulk delete multiple transactions. Reverses account balances for each deleted transaction.

**Request:**
```json
{
  "ids": [101, 102, 103]
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "3 transactions deleted",
  "deletedCount": 3
}
```

### PUT `/transactions/bulk/category`
Bulk update category for multiple transactions.

**Request:**
```json
{
  "ids": [101, 102, 103],
  "categoryId": 5
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "3 transactions updated",
  "updatedCount": 3
}
```

### Transaction Fields

| Field | Type | Description |
|-------|------|-------------|
| `excludeFromAnalysis` | Boolean | If `true`, transaction is excluded from all analytics/summary queries. Default: `false`. |
| `isSplit` | Boolean | Read-only. `true` if this transaction has been linked to a split transaction. |

---

## 7. Budgets

### GET `/budgets?month=2&year=2026`
**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Food Budget",
      "amount": 15000.00,
      "month": 2,
      "year": 2026,
      "category": {
        "id": 4,
        "name": "Food & Dining",
        "icon": "fa-utensils",
        "color": "#FF5722"
      }
    }
  ]
}
```

### GET `/budgets/summary?month=2&year=2026`
**Response:**
```json
{
  "success": true,
  "data": [
    {
      "budgetId": 1,
      "categoryName": "Food & Dining",
      "budgetedAmount": 15000.00,
      "spentAmount": 8750.00,
      "remainingAmount": 6250.00,
      "utilizationPercent": 58.3
    }
  ]
}
```

---

## 8. Dashboard

### GET `/dashboard/cards`
**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "Monthly Expenses",
      "chartType": "BAR",
      "xAxisMeasure": "month",
      "yAxisMeasure": "total_amount",
      "filters": null,
      "positionOrder": 1,
      "width": "HALF"
    }
  ]
}
```

### GET `/dashboard/cards/{id}/data?startDate=2026-01-01&endDate=2026-02-15`
**Response:**
```json
{
  "success": true,
  "data": {
    "labels": ["Jan", "Feb"],
    "datasets": [
      {
        "label": "Total Expenses",
        "data": [45000, 32500],
        "backgroundColor": ["#FF5722", "#FF5722"]
      }
    ]
  }
}
```

### PUT `/dashboard/cards/{id}`
**Request:**
```json
{
  "title": "Monthly Expenses",
  "chartType": "LINE",
  "xAxisMeasure": "week",
  "yAxisMeasure": "average_amount",
  "width": "FULL"
}
```

### PUT `/dashboard/cards/reorder`
**Request:**
```json
{
  "cardOrders": [
    { "cardId": 1, "positionOrder": 1 },
    { "cardId": 3, "positionOrder": 2 },
    { "cardId": 2, "positionOrder": 3 }
  ]
}
```

---

## 9. Analytics

### GET `/analytics/summary?startDate=2026-02-01&endDate=2026-02-28`
**Response:**
```json
{
  "success": true,
  "data": {
    "totalIncome": 125000.00,
    "totalExpense": 89000.00,
    "netBalance": 36000.00,
    "transactionCount": 87
  }
}
```

### GET `/analytics/category-wise?startDate=2026-02-01&endDate=2026-02-28&type=EXPENSE`
**Response:**
```json
{
  "success": true,
  "data": [
    {
      "categoryId": 4,
      "categoryName": "Food & Dining",
      "icon": "fa-utensils",
      "color": "#FF5722",
      "totalAmount": 18500.00,
      "transactionCount": 23,
      "percentage": 20.8
    }
  ]
}
```

### GET `/analytics/trend?months=6`
**Response:**
```json
{
  "success": true,
  "data": [
    {
      "month": "2025-09",
      "income": 120000,
      "expense": 85000,
      "net": 35000
    }
  ]
}
```

### GET `/analytics/spending-activity?startDate=2026-02-10&endDate=2026-02-16`
**Response:**
```json
{
  "success": true,
  "data": [
    {
      "date": "2026-02-10",
      "dayLabel": "Mon",
      "amount": 2500.00
    },
    {
      "date": "2026-02-11",
      "dayLabel": "Tue",
      "amount": 0
    }
  ]
}
```

### GET `/analytics/total-balance`
**Response:**
```json
{
  "success": true,
  "data": {
    "totalBalance": 245000.50,
    "previousMonthBalance": 245000.50,
    "incomeChangePercent": 12.5,
    "expenseChangePercent": -8.3
  }
}
```

---

## 9. Recurring Transactions

### 9.1 List All

`GET /api/v1/recurring-transactions`

**Response:** `{ "success": true, "data": [RecurringTransactionResponse] }`

### 9.2 Get By ID

`GET /api/v1/recurring-transactions/{id}`

**Response:** `{ "success": true, "data": RecurringTransactionResponse }`

### 9.3 Create

`POST /api/v1/recurring-transactions`

**Request Body:**
```json
{
  "name": "Monthly Rent Payment",
  "amount": 25000,
  "type": "EXPENSE",
  "categoryId": 1,
  "accountId": 1,
  "frequency": "MONTHLY",
  "dayOfMonth": 1,
  "startDate": "2026-01-01",
  "endDate": null
}
```

**Response:** `201 Created` with `RecurringTransactionResponse`

### 9.4 Update

`PUT /api/v1/recurring-transactions/{id}`

**Request Body:** Same as Create.

### 9.5 Delete

`DELETE /api/v1/recurring-transactions/{id}`

### 9.6 Toggle Status (Active/Paused)

`PATCH /api/v1/recurring-transactions/{id}/toggle`

### 9.7 Summary

`GET /api/v1/recurring-transactions/summary`

**Response:**
```json
{
  "success": true,
  "data": {
    "estimatedMonthlyExpenses": 2500.49,
    "estimatedMonthlyIncome": 6800.00,
    "scheduledNetFlux": 4299.51,
    "activeCount": 5,
    "pausedCount": 1
  }
}
```

### 9.8 Search

`GET /api/v1/recurring-transactions/search?q=rent`

**Response:** `{ "success": true, "data": [RecurringTransactionResponse] }`

### RecurringTransactionResponse Object

```json
{
  "id": 1,
  "name": "Monthly Rent Payment",
  "amount": 25000,
  "type": "EXPENSE",
  "categoryId": 1,
  "categoryName": "Housing",
  "categoryIcon": "home",
  "categoryColor": "#ef4444",
  "accountId": 1,
  "accountName": "Main Checking",
  "frequency": "MONTHLY",
  "dayOfMonth": 1,
  "startDate": "2026-01-01",
  "endDate": null,
  "status": "ACTIVE",
  "nextExecutionDate": "2026-03-01",
  "createdAt": "2026-01-15T10:00:00",
  "updatedAt": "2026-01-15T10:00:00"
}
```

---

## 10. Bulk Import

### 10.1 Download CSV Template

`GET /api/v1/imports/template`

**Response:** Binary CSV file download (`Content-Disposition: attachment; filename=minted_import_template.csv`)

```csv
date,amount,type,description,categoryName,notes,tags
2026-01-15,1500.00,EXPENSE,Grocery shopping,Groceries,Weekly groceries,food;weekly
2026-01-16,50000.00,INCOME,Monthly salary,Salary,January salary,salary;monthly
```

### 10.2 Upload & Validate CSV

`POST /api/v1/imports/upload` (multipart/form-data)

**Request Params:**
- `file` — CSV file (multipart)
- `accountId` — Target account ID (Long)

**Response (200):**
```json
{
  "success": true,
  "data": {
    "importId": 1,
    "totalRows": 5,
    "validRows": 3,
    "errorRows": 1,
    "duplicateRows": 1,
    "rows": [
      {
        "rowNumber": 1,
        "date": "2026-01-15",
        "amount": "1500.00",
        "type": "EXPENSE",
        "description": "Grocery shopping",
        "categoryName": "Groceries",
        "notes": "Weekly groceries",
        "tags": "food;weekly",
        "status": "VALID",
        "errorMessage": null,
        "matchedCategoryId": 5,
        "isDuplicate": false
      },
      {
        "rowNumber": 2,
        "date": "invalid-date",
        "amount": "100.00",
        "type": "EXPENSE",
        "description": "Test",
        "categoryName": "Food",
        "notes": "",
        "tags": "",
        "status": "ERROR",
        "errorMessage": "Invalid date format. Expected yyyy-MM-dd",
        "matchedCategoryId": null,
        "isDuplicate": false
      }
    ]
  }
}
```

### 10.3 Confirm Import

`POST /api/v1/imports/confirm`

**Request:**
```json
{
  "importId": 1,
  "skipDuplicates": true
}
```

**Response (202 Accepted):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "accountId": 1,
    "accountName": "HDFC Savings",
    "importType": "CSV",
    "fileName": "transactions.csv",
    "fileSize": 2048,
    "totalRows": 5,
    "validRows": 3,
    "duplicateRows": 1,
    "errorRows": 1,
    "importedRows": 0,
    "status": "IMPORTING",
    "jobExecutionId": 5,
    "errorMessage": null,
    "createdAt": "2026-02-20T10:00:00",
    "updatedAt": "2026-02-20T10:00:00"
  }
}
```

### 10.4 List User Imports

`GET /api/v1/imports`

**Response:** `{ "success": true, "data": [BulkImportResponse] }`

### 10.5 Get Import By ID

`GET /api/v1/imports/{id}`

**Response:** `{ "success": true, "data": BulkImportResponse }`

### 10.6 Get Import Job Details

`GET /api/v1/imports/{id}/job`

**Response:** `{ "success": true, "data": JobExecutionResponse }` (same format as admin job detail with steps)

---

## 11. Admin — Jobs, Server Settings & User Management

> Admin-only endpoints. Requires ADMIN role.

### 11.1 User Management

#### List All Users
```
GET /api/v1/admin/users
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "username": "admin",
      "displayName": "Administrator",
      "email": null,
      "isActive": true,
      "forcePasswordChange": false,
      "currency": "INR",
      "role": "ADMIN",
      "createdAt": "2026-02-16T10:00:00",
      "updatedAt": "2026-02-21T14:30:00"
    }
  ]
}
```

#### Get User By ID
```
GET /api/v1/admin/users/{id}
```

**Response:** `{ "success": true, "data": AdminUserResponse }`

#### Create User
```
POST /api/v1/admin/users
```

**Request:**
```json
{
  "username": "newuser",
  "password": "SecureP@ss1",
  "displayName": "New User",
  "email": "user@example.com",
  "role": "USER"
}
```

**Response (201):** `{ "success": true, "data": AdminUserResponse }`

- User is created with `forcePasswordChange = true`
- Default account types and categories are seeded for the new user
- Password must meet strength requirements (min 8 chars, 1 uppercase, 1 number)

#### Toggle User Active/Inactive
```
PUT /api/v1/admin/users/{id}/toggle
```

**Response:** `{ "success": true, "data": AdminUserResponse }`

- Prevents admin from disabling their own account (returns 400)

#### Delete User
```
DELETE /api/v1/admin/users/{id}
```

**Response:** `204 No Content`

- Cascading delete of all user data: transactions, recurring transactions, budgets, accounts, account types, categories, bulk imports, dashboard cards
- Prevents admin from deleting their own account (returns 400)

#### Reset User Password
```
PUT /api/v1/admin/users/{id}/reset-password
```

**Request:**
```json
{
  "newPassword": "NewP@ss123"
}
```

**Response:** `{ "success": true, "message": "Password reset successfully" }`

- Sets `forcePasswordChange = true` so user must change on next login

### 11.2 System Settings

#### Get Setting
```
GET /api/v1/admin/settings/{key}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "settingKey": "SIGNUP_ENABLED",
    "settingValue": "false",
    "description": "Controls whether public user registration is allowed"
  }
}
```

#### Update Setting
```
PUT /api/v1/admin/settings/{key}
```

**Request:**
```json
{
  "value": "true"
}
```

**Response:** `{ "success": true, "data": SystemSettingResponse }`

### 11.3 Job Executions

#### List Job Executions
```
GET /api/v1/admin/jobs?page=0&size=20
```

**Response:** Spring Page of `JobExecutionResponse`
```json
{
  "content": [
    {
      "id": 1,
      "jobName": "RECURRING_TRANSACTION_PROCESSOR",
      "status": "COMPLETED",
      "triggerType": "SCHEDULED",
      "startTime": "2026-02-20T01:00:00",
      "endTime": "2026-02-20T01:00:02",
      "errorMessage": null,
      "totalSteps": 3,
      "completedSteps": 3,
      "steps": []
    }
  ],
  "totalElements": 15,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

#### Get Job Execution Detail
```
GET /api/v1/admin/jobs/{id}
```

**Response:** `JobExecutionResponse` with steps populated
```json
{
  "id": 1,
  "jobName": "RECURRING_TRANSACTION_PROCESSOR",
  "status": "COMPLETED",
  "triggerType": "MANUAL",
  "startTime": "2026-02-20T01:00:00",
  "endTime": "2026-02-20T01:00:02",
  "errorMessage": null,
  "totalSteps": 3,
  "completedSteps": 3,
  "steps": [
    {
      "id": 1,
      "stepName": "Fetch Due Recurring Transactions",
      "stepOrder": 1,
      "status": "COMPLETED",
      "contextJson": "{\"dueCount\":5,\"targetDate\":\"2026-02-20\"}",
      "errorMessage": null,
      "startTime": "2026-02-20T01:00:00",
      "endTime": "2026-02-20T01:00:00"
    },
    {
      "id": 2,
      "stepName": "Process Transactions",
      "stepOrder": 2,
      "status": "COMPLETED",
      "contextJson": "{\"processed\":5,\"created\":5,\"failed\":0}",
      "errorMessage": null,
      "startTime": "2026-02-20T01:00:00",
      "endTime": "2026-02-20T01:00:01"
    },
    {
      "id": 3,
      "stepName": "Update Schedule Configuration",
      "stepOrder": 3,
      "status": "COMPLETED",
      "contextJson": "{\"configUpdated\":true}",
      "errorMessage": null,
      "startTime": "2026-02-20T01:00:01",
      "endTime": "2026-02-20T01:00:02"
    }
  ]
}
```

#### Trigger Job Manually
```
POST /api/v1/admin/jobs/{jobName}/trigger
```
**Response:** `202 Accepted` (job runs asynchronously)

### 11.4 Job Schedule Configs

#### List Schedules
```
GET /api/v1/admin/schedules
```

**Response:**
```json
[
  {
    "id": 1,
    "jobName": "RECURRING_TRANSACTION_PROCESSOR",
    "cronExpression": "0 0 1 * * ?",
    "enabled": true,
    "lastRunAt": "2026-02-20T01:00:02",
    "description": "Processes all active recurring transactions daily."
  }
]
```

#### Update Schedule
```
PUT /api/v1/admin/schedules/{id}
```

**Request:**
```json
{
  "cronExpression": "0 0 2 * * ?",
  "enabled": true
}
```

### 11.5 Default Lists

#### Default Categories
```
GET    /api/v1/admin/defaults/categories
POST   /api/v1/admin/defaults/categories   { "name": "Food", "type": "EXPENSE", "icon": "pi-shopping-cart" }
DELETE /api/v1/admin/defaults/categories/{id}
```

#### Default Account Types
```
GET    /api/v1/admin/defaults/account-types
POST   /api/v1/admin/defaults/account-types   { "name": "Savings Account" }
DELETE /api/v1/admin/defaults/account-types/{id}
```

---

## 12. Notifications

### 12.1 User Notifications

```
GET    /api/v1/notifications?page=0&size=20
```
**Response:** Spring `Page<NotificationResponse>` — `content[]`, `totalElements`, `totalPages`, `size`, `number`, `first`, `last`

```json
{
  "content": [
    {
      "id": 1,
      "type": "SUCCESS",
      "title": "Welcome to Minted!",
      "message": "Thank you for signing up. Start managing your finances today.",
      "isRead": false,
      "createdAt": "2026-02-23T10:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true
}
```

### 12.2 Unread Count (Polled every 30s)

```
GET    /api/v1/notifications/unread-count
```
**Response:** `long` (plain number)

### 12.3 Mark as Read

```
PUT    /api/v1/notifications/{id}/read
```

### 12.4 Mark All as Read

```
PUT    /api/v1/notifications/read-all
```

### 12.5 Dismiss (Hard Delete)

```
DELETE /api/v1/notifications/{id}
```

### 12.6 Dismiss All Read

```
DELETE /api/v1/notifications/read
```

**Notification Types:** `INFO`, `SUCCESS`, `WARNING`, `ERROR`, `SYSTEM`

---

## 13. HTTP Status Code Usage

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful GET, PUT, DELETE |
| 201 | Created | Successful POST |
| 400 | Bad Request | Validation errors |
| 401 | Unauthorized | Invalid/expired token |
| 403 | Forbidden | Access denied / force password change |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource |
| 500 | Server Error | Unexpected errors |

---

## 14. Credit Card Statement Parser

### 14.1 Statement Endpoints (`/api/v1/statements`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/statements/upload` | Upload PDF, extract text (multipart form) |
| POST | `/statements/{id}/parse` | Trigger LLM parse (async) |
| GET | `/statements/{id}/parsed-rows` | Get parsed transaction rows |
| POST | `/statements/confirm` | Confirm and import transactions |
| GET | `/statements` | List user's statements |
| GET | `/statements/{id}` | Get statement detail |

#### Upload Statement (multipart/form-data)

`POST /api/v1/statements/upload`

**Request Params:**
- `file` — PDF (max 20MB), CSV (max 5MB), or TXT (max 5MB) file
- `accountId` — Target account ID (Long)
- `pdfPassword` — Optional PDF password (String, only used for PDF files)

**Supported file types:** PDF (`.pdf`), CSV (`.csv`), TXT (`.txt`). For CSV/TXT files, contents are read as UTF-8 text directly. For PDF files, text is extracted via PDFBox.

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "accountId": 2,
    "accountName": "HDFC Credit Card",
    "fileName": "statement_jan.pdf",
    "fileSize": 204800,
    "fileType": "PDF",
    "status": "TEXT_EXTRACTED",
    "currentStep": 2,
    "extractedText": "HDFC BANK CREDIT CARD STATEMENT...",
    "parsedCount": 0,
    "duplicateCount": 0,
    "importedCount": 0,
    "errorMessage": null,
    "jobExecutionId": null,
    "createdAt": "2026-02-23T18:00:00",
    "updatedAt": "2026-02-23T18:00:01"
  }
}
```

#### Trigger LLM Parse

`POST /api/v1/statements/{id}/parse`

**Request Body (optional):**
```json
{
  "extractedText": "Edited text content to override stored text before parsing..."
}
```

If `extractedText` is provided and non-blank, it overwrites the stored extracted text before sending to AI. This allows users to fix OCR/extraction errors in the text review step.

**Response (202 Accepted):**
```json
{
  "success": true,
  "data": { "...StatementResponse with status SENT_FOR_AI_PARSING..." },
  "message": "AI parsing started"
}
```

**Statement Status Flow:** `UPLOADED` → `TEXT_EXTRACTED` → `SENT_FOR_AI_PARSING` → `LLM_PARSED` → `COMPLETED`

> Parsing runs asynchronously. Poll `GET /statements/{id}` until `status` changes from `SENT_FOR_AI_PARSING` to `LLM_PARSED` or `FAILED`.

**Note:** `extractedText` is suppressed (null) in API responses when status is `SENT_FOR_AI_PARSING`, `LLM_PARSED`, `CONFIRMING`, or `COMPLETED` to reduce payload size.

#### Get Parsed Transaction Rows

`GET /api/v1/statements/{id}/parsed-rows`

**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "tempId": "uuid-string",
      "amount": 450.00,
      "type": "EXPENSE",
      "description": "SWIGGY*ORDER",
      "transactionDate": "2026-01-15",
      "categoryName": "Food & Dining",
      "matchedCategoryId": 4,
      "notes": "Ref: TXN123456",
      "tags": "",
      "isDuplicate": false,
      "duplicateReason": "",
      "mappedByRule": true
    }
  ]
}
```

#### Confirm Import

`POST /api/v1/statements/confirm`

**Request:**
```json
{
  "statementId": 1,
  "skipDuplicates": true
}
```

**Response (200):**
```json
{
  "success": true,
  "message": "Transactions imported successfully"
}
```

#### List Statements

`GET /api/v1/statements`

**Response:** `{ "success": true, "data": [StatementResponse] }`

#### Get Statement Detail

`GET /api/v1/statements/{id}`

**Response:** `{ "success": true, "data": StatementResponse }`

### 14.2 LLM Config Endpoints (`/api/v1/llm-config`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/llm-config` | Get user's LLM config (includes merchant mappings) |
| PUT | `/llm-config` | Save/update user's LLM config (key + model) |
| GET | `/llm-config/models` | List active models (for user dropdown) |
| GET | `/llm-config/mappings` | List user's merchant-category mappings |
| POST | `/llm-config/mappings` | Create new mapping |
| PUT | `/llm-config/mappings/{id}` | Update mapping |
| DELETE | `/llm-config/mappings/{id}` | Delete mapping |

#### Get Config (with merchant mappings)

`GET /api/v1/llm-config`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "provider": "GEMINI",
    "hasApiKey": true,
    "selectedModel": {
      "id": 1, "name": "Gemini 2.0 Flash", "provider": "GEMINI",
      "modelKey": "gemini-2.0-flash", "description": "Fast and efficient",
      "isActive": true, "isDefault": true
    },
    "merchantMappings": [
      {
        "id": 1,
        "snippets": "ZEPTO,BLINKIT,BIGBASKET",
        "snippetList": ["ZEPTO", "BLINKIT", "BIGBASKET"],
        "categoryId": 5,
        "categoryName": "Groceries",
        "categoryIcon": "fa-cart-shopping",
        "categoryColor": "#FF9800"
      }
    ]
  }
}
```

#### Save Config

`PUT /api/v1/llm-config`

**Request:**
```json
{
  "apiKey": "AIzaSy...",
  "modelId": 1
}
```

#### Merchant Mapping CRUD

**Create:** `POST /api/v1/llm-config/mappings`
```json
{ "snippets": "ZEPTO,BLINKIT,BIGBASKET", "categoryId": 5 }
```

**Update:** `PUT /api/v1/llm-config/mappings/{id}` — Same body as create.

**Delete:** `DELETE /api/v1/llm-config/mappings/{id}` — Returns 204.

### 14.3 Admin LLM Model Endpoints (`/api/v1/admin/llm-models`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/admin/llm-models` | List all models (incl. disabled) |
| POST | `/admin/llm-models` | Create new model |
| PUT | `/admin/llm-models/{id}` | Update model |
| DELETE | `/admin/llm-models/{id}` | Hard delete model |

**Create/Update Request:**
```json
{
  "name": "Gemini 2.0 Flash",
  "provider": "GEMINI",
  "modelKey": "gemini-2.0-flash",
  "description": "Fast and efficient",
  "isActive": true,
  "isDefault": false
}
```

### 14.4 Dashboard Configuration (`/api/v1/dashboard-config`)

Per-user configuration for excluding categories from analytics dashboards.

#### Get Dashboard Config

`GET /api/v1/dashboard-config`

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "excludedCategoryIds": [4, 7, 12]
  }
}
```

Returns empty `excludedCategoryIds: []` if no config exists for the user yet.

#### Save Dashboard Config

`PUT /api/v1/dashboard-config`

**Request:**
```json
{
  "excludedCategoryIds": [4, 7, 12]
}
```

**Response (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "excludedCategoryIds": [4, 7, 12]
  }
}
```

Creates config if none exists, updates if already exists.

---

### 14.5 New System Settings

| Key | Default | Description |
|-----|---------|-------------|
| `CREDIT_CARD_PARSER_ENABLED` | `true` | Enable/disable the Credit Card Statement Parser feature |
| `ADMIN_LLM_KEY_SHARED` | `false` | If true, admin LLM key is available to users without their own key |

Managed via existing admin settings endpoints: `GET/PUT /api/v1/admin/settings/{key}`

---

## 15. Friends

### GET `/friends`
Returns all active friends for the authenticated user.
**Response:** `{ success: true, data: FriendResponse[] }`

### GET `/friends/{id}`
**Response:** `{ success: true, data: FriendResponse }`

### POST `/friends`
**Request:**
```json
{
  "name": "Marcus Chen",
  "email": "marcus@email.com",
  "phone": "+1 555 0101",
  "avatarColor": "#6366f1"
}
```
**Response (201):** `{ success: true, data: FriendResponse, message: "Friend added successfully" }`

### PUT `/friends/{id}`
Same body as POST. **Response:** `{ success: true, data: FriendResponse, message: "Friend updated successfully" }`

### DELETE `/friends/{id}`
Soft-delete (sets `isActive=false`). **Response:** `{ success: true, message: "Friend removed successfully" }`

### FriendResponse
```json
{
  "id": 1,
  "name": "Marcus Chen",
  "email": "marcus@email.com",
  "phone": "+1 555 0101",
  "avatarColor": "#6366f1",
  "isActive": true,
  "createdAt": "2026-02-25T10:00:00",
  "updatedAt": "2026-02-25T10:00:00"
}
```

---

## 16. Split Transactions

### GET `/splits`
Returns all split transactions for the authenticated user (ordered by date desc).
**Response:** `{ success: true, data: SplitTransactionResponse[] }`

### GET `/splits/{id}`
**Response:** `{ success: true, data: SplitTransactionResponse }`

### POST `/splits`
**Request:**
```json
{
  "sourceTransactionId": null,
  "description": "Dinner at Mario's",
  "categoryName": "Dining",
  "totalAmount": 142.50,
  "splitType": "EQUAL",
  "transactionDate": "2026-02-25",
  "shares": [
    { "friendId": null, "shareAmount": 47.50, "isPayer": true },
    { "friendId": 1, "shareAmount": 47.50, "isPayer": false },
    { "friendId": 2, "shareAmount": 47.50, "isPayer": false }
  ]
}
```
- `friendId: null` represents "Me" (the authenticated user)
- `splitType`: `EQUAL` | `UNEQUAL` | `SHARE`
- For `EQUAL` split type, share amounts are auto-calculated (remainder goes to first share)

**Response (201):** `{ success: true, data: SplitTransactionResponse, message: "Split transaction created successfully" }`

### PUT `/splits/{id}`
Same body as POST. Existing shares are cleared and rebuilt via orphanRemoval. **Response:** `{ success: true, data: SplitTransactionResponse }`

### DELETE `/splits/{id}`
Hard delete (cascades to shares). **Response:** `{ success: true, message: "Split transaction deleted successfully" }`

### GET `/splits/summary`
Returns aggregate balances.
**Response:**
```json
{
  "success": true,
  "data": {
    "youAreOwed": 450.00,
    "youOwe": 125.50
  }
}
```

### GET `/splits/balances`
Returns per-friend net balance (unsettled shares only).
**Response:**
```json
{
  "success": true,
  "data": [
    { "friendId": 1, "friendName": "Marcus Chen", "avatarColor": "#6366f1", "balance": 124.50 },
    { "friendId": 2, "friendName": "Sarah Jenkins", "avatarColor": "#ec4899", "balance": -85.00 }
  ]
}
```
Positive balance = friend owes user. Negative = user owes friend.

### POST `/splits/settle`
Settles all unsettled shares for a friend. Sends notification on success.
**Request:**
```json
{ "friendId": 1 }
```
**Response:** `{ success: true, message: "Settlement completed successfully" }`

### GET `/splits/friend/{friendId}/shares`
Returns all shares for a specific friend (for CSV export).
**Response:** `{ success: true, data: SplitShareResponse[] }`

### SplitTransactionResponse
```json
{
  "id": 1,
  "sourceTransactionId": null,
  "description": "Dinner at Mario's",
  "categoryName": "Dining",
  "totalAmount": 142.50,
  "splitType": "EQUAL",
  "transactionDate": "2026-02-25",
  "isSettled": false,
  "yourShare": 47.50,
  "shares": [
    {
      "id": 1,
      "friendId": null,
      "friendName": "Me",
      "friendAvatarColor": null,
      "shareAmount": 47.50,
      "sharePercentage": null,
      "isPayer": true,
      "isSettled": false,
      "settledAt": null,
      "splitDescription": "Dinner at Mario's",
      "splitCategoryName": "Dining",
      "splitTransactionDate": "2026-02-25"
    }
  ],
  "createdAt": "2026-02-25T10:00:00",
  "updatedAt": "2026-02-25T10:00:00"
}
```

