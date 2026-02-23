# Bulk Transaction Importer — Implementation Progress

> **Feature:** CSV-based bulk transaction import with 3-step wizard, AG Grid preview, background job processing, and user-accessible import history page.
>
> **Started:** 2026-02-20

---

## Phase 0: Pre-Flight

- [x] Read `docs/MISTAKES.md` in full
- [x] Confirm backend compiles: `cd minted-api && ./gradlew compileJava`
- [x] Confirm frontend compiles: `cd minted-web && ng build`

---

## Phase 1: Backend — Database Migration

- [x] Create `V0_0_20__create_bulk_imports_table.sql`
  - `bulk_imports` table with all columns (user_id, account_id, import_type, file_name, file_size, row counts, status, csv_data, validation_result, skip_duplicates, etc.)
  - Indexes on user_id and status
  - Seed `BULK_IMPORT_PROCESSOR` job schedule config
- [x] Verify migration file syntax

**File:** `minted-api/src/main/resources/db/migration/V0_0_20__create_bulk_imports_table.sql`

---

## Phase 2: Backend — Enums

- [x] Create `ImportStatus.java` (PENDING, VALIDATING, VALIDATED, IMPORTING, COMPLETED, FAILED)
- [x] Create `ImportType.java` (CSV, CREDIT_CARD_STATEMENT)

**Files:**
- `minted-api/src/main/java/com/minted/api/enums/ImportStatus.java`
- `minted-api/src/main/java/com/minted/api/enums/ImportType.java`

---

## Phase 3: Backend — Entity

- [x] Create `BulkImport.java` entity
  - All JPA annotations, Lombok (`@Getter @Setter @NoArgsConstructor @AllArgsConstructor`)
  - Relationships: ManyToOne to User, Account, JobExecution
  - `skipDuplicates` field added for persist across async processing

**File:** `minted-api/src/main/java/com/minted/api/entity/BulkImport.java`

---

## Phase 4: Backend — Repository

- [x] Create `BulkImportRepository.java`
  - `findByUserIdOrderByCreatedAtDesc`, `findByIdAndUserId`, `findByStatus`
- [x] Add `findByNameIgnoreCaseAndUserIdAndType` to `TransactionCategoryRepository.java`
- [x] Add `existsByTransactionDateAndAmountAndDescriptionAndAccountIdAndUserId` to `TransactionRepository.java`

**Files:**
- `minted-api/src/main/java/com/minted/api/repository/BulkImportRepository.java` (new)
- `minted-api/src/main/java/com/minted/api/repository/TransactionCategoryRepository.java` (modified)
- `minted-api/src/main/java/com/minted/api/repository/TransactionRepository.java` (modified)

---

## Phase 5: Backend — DTOs

- [x] Create `CsvRowPreview.java` (rowNumber, date, amount, type, description, categoryName, notes, tags, status, errorMessage, matchedCategoryId, isDuplicate)
- [x] Create `CsvUploadResponse.java` (importId, totalRows, validRows, errorRows, duplicateRows, rows)
- [x] Create `BulkImportConfirmRequest.java` (importId, skipDuplicates with @NotNull)
- [x] Create `BulkImportResponse.java` (with `from(BulkImport)` factory method)

**Files:**
- `minted-api/src/main/java/com/minted/api/dto/CsvRowPreview.java`
- `minted-api/src/main/java/com/minted/api/dto/CsvUploadResponse.java`
- `minted-api/src/main/java/com/minted/api/dto/BulkImportConfirmRequest.java`
- `minted-api/src/main/java/com/minted/api/dto/BulkImportResponse.java`

---

## Phase 6: Backend — Service

- [x] Create `BulkImportService.java` interface
- [x] Create `BulkImportServiceImpl.java` implementation
  - `getCsvTemplate()` — returns sample CSV bytes
  - `uploadAndValidate()` — synchronous CSV parsing + validation + duplicate checking + entity save
  - `confirmImport()` — sets status to IMPORTING, creates JobExecution, triggers async processing via `CompletableFuture.runAsync()`
  - `getUserImports()` — returns user's import history
  - `getImportById()` — returns single import details
  - `getImportJobDetails()` — returns linked JobExecution with steps
  - CSV parser handles quoted fields with commas
  - Per-row validation: date format, amount > 0, type enum, description not blank, category name matching by type
  - Duplicate detection via `existsByTransactionDateAndAmountAndDescriptionAndAccountIdAndUserId`

