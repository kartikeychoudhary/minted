---
title: Settings — Web
feature: settings
layer: web
module: SettingsModule (lazy, /settings)
components:
  - SettingsComponent (container with tabs)
  - AccountTypeSettingsComponent
  - AccountSettingsComponent
  - CategorySettingsComponent
  - BudgetSettingsComponent
  - ProfileSettingsComponent
  - LlmConfigComponent
  - MerchantMappingsComponent
related:
  - docs/features/api/accounts.md
  - docs/features/api/transactions.md    (categories)
  - docs/features/api/budgets.md
  - docs/features/api/statements.md     (LLM config tab)
---

# Settings — Web

## Overview

PrimeNG tab-based settings page with tabs for Accounts, Account Types, Categories, Budgets, Dashboard Config, Profile, and LLM Configuration.

---

## Module

`SettingsModule` — lazy-loaded at `/settings`.

---

## Tab: Accounts

- `p-table` listing all active accounts: Name, Type, Balance, Currency, Status
- Add/Edit via inline or dialog
- Delete → soft delete (`isActive = false`); account disappears from UI
- Creating with same name as soft-deleted account → restores the deleted record
- `refreshData()` reloads both accounts and account types (called on tab activation)

---

## Tab: Account Types

- Table: Name, Description, Icon (PrimeNG icon class)
- Add / Edit / Soft Delete (`isActive = false`)
- Soft-deleted types shown at bottom: strikethrough text, 55% opacity, red "Deleted" badge, "Undo" restore button
- Active types sorted first, inactive last
- Restore calls `PATCH /account-types/{id}/toggle`
- Tab switch from Account Types → Accounts triggers `refreshData()` (prevents stale dropdown data)

---

## Tab: Categories

- Grouped/tree table of categories with sub-categories
- Filter by type (Income / Expense / Transfer)
- Add / Edit / Deactivate
- Icon picker and color picker (`p-colorpicker`) per category

---

## Tab: Budgets

- Month/Year selector at top
- Table: Category, Budgeted Amount, Spent Amount, Remaining, Progress Bar (`p-progressbar`)
- Add / Edit budget amounts
- Visual utilization indicator per budget

---

## Tab: Dashboard Config

**Chart Color Palette:**
- Color list with `p-colorpicker` for each color + remove button
- "Add Color" button
- 9 preset palette buttons: Minted, Pastel, Vibrant, Ocean, Sunset, Forest, Berry, Earth, Neon
- Color preview bar showing all current colors as horizontal segments
- Colors stored in `localStorage` via `DashboardConfigService.saveChartColors()`
- Colors normalized with `#` prefix (handles PrimeNG ColorPicker hex format)

**Excluded Categories (optional):**
- Multi-select for categories to exclude from analytics

---

## Tab: Profile

- Display name, email, username (read-only)
- Avatar upload via `<app-avatar-upload>` (size="lg")
- Change password button → navigates to `/auth/change-password`
- Theme toggle (light/dark)
- Currency selector

---

## Tab: LLM Configuration (value="5")

Two sections via `LlmConfigComponent`:

1. **API & Model Configuration**
   - Model dropdown (`p-select`, active models only)
   - Password input for Gemini API key (placeholder adapts if key already saved)
   - Info card with Gemini API key setup instructions
   - Save button

2. **Merchant Mappings** (`MerchantMappingsComponent`)
   - AG Grid (inline editing, `domLayout: 'autoHeight'`)
   - Columns: Keywords (editable), Category (editable select + color dot), Delete
   - Row-by-row save via `onCellValueChanged`
   - "Add Mapping" → adds empty editable row
   - Delete with `ConfirmationService`
   - Empty state message

---

## Notes

- **Module providers:** `MessageService` and `ConfirmationService` must be in `providers[]` of `SettingsModule` (not root)
- **AG Grid in settings:** `AgGridModule` imported in `SettingsModule` for merchant mappings grid
