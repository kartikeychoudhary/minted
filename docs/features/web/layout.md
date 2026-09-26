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

Deep-green navigation rail (gradient using `--minted-sidebar-*` tokens), 256px expanded / 76px collapsed.

**Navigation structure (grouped by section header):**

| Section | Label | Icon | Route |
|---------|-------|------|-------|
| Overview | Dashboard | `pi pi-th-large` | `/` |
| | Analytics | `pi pi-chart-pie` | `/analytics` |
| | Notifications | `pi pi-bell` (+ unread badge) | `/notifications` |
| Money | Transactions | `pi pi-list` | `/transactions` |
| | Recurring | `pi pi-sync` | `/recurring` |
| | Splits | `pi pi-sitemap` | `/splits` |
| Data | Import | `pi pi-upload` | `/import` |
| | Statements | `pi pi-file` | `/statements` |
| Management | Settings | `pi pi-cog` | `/settings` |
| Admin (role=ADMIN) | Users / Server Jobs / Server Settings | `pi pi-users` / `pi pi-clock` / `pi pi-server` | `/admin/*` |

- Active item: tinted background, accent icon and a 3px accent indicator bar.
- Collapse toggle (`pi pi-angle-double-left/right`) persists state in `localStorage` key `minted-sidebar-collapsed`.
- User menu (Profile Settings / Logout) closes on outside click or Escape.

**Avatar:** Shows `<img>` when `userAvatar` getter finds base64 in localStorage, initials fallback otherwise.

**Mobile:** Sidebar hidden via `hidden md:block`; rendered inside a left `p-drawer` opened from the header hamburger. Auto-closes on navigation.

---

## Header (in `layout.html`)

- Hamburger + logo (mobile only)
- Breadcrumb (`section › page`) derived from the current URL (`pageContexts` in `layout.ts`)
- Theme toggle, privacy toggle (highlighted when active), notification bell with unread badge → toggles `p-drawer`
- Translucent background with backdrop blur; route loading bar sits on its bottom edge

**Notification drawer** (`p-drawer`, right side, `min(420px, 100vw)`):
- See [notifications.md](notifications.md) for full drawer spec

---

## Mobile Behavior

| Element | Mobile Behavior |
|---------|-----------------|
| Sidebar | `hidden md:block` — hidden on < 768px |
| Mobile nav | Hamburger (`pi pi-bars`) + PrimeNG `<p-drawer>` from left with full sidebar content |
| Dialogs | `max-width: 94vw` via global `@media (max-width: 767px)` in `styles.scss` |
| AG Grid | `height: 60vh !important; min-height: 300px` globally on mobile |
| Notification drawer | `min(420px, 100vw)` |
| Header | 12px side padding on mobile, 32px on desktop |

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
