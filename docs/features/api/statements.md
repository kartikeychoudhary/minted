---
title: Credit Card Statement Parser — API
feature: statements
layer: api
package: com.minted.api.statement, com.minted.api.llm
routes:
  - POST   /api/v1/statements/upload
  - POST   /api/v1/statements/{id}/parse
  - GET    /api/v1/statements/{id}/parsed-rows
  - POST   /api/v1/statements/{id}/confirm
  - GET    /api/v1/statements
  - GET    /api/v1/statements/{id}
  - DELETE /api/v1/statements/{id}
  - GET/PUT /api/v1/llm-config
  - GET    /api/v1/llm-config/models
  - GET/POST/PUT/DELETE /api/v1/llm-config/mappings
  - GET/POST/PUT/DELETE /api/v1/admin/llm-models
migrations: V0_0_23 (llm_models), V0_0_24 (llm_configurations), V0_0_25 (credit_card_statements), V0_0_26 (merchant_category_mappings), V0_0_27 (system settings seed)
related:
  - docs/features/api/transactions.md   (confirmed transactions created here)
  - docs/features/api/accounts.md       (balance updated on confirm)
  - docs/features/api/admin.md          (system settings CREDIT_CARD_PARSER_ENABLED, ADMIN_LLM_KEY_SHARED)
  - docs/features/api/notifications.md  (parse/import step notifications)
  - docs/features/web/statements.md
---

# Credit Card Statement Parser — API

## Overview

Upload a credit card PDF → extract text (PDFBox) → send to Google Gemini LLM → review parsed transactions → confirm import. Merchant-category mapping rules provide user-defined keyword overrides to improve LLM accuracy.

**Dependency:** `org.apache.pdfbox:pdfbox:3.0.2`

---

## Database

### V0_0_23 — `llm_models`
Admin-managed LLM model catalog (seeded with 3 Gemini models).

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | PK |
| name | VARCHAR(100) | Display name |
| provider | VARCHAR(50) | Default 'GEMINI' |
| model_key | VARCHAR(200) | API model identifier |
| description | VARCHAR(255) | |
| is_active | BOOLEAN | Hidden from users when false |
| is_default | BOOLEAN | Default model selection |

### V0_0_24 — `llm_configurations`
Per-user LLM settings. One row per user (`user_id UNIQUE`).

| Column | Type | Notes |
|--------|------|-------|
| user_id | BIGINT | FK → users (UNIQUE, CASCADE) |
| provider | VARCHAR(50) | Default 'GEMINI' |
| api_key | VARCHAR(500) | User's Gemini API key |
| model_id | BIGINT | FK → llm_models (SET NULL) |
| is_enabled | BOOLEAN | |

### V0_0_25 — `credit_card_statements`
Tracks the 4-step parsing workflow.

| Column | Type | Notes |
|--------|------|-------|
| status | VARCHAR(30) | UPLOADED, TEXT_EXTRACTED, LLM_PARSED, CONFIRMING, COMPLETED, FAILED |
| current_step | INT | 1–4 |
| extracted_text | LONGTEXT | PDFBox output |
| llm_response_json | LONGTEXT | Serialized `ParsedTransactionRow` list |
| parsed_count / duplicate_count / imported_count | INT | |
| job_execution_id | BIGINT | FK → job_executions (SET NULL) |
| pdf_password_hint | VARCHAR(20) | For password-protected PDFs |

### V0_0_26 — `merchant_category_mappings`
User-defined keyword → category override rules.

| Column | Type | Notes |
|--------|------|-------|
| snippets | VARCHAR(500) | Comma-separated keywords (e.g. "ZEPTO,BLINKIT") |
| category_id | BIGINT | FK → transaction_categories (CASCADE) |

### V0_0_27 — System Settings Seed
Adds `CREDIT_CARD_PARSER_ENABLED` (true) and `ADMIN_LLM_KEY_SHARED` (false) to `system_settings`.

---

## Parsing Workflow (4 Steps)

