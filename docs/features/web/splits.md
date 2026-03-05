---
title: Splits & Friends — Web
feature: splits
layer: web
module: SplitsModule (lazy, /splits)
components:
  - SplitsPageComponent
  - SplitFriendsCellRendererComponent
  - SplitActionsCellRendererComponent
related:
  - docs/features/api/splits.md
  - docs/features/web/transactions.md  (inline split dialog)
---

# Splits & Friends — Web

## Overview

Manage friend contacts, split expenses among friends, track balances, settle debts, and export per-friend CSV reports. Also integrates inline split dialog into the Transactions page.

---

## Module

`SplitsModule` — lazy-loaded at `/splits`.

**Module providers:** `[MessageService, ConfirmationService]` (per MISTAKES.md rule).

---

## SplitsPage Layout

Matches Stitch reference `split-transactions.html`:

1. **Header** — "Splits" title + "Add Friend" / "Add Split" buttons
2. **Friends card** — Horizontal scroll of friend avatar circles (initials in colored circles, or avatar image) + dashed "Add" button
3. **Summary cards (2-col)** — "You are owed" (green) / "You owe" (orange) with formatted amounts
4. **Pending settlements** — Cards per friend: avatar, name, balance (+/-), "Settle" + "Export CSV" buttons
5. **AG Grid** — Split transactions: Date, Description, Category, Split With (avatar circles), Total, Your Share, Actions

AG Grid uses `themeQuartz.withParams()` with `--minted-*` CSS vars (same pattern as transactions).

---

## Dialogs (`p-dialog`)

### Add/Edit Friend
- Fields: Name, Email, Phone, Avatar color picker (8 preset colors)
- Edit mode includes `<app-avatar-upload>` for friend avatar

### Split Transaction
- Fields: Description, Category, Total Amount, Date
- Source transaction link (optional, from transaction split button)
- Split type radios: Equal / Unequal / By Share
- Friend multi-select with per-friend share amounts/percentages
- Summary box showing split breakdown

### Settlement Review
- Friend avatar, total amount
- Itemized unsettled shares list
- Confirm button

---

## Split Types

| Type | Icon | Behavior |
|------|------|---------|
| Equal | `pi-equals` | Auto-divides; remainder to payer |
| Unequal | `pi-chart-pie` | Manual amount per friend |
| By Share | `pi-percentage` | Percentage input with live amount calculation |

---

## Avatar Support

- Friends show avatar images (base64 data URI) when available, initials fallback
- Avatar circles in: friend ring, balance cards, split shares, settle dialog, available friends pills
- Edit friend dialog uses `<app-avatar-upload>` component
- `FriendResponse.avatarBase64` — `data:{contentType};base64,{encoded}` or null

---

## Services

### `FriendService` (`core/services/friend.service.ts`)
Standard CRUD for `/api/v1/friends`:
- `getAll()`, `getById(id)`, `create(request)`, `update(id, request)`, `delete(id)`
- `uploadAvatar(id, file)`, `deleteAvatar(id)`

### `SplitService` (`core/services/split.service.ts`)
- CRUD: `getAll()`, `getById(id)`, `create(request)`, `update(id, request)`, `delete(id)`
- Analytics: `getBalanceSummary()`, `getFriendBalances()`
- Settlement: `settle(request)`
- Export: `getSharesByFriend(friendId)`, `exportFriendShares(friendId)` — client-side CSV generation

---

## Models

### `core/models/friend.model.ts`
```typescript
interface FriendRequest {
  name: string;
  email?: string;
  phone?: string;
  avatarColor?: string;
}

interface FriendResponse {
  id: number;
  name: string;
  email: string | null;
  phone: string | null;
  avatarColor: string;
  avatarBase64: string | null;
  isActive: boolean;
}
```

### `core/models/split.model.ts`
```typescript
type SplitType = 'EQUAL' | 'UNEQUAL' | 'SHARE';

interface SplitTransactionRequest {
  description: string;
  categoryName: string;
  totalAmount: number;
  splitType: SplitType;
  transactionDate: string;
  transactionId?: number;
  shares: SplitShareRequest[];
}

interface SplitShareRequest {
  friendId: number | null;   // null = "Me" (payer)
  shareAmount: number;
  sharePercentage?: number;
  isPayer: boolean;
}

interface SplitBalanceSummaryResponse {
  youAreOwed: number;
  youOwe: number;
}

interface FriendBalanceResponse {
  friendId: number;
  friendName: string;
  avatarColor: string;
  balance: number;   // positive = friend owes you, negative = you owe friend
}

interface SettleRequest {
  friendId: number;
}
```
