---
title: Notifications — Web
feature: notifications
layer: web
module: NotificationsModule (lazy, /notifications)
components:
  - NotificationsListComponent
  - Header bell icon + p-drawer (in LayoutModule)
related:
  - docs/features/api/notifications.md
  - docs/features/web/layout.md
---

# Notifications — Web

## Overview

Header bell icon with unread badge, right-side drawer for quick access, and full notifications page. Unread count polls every 30 seconds.

---

## Module

`NotificationsModule` — lazy-loaded at `/notifications`.

---

## NotificationService (`core/services/notification.service.ts`)

Singleton (`providedIn: 'root'`) — reactive state via BehaviorSubjects.

| Observable | Type | Description |
|------------|------|-------------|
| `unreadCount$` | `BehaviorSubject<number>` | Badge count |
| `notifications$` | `BehaviorSubject<NotificationResponse[]>` | Accumulated list |
| `loading$` | `BehaviorSubject<boolean>` | True while fetching |
| `markingAllRead$` | `BehaviorSubject<boolean>` | True during mark all read |
| `clearing$` | `BehaviorSubject<boolean>` | True during clear all read |
| `hasMore` | `boolean` | More pages available |
| `hasUnread` | getter: `boolean` | True if unreadCount > 0 |

| Method | Description |
|--------|-------------|
| `startPolling()` | 30s `interval()` for unread count |
| `stopPolling()` | Cancel polling |
| `fetchUnreadCount()` | Single GET for badge |
| `loadNotifications(page)` | GET paginated, replaces list |
| `loadMore()` | GET next page, appends |
| `markAsRead(id)` | PUT + optimistic local update |
| `markAllAsRead()` | PUT + optimistic local update |
| `dismiss(id)` | DELETE + remove from local list |
| `dismissAllRead()` | DELETE all read + update local list |

**Polling lifecycle:** Managed in `LayoutComponent` via `authService.currentUser$` subscription. Starts on login, stops on logout. Avoids circular dependency (AuthService ↔ NotificationService ↔ JwtInterceptor ↔ AuthService).

---

## Header Bell Icon + Drawer (LayoutModule)

- `pi-bell` icon with unread badge (capped at 99+)
- Click toggles PrimeNG `<p-drawer>` from right side (400px / 100vw on mobile)

**Drawer contents:**
- Header: "Notifications" + "Mark all read" (loading binding to `markingAllRead$`) + "Clear all read" (loading binding to `clearing$`)
- Loading state: `p-progressSpinner`
- Empty state: checkmark icon + "All caught up!"
- Notification cards: type icon (colored), title, message, relative time, unread accent bar, hover dismiss button
- Click card → marks as read
- "See all notifications" → `/notifications` (with delayed navigation + DOM cleanup of `.p-drawer-mask`)
- "Load more" button when `hasMore`
- "Clear all read" warns via toast if unread exist

---

## NotificationsListComponent (`/notifications`)

Full-page notification center:
- Title + subtitle
- Global actions: "Mark all as read" (when unread exist), "Clear all" (with confirm dialog)
- Skeleton loading (3 skeleton cards)
- Empty state: "All caught up!" + back-to-dashboard button
- **Date-grouped sections:** Today, Yesterday, Earlier this Week, Older
- Notification cards: type icon with colored background, title (bold if unread), message, relative time, hover dismiss button, unread accent bar
- "Load more" pagination button

---

## Design Notes

- Added `--minted-warning` and `--minted-warning-subtle` CSS vars (light + dark mode) for WARNING-type notifications
- PrimeNG Drawer overrides added to `styles.scss` using `--minted-*` variables
- Drawer width: `min(400px, 100vw)` — fills screen on mobile

---

## Models (`core/models/notification.model.ts`)

```typescript
type NotificationType = 'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR' | 'SYSTEM';

interface NotificationResponse {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

interface NotificationPage {
  content: NotificationResponse[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
```