### Step 1 — Upload & Extract (`uploadAndExtract`)
- Validates PDF content type
- Extracts text via PDFBox 3.x `Loader.loadPDF(bytes, password)` + `PDFTextStripper`
- Handles password-protected PDFs; throws `BadRequestException` on wrong password
- Truncates extracted text at 100,000 characters
- Saves statement with `TEXT_EXTRACTED` status, fires INFO notification

### Step 2 — LLM Parse (`triggerLlmParse` + `processLlmParseAsync`)
- Resolves LLM config: user key → admin shared key → throw exception
- Creates `JobExecution`, defers async processing after transaction commit (same pattern as BulkImport)
- `processLlmParseAsync` calls `GeminiLlmService` with:
  - User's active category names (from `TransactionCategoryService`)
  - Merchant mapping rules as "ABSOLUTE RULES" (override LLM category picks)
- Runs merchant mapping pre-pass: iterates mappings, overrides `categoryName`, sets `mappedByRule=true`
- Runs duplicate detection: matches by accountId + amount + date ±1 day + first 10 chars of description
- Fires SUCCESS or ERROR notification

### Step 3 — Review (`getParsedRows`)
Frontend displays parsed rows for user review. Users can edit categories before confirming.

### Step 4 — Confirm Import (`confirmImport`)
- Accepts `modifiedRows` list (user edits) or falls back to stored AI-parsed JSON
- Creates `Transaction` entities from rows
- Updates account balance
- Sets status to COMPLETED, fires SUCCESS notification

---

## LLM Integration

**Interface:** `LlmService` (generic, supports future providers)
**Implementation:** `GeminiLlmService`
- Calls `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`
- Uses Spring `RestClient` (no extra dependencies)
- Prompt engineering:
  - **Available Categories block** — forces LLM to pick from user's actual category list
  - **Merchant hints block** — "ABSOLUTE RULES" that override the category list

---

## Endpoints

### Statement Endpoints (`/api/v1/statements`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/statements/upload` | Upload PDF + extract text (multipart: file, accountId, password?) |
| POST | `/api/v1/statements/{id}/parse` | Trigger LLM parse (async) |
| GET | `/api/v1/statements/{id}/parsed-rows` | Get AI-parsed transaction rows |
| POST | `/api/v1/statements/{id}/confirm` | Confirm import (with optional modified rows) |
| GET | `/api/v1/statements` | List all statements for user |
| GET | `/api/v1/statements/{id}` | Get statement by ID |
| DELETE | `/api/v1/statements/{id}` | Delete statement |

### LLM Config Endpoints (`/api/v1/llm-config`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/llm-config` | Get user's LLM config + merchant mappings |
| PUT | `/api/v1/llm-config` | Save LLM config (apiKey, modelId, isEnabled) |
| GET | `/api/v1/llm-config/models` | List active LLM models |
| GET | `/api/v1/llm-config/mappings` | List merchant-category mappings |
| POST | `/api/v1/llm-config/mappings` | Create mapping |
| PUT | `/api/v1/llm-config/mappings/{id}` | Update mapping |
| DELETE | `/api/v1/llm-config/mappings/{id}` | Delete mapping |

### Admin LLM Model Endpoints (`/api/v1/admin/llm-models`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/admin/llm-models` | List all LLM models |
| POST | `/api/v1/admin/llm-models` | Create model |
| PUT | `/api/v1/admin/llm-models/{id}` | Update model |
| DELETE | `/api/v1/admin/llm-models/{id}` | Delete model |

---

## Notifications

All workflow steps fire notifications via `NotificationHelper`:

| Step | Type | Title | Details |
|------|------|-------|---------|
| Upload complete | INFO | "Statement Uploaded" | Extracted text character count |
| LLM parse success | SUCCESS | "AI Parsing Complete" | Parsed + duplicate counts |
| LLM parse failure | ERROR | "AI Parsing Failed" | Error message |
| Import confirmed | SUCCESS | "Import Complete" | Imported count + account name |