**Files:**
- `minted-api/src/main/java/com/minted/api/service/BulkImportService.java`
- `minted-api/src/main/java/com/minted/api/service/impl/BulkImportServiceImpl.java`

---

## Phase 7: Backend — Job

- [x] Create `BulkImportJob.java`
  - Implements `Runnable`, `@Component`, `@PostConstruct` registration (same pattern as RecurringTransactionJob)
  - `run()` method sweeps for stuck imports with status=IMPORTING and no active job
  - Async processing method (`processImportAsync`) with 4 steps:
    1. Re-validate CSV
    2. Check Duplicates (applies skipDuplicates flag)
    3. Insert Transactions (with account balance updates per TransactionServiceImpl pattern)
    4. Summary
  - Step helpers: `createStep()`, `completeStep()`, `failStep()` — identical pattern to RecurringTransactionJob

**File:** `minted-api/src/main/java/com/minted/api/job/BulkImportJob.java`

---

## Phase 8: Backend — Controller

- [x] Create `BulkImportController.java`
  - `GET /api/v1/imports/template` — download CSV template
  - `POST /api/v1/imports/upload` — upload + validate CSV (multipart file + accountId)
  - `POST /api/v1/imports/confirm` — confirm import (returns 202 Accepted)
  - `GET /api/v1/imports` — list user's imports
  - `GET /api/v1/imports/{id}` — single import
  - `GET /api/v1/imports/{id}/job` — job execution details with steps
  - Authentication: uses existing SecurityConfig pattern (`getUserId(Authentication)`)
- [x] **Backend compiles: `./gradlew compileJava` — BUILD SUCCESSFUL**

**File:** `minted-api/src/main/java/com/minted/api/controller/BulkImportController.java`

---

## Phase 9: Frontend — Model

- [x] Create `import.model.ts`
  - `BulkImportResponse`, `CsvUploadResponse`, `CsvRowPreview`, `BulkImportConfirmRequest`, `ImportStatus` type

**File:** `minted-web/src/app/core/models/import.model.ts`

---

## Phase 10: Frontend — Service

- [x] Create `import.service.ts`
  - `providedIn: 'root'`, `HttpClient`, `map(r => r.data)` pattern
  - Methods: `downloadTemplate()`, `uploadCsv()`, `confirmImport()`, `getUserImports()`, `getImportById()`, `getImportJobDetails()`

**File:** `minted-web/src/app/core/services/import.service.ts`

---

## Phase 11: Frontend — Module & Routing

- [x] Add `StepperModule` to SharedModule's `PRIMENG_MODULES` array
- [x] Create `import-module.ts` with declarations, imports, providers (MessageService, ConfirmationService)
- [x] Create `import-routing-module.ts` with routes: `''` → ImportWizard, `'jobs'` → ImportJobs, `'jobs/:id'` → ImportJobDetail
- [x] Add lazy-loaded `import` route to `app-routing-module.ts` (after analytics, before admin)

**Files:**
- `minted-web/src/app/shared/shared.module.ts` (modified)
- `minted-web/src/app/modules/import/import-module.ts` (new)
- `minted-web/src/app/modules/import/import-routing-module.ts` (new)
- `minted-web/src/app/app-routing-module.ts` (modified)

---

## Phase 12: Frontend — Components (Scaffolding)

- [x] Create StatusCellRendererComponent (AG Grid cell renderer for status badges)
  - Implements `ICellRendererAngularComp` with `agInit()` and `refresh()`
  - Color-coded badges: VALID (green), ERROR (red), DUPLICATE (amber), COMPLETED (green), FAILED (red), IMPORTING/RUNNING (blue), VALIDATED/PENDING (gray)
