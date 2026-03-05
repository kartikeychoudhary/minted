---
title: Bulk CSV Import — API
feature: import
layer: api
package: com.minted.api.bulkimport
routes:
  - GET  /api/v1/bulk-import/template
  - POST /api/v1/bulk-import/upload
  - POST /api/v1/bulk-import/confirm
  - GET  /api/v1/bulk-import
  - GET  /api/v1/bulk-import/{id}
  - GET  /api/v1/bulk-import/{id}/job-details
migrations: V0_0_20 (bulk_imports table)
jobs: BulkImportJob (every 5 minutes)
related:
  - docs/features/api/transactions.md   (imported transactions created here)
  - docs/features/api/accounts.md       (account balance updated on import)
  - docs/features/api/admin.md          (job scheduling, manual trigger)
  - docs/features/web/import.md
---

# Bulk CSV Import — API

## Overview

Two-phase CSV import: upload + validate → confirm + async process. Supports up to 5000 rows per file. Duplicate detection is configurable via `skipDuplicates` flag.

---

## Database

### V0_0_20 — `bulk_imports`
```sql
CREATE TABLE bulk_imports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    import_type VARCHAR(30) NOT NULL DEFAULT 'CSV',
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT DEFAULT 0,
    total_rows INT NOT NULL DEFAULT 0,
    valid_rows INT NOT NULL DEFAULT 0,
    duplicate_rows INT NOT NULL DEFAULT 0,
    error_rows INT NOT NULL DEFAULT 0,
    imported_rows INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    job_execution_id BIGINT NULL,
    csv_data LONGTEXT NULL,
    validation_result JSON NULL,
    skip_duplicates BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_import_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_import_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_import_job_exec FOREIGN KEY (job_execution_id)
        REFERENCES job_executions(id) ON DELETE SET NULL
);
```

---

## Enums

| Enum | Values |
|------|--------|
| `ImportStatus` | PENDING, VALIDATING, VALIDATED, IMPORTING, COMPLETED, FAILED |
| `ImportType` | CSV, CREDIT_CARD_STATEMENT |

Both use `@JdbcTypeCode(Types.VARCHAR)` (Hibernate 6.x + MySQL VARCHAR requirement).

---

## Import Workflow

### Phase 1 — Upload & Validate (synchronous)
`POST /api/v1/bulk-import/upload` (multipart: `file` + `accountId`)

1. Parse CSV (max 5000 rows)
2. Per-row validation: date format, amount, type (INCOME/EXPENSE), category name matching
3. Duplicate detection: checks existing transactions by same account + amount + date + description prefix
4. Save raw `csvData` (LONGTEXT) and `validationResult` (JSON) to `bulk_imports`
5. Return `CsvUploadResponse` with import ID and row previews

### Phase 2 — Confirm & Async Process
`POST /api/v1/bulk-import/confirm` (body: `importId`, `skipDuplicates`)

1. Sets status to IMPORTING, creates `JobExecution`
2. Uses `TransactionSynchronizationManager.registerSynchronization(afterCommit)` to defer async processing until parent transaction commits
3. `processImportAsync()` runs in `CompletableFuture.runAsync()` wrapped in `TransactionTemplate` (avoids self-invocation proxy bypass issue)

**Async steps:**
1. **Re-validate CSV** — Re-parse stored `csvData`, re-run all validations (categories/accounts may have changed since upload)
2. **Check Duplicates** — Apply `skipDuplicates` flag
3. **Insert Transactions** — Create `Transaction` entities, update account balances (INCOME adds, EXPENSE subtracts)
4. **Summary** — Update `BulkImport` with final counts, set COMPLETED or FAILED status

---

## BulkImportJob

**File:** `com.minted.api.bulkimport.job.BulkImportJob`
**Cron:** `0 */5 * * * ?` (every 5 minutes)
**Purpose:** Sweeps for stuck imports with status=IMPORTING and no active job execution, then processes them.
**Registration:** `@PostConstruct init()` reads config from DB, calls `jobSchedulerService.registerJob()`

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/bulk-import/template` | Download sample CSV template |
| POST | `/api/v1/bulk-import/upload` | Upload + validate CSV (multipart) |
| POST | `/api/v1/bulk-import/confirm` | Confirm import (triggers async) |
| GET | `/api/v1/bulk-import` | List all imports for user |
| GET | `/api/v1/bulk-import/{id}` | Get import by ID |
| GET | `/api/v1/bulk-import/{id}/job-details` | Get linked job execution details |

---

## DTOs

| DTO | Type | Purpose |
|-----|------|---------|
| `CsvRowPreview` | record | Per-row validation (rowNumber, date, amount, type, description, categoryName, status, errorMessage, matchedCategoryId, isDuplicate) |
| `CsvUploadResponse` | record | Upload response: importId, row counts, row previews |
| `BulkImportConfirmRequest` | record | Confirm: importId, skipDuplicates |
| `BulkImportResponse` | record | Import metadata with `from(BulkImport)` factory |
