# API_SPEC.md — Minted REST API Contract

> Base URL: `http://localhost:5500/api/v1`
> All responses use JSON. Dates: `yyyy-MM-dd`. Timestamps: ISO 8601.
> Authorization: `Bearer <JWT>` header required on all endpoints except `/auth/**`.

---

## Common Formats

### Error Response
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "details": ["amount must be greater than 0"],
  "timestamp": "2026-02-15T10:30:00Z",
  "path": "/api/v1/transactions"
}
```

### Paginated Response
Spring `Page<T>` wrapper: `{ content[], totalElements, totalPages, size, number, first, last }`

### HTTP Status Conventions
| Status | Meaning |
|--------|---------|
| 200 | OK |
| 201 | Created (admin user creation) |
| 202 | Accepted (async job trigger) |
| 204 | No Content (delete) |
| 400 | Bad Request / Validation failure |
| 401 | Unauthorized (bad/missing JWT) |
| 403 | Forbidden (forcePasswordChange or wrong role) |
| 404 | Resource not found |
| 409 | Duplicate resource |

---

## Feature Endpoint Index

> For request/response shapes, DTOs, and business rules, see the feature doc.

| Feature | Endpoints | Feature Doc |
|---------|-----------|-------------|
| **Auth** | `POST /auth/login`, `/auth/refresh`, `PUT /auth/change-password`, `POST /auth/signup`, `GET /auth/signup-enabled` | [docs/features/api/auth.md](features/api/auth.md) |
| **Accounts** | `GET/POST/PUT/DELETE /accounts`, `GET /accounts/{id}` | [docs/features/api/accounts.md](features/api/accounts.md) |
| **Account Types** | `GET/POST/PUT/DELETE /account-types`, `PATCH /account-types/{id}/toggle` | [docs/features/api/accounts.md](features/api/accounts.md) |
| **Transactions** | `GET/POST/PUT/DELETE /transactions`, `/transactions/{id}`, `/transactions/bulk-delete`, `/transactions/bulk-category` | [docs/features/api/transactions.md](features/api/transactions.md) |
| **Categories** | `GET/POST/PUT/DELETE /categories` | [docs/features/api/transactions.md](features/api/transactions.md) |
| **Budgets** | `GET/POST/PUT/DELETE /budgets`, `GET /budgets/summary` | [docs/features/api/budgets.md](features/api/budgets.md) |
| **Analytics** | `GET /analytics/summary`, `/category-wise`, `/trend`, `/budget-summary` | [docs/features/api/analytics.md](features/api/analytics.md) |
| **Dashboard Cards** | `GET/POST/PUT/DELETE /dashboard/cards`, `PUT /dashboard/cards/reorder`, `GET /dashboard/cards/{id}/data` | [docs/features/api/analytics.md](features/api/analytics.md) |
| **Dashboard Config** | `GET/PUT /dashboard-config` | [docs/features/api/analytics.md](features/api/analytics.md) |
| **Recurring** | `GET/POST/PUT/DELETE /recurring-transactions`, `PATCH /{id}/toggle`, `GET /summary` | [docs/features/api/recurring.md](features/api/recurring.md) |
| **Notifications** | `GET /notifications`, `GET /unread-count`, `PUT /{id}/read`, `PUT /read-all`, `DELETE /{id}`, `DELETE /read` | [docs/features/api/notifications.md](features/api/notifications.md) |
| **Bulk Import** | `GET /bulk-import/template`, `POST /upload`, `POST /confirm`, `GET /bulk-import`, `GET /{id}`, `GET /{id}/job-details` | [docs/features/api/import.md](features/api/import.md) |
| **Statements** | `POST /statements/upload`, `/parse`, `GET /parsed-rows`, `POST /confirm`, `GET/DELETE /statements` | [docs/features/api/statements.md](features/api/statements.md) |
| **LLM Config** | `GET/PUT /llm-config`, `GET /models`, `CRUD /mappings` | [docs/features/api/statements.md](features/api/statements.md) |
| **Friends** | `GET/POST/PUT/DELETE /friends`, `POST/DELETE /{id}/avatar` | [docs/features/api/splits.md](features/api/splits.md) |
| **Splits** | `GET/POST/PUT/DELETE /splits`, `GET /summary`, `GET /balances`, `POST /settle`, `GET /friend/{id}/shares` | [docs/features/api/splits.md](features/api/splits.md) |
| **Admin — Users** | `GET/POST /admin/users`, `GET/PUT/DELETE /admin/users/{id}`, `PUT /toggle`, `PUT /reset-password` | [docs/features/api/admin.md](features/api/admin.md) |
| **Admin — Settings** | `GET/PUT /admin/settings/{key}` | [docs/features/api/admin.md](features/api/admin.md) |
| **Admin — Jobs** | `GET /admin/jobs`, `GET /admin/jobs/{id}`, `POST /admin/jobs/{name}/trigger` | [docs/features/api/admin.md](features/api/admin.md) |
| **Admin — Schedules** | `GET/PUT /admin/schedules` | [docs/features/api/admin.md](features/api/admin.md) |
| **Admin — Defaults** | `GET/POST/DELETE /admin/defaults/categories`, `/account-types` | [docs/features/api/admin.md](features/api/admin.md) |
| **Admin — LLM Models** | `GET/POST/PUT/DELETE /admin/llm-models` | [docs/features/api/statements.md](features/api/statements.md) |
| **Profile** | `GET/PUT /profile`, `POST/DELETE /profile/avatar` | [docs/features/api/auth.md](features/api/auth.md) |

---

## Global Query Parameters (Transactions)

| Param | Type | Description |
|-------|------|-------------|
| `page` | int | 0-indexed page number |
| `size` | int | Page size (default 20) |
| `startDate` / `endDate` | `yyyy-MM-dd` | Date range filter |
| `accountId` | Long | Filter by account |
| `categoryId` | Long | Filter by category |
| `type` | String | `INCOME` / `EXPENSE` / `TRANSFER` |
| `search` | String | Search in description |
| `sortBy` / `sortDir` | String | Sort field + `ASC`/`DESC` |
| `period` | String | `LAST_WEEK`, `LAST_MONTH`, `THIS_MONTH`, `LAST_3_MONTHS`, `CUSTOM` |

## Analytics Query Parameters

All analytics endpoints accept: `startDate`, `endDate` (`yyyy-MM-dd`), `accountId` (optional Long).
Excluded categories are applied automatically from `DashboardConfig`.

---

## Infrastructure & Security

See [docs/features/api/infrastructure.md](features/api/infrastructure.md) for:
- JWT token details (HMAC-SHA512, env vars, expiry)
- Security filter chain (MdcFilter → JwtAuthFilter)
- Public routes vs protected routes
- Error handling / GlobalExceptionHandler
- Data ownership (all queries filtered by userId)
- Structured logging (MDC keys, logback profiles)
