---
title: Bulk CSV Import — Web
feature: import
layer: web
module: ImportModule (lazy, /import)
components:
  - ImportWizardComponent
  - ImportJobsComponent
  - ImportJobDetailComponent
  - StatusCellRendererComponent
related:
  - docs/features/api/import.md
  - docs/features/web/statements.md  (linked from import type selector)
---

# Bulk CSV Import — Web

## Overview

Three-step import wizard (account select → upload & preview → confirm), import history table, and job detail view.

---

## Module

`ImportModule` — lazy-loaded at `/import`.

**Child routes:**
- `''` → `ImportWizardComponent`
- `'jobs'` → `ImportJobsComponent`
- `'jobs/:id'` → `ImportJobDetailComponent`

---

## ImportWizardComponent

PrimeNG Stepper (`<p-stepper>`, `<p-step-list>`, `<p-step-panels>` with `#content` template and `activateCallback`).

### Step 1 — Account Selection
- Import type cards: **CSV Import** (active) + **Credit Card Statement** (active, navigates to `/statements`)
- Account dropdown (`p-select`, from AccountService)
- Download template button → GET `/bulk-import/template` (blob download)
- CSV format instructions card

### Step 2 — Upload & Preview
- Native `<input type="file" accept=".csv">`
- Upload button → POST `/bulk-import/upload`
- Summary cards: Total / Valid / Errors / Duplicates (colored)
- AG Grid preview table with `StatusCellRendererComponent`
  - Columns: #, Status (badge), Date, Amount (`CurrencyService.format`), Type, Description, Category, Error
  - Same minted AG Grid theme as transactions

### Step 3 — Confirm
- Import summary stats
- "Skip duplicate transactions" toggle (`p-toggleswitch`, default checked)
- "Start Import" button → POST `/bulk-import/confirm`
- On success: shows success card with navigation to job detail

---

## ImportJobsComponent

AG Grid table — import history.

Columns: ID, File Name, Account, Status (`StatusCellRendererComponent`), Rows (imported/total), Created, Actions (View button)

---

## ImportJobDetailComponent

- Import metadata cards: Account, File Size, Created, Import Type
- Row stat cards: Total, Valid, Duplicates, Errors, Imported (semantic colors)
- **Auto-refresh:** `interval(5000)` with `takeWhile()` while status is IMPORTING — polling indicator shown
- Job execution step timeline
- Error display

---

## StatusCellRendererComponent

AG Grid cell renderer implementing `ICellRendererAngularComp`:

| Status | Color |
|--------|-------|
| VALID / COMPLETED | Green |
| ERROR / FAILED | Red |
| DUPLICATE | Amber |
| IMPORTING / RUNNING | Blue |
| VALIDATED / PENDING | Gray |

---

## Service (`core/services/import.service.ts`)

| Method | Call |
|--------|------|
| `downloadTemplate()` | GET `/bulk-import/template` (blob) |
| `uploadCsv(file, accountId)` | POST `/bulk-import/upload` (FormData) |
| `confirmImport(request)` | POST `/bulk-import/confirm` |
| `getUserImports()` | GET `/bulk-import` |
| `getImportById(id)` | GET `/bulk-import/{id}` |
| `getImportJobDetails(id)` | GET `/bulk-import/{id}/job-details` |

---

## Models (`core/models/import.model.ts`)

- `BulkImportResponse` — id, accountId, accountName, status, row counts, jobExecutionId, timestamps
- `CsvUploadResponse` — importId, counts, `CsvRowPreview[]`
- `CsvRowPreview` — rowNumber, date, amount, type, description, categoryName, status, errorMessage, matchedCategoryId, isDuplicate
- `BulkImportConfirmRequest` — `{ importId: number, skipDuplicates: boolean }`
- `ImportStatus` — `'PENDING' | 'VALIDATING' | 'VALIDATED' | 'IMPORTING' | 'COMPLETED' | 'FAILED'`
