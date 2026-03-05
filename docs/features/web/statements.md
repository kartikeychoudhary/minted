---
title: Credit Card Statement Parser — Web
feature: statements
layer: web
module: StatementModule (lazy, /statements)
components:
  - StatementListComponent
  - UploadStepComponent
  - StatementDetailComponent
  - TextReviewStepComponent
  - ParsePreviewStepComponent
  - ConfirmStepComponent
related:
  - docs/features/api/statements.md
  - docs/features/web/settings.md   (LLM Configuration tab)
  - docs/features/web/admin.md      (LLM models, feature toggles)
---

# Credit Card Statement Parser — Web

## Overview

4-step PDF statement parsing workflow: upload PDF → review extracted text → review AI-parsed transactions → confirm import. Plus LLM configuration and merchant mapping rules in Settings.

---

## Module

`StatementModule` — lazy-loaded at `/statements`.

**Child routes:**
- `''` → `StatementListComponent`
- `'new'` → `UploadStepComponent`
- `':id'` → `StatementDetailComponent`

---

## Components

### StatementListComponent (`/statements`)
- Cards showing: filename, account, file size, date, step progress, status badge (`p-tag`)
- "Parse New Statement" button → navigates to `/statements/new`
- Empty state with CTA

### UploadStepComponent (`/statements/new`)
- Native file input for PDF (drag-to-select styling, max 20MB, PDF only)
- Account dropdown (`p-select`, from AccountService)
- Optional password field (toggle via checkbox) for password-protected PDFs
- On submit: `statementService.upload()` → navigates to `/statements/{id}`

### StatementDetailComponent (`/statements/:id`)
- Custom 4-step progress indicator (not PrimeNG stepper)
- Dynamically renders child step components based on `statement.currentStep`
- **Polls status every 3 seconds** while waiting for async LLM parsing
- Error banner for FAILED status

### TextReviewStep (Step 2)
- Read-only textarea showing extracted text with character count
- "Send to AI for Parsing" button → `triggerParse()`
- Spinner + message while parsing
- Triggers parent polling on parse start

### ParsePreviewStep (Step 3)
AG Grid with minted theme (`themeQuartz.withParams()` with `--minted-*` vars).

| Column | Notes |
|--------|-------|
| Duplicate indicator | Warning icon for duplicates |
| Date | Editable |
| Description | Editable |
| Amount | Colored, editable |
| Type | Editable select (INCOME/EXPENSE) |
| Category | Editable select; rule icon shown when `mappedByRule=true` |
| Notes | Editable |

- Duplicate rows: amber warning background + left border
- "Skip duplicate transactions" checkbox (default checked)
- "Import Transactions" button with confirmation dialog
- Summary: total / duplicate / import counts

### ConfirmStep (Step 4)
- Success card with check icon
- Stats: Parsed, Duplicates, Imported
- Navigation to Transactions page or Statements list

---

## Settings Integration: LLM Configuration Tab

New tab in Settings page (value="5"):

**LlmConfigComponent** — Two sections:
1. **API & Model Config** — Model dropdown (`p-select`), API key password input (shows placeholder if key exists), Gemini setup instructions card, Save button
2. **Merchant Mappings** (`MerchantMappingsComponent`) — AG Grid with inline editing
   - Columns: Keywords (editable), Category (editable select with color dot), Delete
   - Row-by-row save via `onCellValueChanged`
   - "Add Mapping" button adds empty editable row
   - Delete with confirmation dialog

---

## Services

### `StatementService` (`core/services/statement.service.ts`)
| Method | Call |
|--------|------|
| `upload(file, accountId, pdfPassword?)` | POST `/statements/upload` (multipart) |
| `triggerParse(statementId)` | POST `/statements/{id}/parse` |
| `getParsedRows(statementId)` | GET `/statements/{id}/parsed-rows` |
| `confirmImport(request)` | POST `/statements/confirm` |
| `getStatements()` | GET `/statements` |
| `getStatement(id)` | GET `/statements/{id}` |

### `LlmConfigService` (`core/services/llm-config.service.ts`)
- User config: `getConfig()`, `saveConfig(request)`, `getAvailableModels()`
- Merchant mappings: `getMappings()`, `createMapping()`, `updateMapping()`, `deleteMapping()`
- Admin models: `getAllModels()`, `createModel()`, `updateModel()`, `deleteModel()`

---

## Models

**`core/models/statement.model.ts`:**
- `StatementStatus` — `'UPLOADED' | 'TEXT_EXTRACTED' | 'LLM_PARSED' | 'CONFIRMING' | 'COMPLETED' | 'FAILED'`
- `CreditCardStatement` — id, accountId, fileName, fileSize, status, currentStep, extractedText, parsedCount, duplicateCount, importedCount, errorMessage, jobExecutionId
- `ParsedTransactionRow` — tempId, amount, type, description, transactionDate, categoryName, matchedCategoryId, notes, tags, isDuplicate, duplicateReason, mappedByRule
- `ConfirmStatementRequest` — statementId, skipDuplicates

**`core/models/llm-config.model.ts`:**
- `LlmModel` — id, name, provider, modelKey, description, isActive, isDefault
- `LlmConfig` — id, provider, hasApiKey, selectedModel, merchantMappings[]
- `MerchantMapping` — id, snippets, snippetList[], categoryId, categoryName, categoryIcon, categoryColor
