---
title: Layout — Web
feature: layout
layer: web
module: LayoutModule
components:
  - MainLayoutComponent
  - SidebarComponent
  - HeaderComponent
  - FooterComponent
related:
  - docs/features/web/notifications.md  (header bell + drawer)
  - docs/features/web/theme.md          (dark mode toggle in header)
---

# Layout — Web

## Overview

App shell with persistent sidebar (desktop) / hamburger drawer (mobile), header with notifications bell, and route loading bar.

---

## Module

`LayoutModule` — not lazy-loaded. Wraps all authenticated pages.

**Providers in LayoutModule:** `MessageService` (for notification warning toasts in drawer).

---

## MainLayoutComponent

Container for authenticated app:
- `<app-sidebar>` (hidden on mobile)
- `<app-header>`
- `<router-outlet>`
- Route loading bar
- Notification drawer

**Route loading bar:** Subscribes to Router events. `NavigationStart` → `isRouteLoading=true`. `NavigationEnd/Cancel/Error` → `isRouteLoading=false`. Renders a 3px animated bar at top of content area using `var(--minted-accent)` color (`route-loading-slide` keyframes in `layout.scss`).

**Notification polling:** Managed here via `authService.currentUser$` — calls `notificationService.startPolling()` on login, `stopPolling()` on logout.

---

## SidebarComponent

Navigation sidebar with all app routes.

**Navigation structure (built by `buildNavigation(role)`):**

| Label | Icon | Route |
|-------|------|-------|
| Dashboard | `pi pi-home` | `/dashboard` |
| Transactions | `pi pi-list` | `/transactions` |
| Recurring | `pi pi-refresh` | `/recurring` |
| Import | `pi pi-upload` | `/import` |
| Analytics | `pi pi-chart-bar` | `/analytics` |
| Statements | `pi pi-file-pdf` | `/statements` |
| Splits | `pi pi-users` | `/splits` |
| Notifications | `pi pi-bell` | `/notifications` |
| Settings | `pi pi-cog` | `/settings` |

**Admin section (role=ADMIN only):**

| Label | Icon | Route |
|-------|------|-------|
| Users | `pi pi-user` | `/admin/users` |
| Server Jobs | `pi pi-clock` | `/admin/jobs` |
| Server Settings | `pi pi-server` | `/admin/settings` |

**Avatar:** Shows `<img>` when `userAvatar` getter finds base64 in localStorage, initials fallback otherwise.

**Mobile:** Sidebar hidden via `hidden md:block`. Hamburger button visible only on mobile (`md:hidden`). Auto-closes on navigation.

---

## HeaderComponent

- App name / branding (shown on desktop)
- Dark mode toggle button
- Notification bell with unread badge → toggles `p-drawer`
- User menu or profile link

**Notification drawer** (`p-drawer`, right side, 400px / `min(400px, 100vw)` on mobile):
- See [notifications.md](notifications.md) for full drawer spec

---

## Mobile Behavior

| Element | Mobile Behavior |
|---------|-----------------|
| Sidebar | `hidden md:block` — hidden on < 768px |
| Mobile nav | Hamburger (`pi pi-bars`) + PrimeNG `<p-drawer>` from left with full sidebar content |
| Dialogs | `max-width: 90vw` via global `@media (max-width: 767px)` in `styles.scss` |
| AG Grid | `height: 60vh !important; min-height: 300px` globally on mobile |
| Notification drawer | `min(400px, 100vw)` |
| Header | `px-4 md:px-8` padding |

---

## Routing

```typescript
// app-routing.module.ts
{
  path: '',
  component: MainLayoutComponent,
  canActivate: [AuthGuard],
  children: [
    { path: 'dashboard', loadChildren: ... },
    { path: 'transactions', loadChildren: ... },
    { path: 'settings', loadChildren: ... },
    { path: 'recurring', loadChildren: ... },
    { path: 'import', loadChildren: ... },
    { path: 'analytics', loadChildren: ... },
    { path: 'statements', loadChildren: ... },
    { path: 'splits', loadChildren: ... },
    { path: 'notifications', loadChildren: ... },
    { path: 'admin', loadChildren: ..., canActivate: [adminGuard] },
  ]
}
```