- [x] All components use `standalone: false`

**File:** `minted-web/src/app/modules/import/components/cell-renderers/status-cell-renderer.ts`

---

## Phase 13: Frontend — Import Wizard Component

- [x] Create `import-wizard.ts` — 3-step wizard with PrimeNG Stepper
  - Step 1: Account selection dropdown + Download template button + CSV format instructions
  - Step 2: File input + Upload button + Summary cards (Total/Valid/Errors/Duplicates) + AG Grid preview
  - Step 3: Import summary + Skip duplicates toggle + Start Import button + Post-import success with navigation
  - AG Grid theme: exact copy of minted theme from transactions-list
  - Column defs: #, Status (StatusCellRendererComponent), Date, Amount (CurrencyService.format), Type, Description, Category, Error
  - All styling uses `var(--minted-*)` CSS variables
- [x] Create `import-wizard.html` — full template with PrimeNG `<p-stepper>` / `<p-step-list>` / `<p-step-panels>` syntax
- [x] Create `import-wizard.scss` — stepper step number styling overrides

**Files:**
- `minted-web/src/app/modules/import/components/import-wizard/import-wizard.ts`
- `minted-web/src/app/modules/import/components/import-wizard/import-wizard.html`
- `minted-web/src/app/modules/import/components/import-wizard/import-wizard.scss`

---

## Phase 14: Frontend — Import Jobs & Job Detail Components

- [x] Create `import-jobs.ts` + `import-jobs.html` + `import-jobs.scss`
  - AG Grid table: ID, File Name, Account, Status (StatusCellRendererComponent), Rows (imported/total), Created, Actions (View button)
  - Same minted theme, pagination, overlayNoRowsTemplate
- [x] Create `import-job-detail.ts` + `import-job-detail.html` + `import-job-detail.scss`
  - Import metadata cards (Account, File Size, Created, Import Type)
  - Row stat cards (Total, Valid, Duplicates, Errors, Imported) with semantic colors
  - Auto-refresh polling: `interval(5000)` with `takeWhile()` while status is IMPORTING
  - Job execution step timeline (same pattern as admin JobDetail)
  - Error display, polling indicator

**Files:**
- `minted-web/src/app/modules/import/components/import-jobs/import-jobs.ts`
- `minted-web/src/app/modules/import/components/import-jobs/import-jobs.html`
- `minted-web/src/app/modules/import/components/import-jobs/import-jobs.scss`
- `minted-web/src/app/modules/import/components/import-job-detail/import-job-detail.ts`
- `minted-web/src/app/modules/import/components/import-job-detail/import-job-detail.html`
- `minted-web/src/app/modules/import/components/import-job-detail/import-job-detail.scss`

---

## Phase 15: Frontend — Sidebar Update

- [x] Add `{ label: 'Import', icon: 'upload_file', route: '/import' }` to `baseNavigationItems` array in `sidebar.ts` (after Recurring, before Analytics)

**File:** `minted-web/src/app/layout/components/sidebar/sidebar.ts` (modified)

---

## Phase 16: Verification

- [x] Backend compiles: `cd minted-api && ./gradlew compileJava` — **BUILD SUCCESSFUL**
- [x] Frontend compiles: `cd minted-web && ng build` — **BUILD SUCCESSFUL** (import-module lazy chunk: 40.65 kB)
- [ ] Integration testing (requires running backend + frontend + database)

### Integration Test Checklist (manual)
- [ ] Sidebar shows "Import" navigation item
- [ ] `/import` route loads the wizard component
- [ ] Step 1: Account dropdown loads accounts, template downloads as CSV
- [ ] Step 2: CSV upload returns parsed preview, AG Grid shows rows with status badges
- [ ] Step 3: Confirm triggers import job, shows success with link to job detail
- [ ] `/import/jobs` shows import history
- [ ] `/import/jobs/:id` shows job detail with steps and auto-refresh
- [ ] Imported transactions appear in `/transactions` page
- [ ] Account balances are correctly updated after import

---

## Files Summary

### New files (27 total)

**Backend (13 files):**
1. [x] `minted-api/src/main/resources/db/migration/V0_0_20__create_bulk_imports_table.sql`
2. [x] `minted-api/src/main/java/com/minted/api/enums/ImportStatus.java`
3. [x] `minted-api/src/main/java/com/minted/api/enums/ImportType.java`
4. [x] `minted-api/src/main/java/com/minted/api/entity/BulkImport.java`
5. [x] `minted-api/src/main/java/com/minted/api/repository/BulkImportRepository.java`
6. [x] `minted-api/src/main/java/com/minted/api/dto/BulkImportResponse.java`
7. [x] `minted-api/src/main/java/com/minted/api/dto/CsvUploadResponse.java`
8. [x] `minted-api/src/main/java/com/minted/api/dto/CsvRowPreview.java`
9. [x] `minted-api/src/main/java/com/minted/api/dto/BulkImportConfirmRequest.java`
10. [x] `minted-api/src/main/java/com/minted/api/service/BulkImportService.java`
11. [x] `minted-api/src/main/java/com/minted/api/service/impl/BulkImportServiceImpl.java`
12. [x] `minted-api/src/main/java/com/minted/api/job/BulkImportJob.java`
13. [x] `minted-api/src/main/java/com/minted/api/controller/BulkImportController.java`

**Frontend (14 files):**
14. [x] `minted-web/src/app/core/models/import.model.ts`
15. [x] `minted-web/src/app/core/services/import.service.ts`
16. [x] `minted-web/src/app/modules/import/import-module.ts`
17. [x] `minted-web/src/app/modules/import/import-routing-module.ts`
18. [x] `minted-web/src/app/modules/import/components/import-wizard/import-wizard.ts`
19. [x] `minted-web/src/app/modules/import/components/import-wizard/import-wizard.html`
20. [x] `minted-web/src/app/modules/import/components/import-wizard/import-wizard.scss`
21. [x] `minted-web/src/app/modules/import/components/import-jobs/import-jobs.ts`
22. [x] `minted-web/src/app/modules/import/components/import-jobs/import-jobs.html`
23. [x] `minted-web/src/app/modules/import/components/import-jobs/import-jobs.scss`
24. [x] `minted-web/src/app/modules/import/components/import-job-detail/import-job-detail.ts`
25. [x] `minted-web/src/app/modules/import/components/import-job-detail/import-job-detail.html`
26. [x] `minted-web/src/app/modules/import/components/import-job-detail/import-job-detail.scss`
27. [x] `minted-web/src/app/modules/import/components/cell-renderers/status-cell-renderer.ts`

### Modified files (5 total)
1. [x] `minted-web/src/app/shared/shared.module.ts` — Added `StepperModule`
2. [x] `minted-web/src/app/app-routing-module.ts` — Added import lazy route
3. [x] `minted-web/src/app/layout/components/sidebar/sidebar.ts` — Added Import nav item
4. [x] `minted-api/src/main/java/com/minted/api/repository/TransactionCategoryRepository.java` — Added name lookup method
5. [x] `minted-api/src/main/java/com/minted/api/repository/TransactionRepository.java` — Added duplicate check method

---

## Implementation Notes

- **No `@EnableAsync` needed** — used `CompletableFuture.runAsync()` for async processing instead of Spring's `@Async` annotation
- **`skipDuplicates` persisted on entity** — added `skip_duplicates` column to `bulk_imports` table so the flag survives across async processing
- **No new dependencies added** — all functionality built with existing Spring Boot + Angular + PrimeNG + AG Grid stack
- **PrimeNG Stepper** — used `<p-stepper>`, `<p-step-list>`, `<p-step>`, `<p-step-panels>`, `<p-step-panel>` with `#content` template and `activateCallback`
- **AG Grid theme** — exact copy from transactions-list.ts to maintain visual consistency
- **StatusCellRendererComponent** — reusable across preview grid and import history grid
- **Auto-refresh polling** — `interval(5000)` with `takeWhile()` and proper `ngOnDestroy` cleanup
