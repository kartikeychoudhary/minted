---
title: Web Feature Documentation Index
layer: web
---

# Web Feature Documentation

Frontend (Angular) feature docs. Each file covers one module/feature area.

## Features

| File | Feature | Module | Route |
|------|---------|--------|-------|
| [auth.md](auth.md) | Authentication | `AuthModule` | `/auth/**` |
| [layout.md](layout.md) | App Shell (Sidebar, Header) | `LayoutModule` | (wrapper) |
| [theme.md](theme.md) | Design Tokens, Dark Mode, PrimeNG, AG Grid Theming | core | (global) |
| [dashboard.md](dashboard.md) | Dashboard & Chart Cards | `DashboardModule` | `/dashboard` |
| [transactions.md](transactions.md) | Transactions + Inline Split | `TransactionsModule` | `/transactions` |
| [analytics.md](analytics.md) | Analytics Overview | `AnalyticsModule` | `/analytics` |
| [recurring.md](recurring.md) | Recurring Transactions | `RecurringModule` | `/recurring` |
| [import.md](import.md) | Bulk CSV Import | `ImportModule` | `/import` |
| [statements.md](statements.md) | Credit Card Statement Parser | `StatementModule` | `/statements` |
| [splits.md](splits.md) | Splits & Friends | `SplitsModule` | `/splits` |
| [notifications.md](notifications.md) | Notifications | `NotificationsModule` | `/notifications` |
| [settings.md](settings.md) | Settings (Accounts, Categories, Budgets, Profile, LLM) | `SettingsModule` | `/settings` |
| [admin.md](admin.md) | Admin Panel | `AdminModule` | `/admin/**` |

## Module Architecture

```
AppModule
├── CoreModule          — services, guards, interceptors, models
├── SharedModule        — reusable components (AvatarUpload, DateRangePicker, pipes)
├── LayoutModule        — sidebar, header, footer
└── Feature Modules (all lazy-loaded)
    ├── AuthModule      /auth
    ├── DashboardModule /dashboard
    ├── TransactionsModule /transactions
    ├── AnalyticsModule /analytics
    ├── RecurringModule /recurring
    ├── ImportModule    /import
    ├── StatementModule /statements
    ├── SplitsModule    /splits
    ├── NotificationsModule /notifications
    ├── SettingsModule  /settings
    └── AdminModule     /admin (adminGuard)
```

## Key Rules

- All components: `standalone: false` (module-based — NEVER standalone)
- Icons: `pi pi-*` (PrimeNG Icons) ONLY — no Material Icons, no Font Awesome
- UI: PrimeNG + AG Grid + Tailwind ONLY — no Material, no Bootstrap
- State: Services + BehaviorSubject — NO NgRx
- Colors: Always use `var(--minted-*)` CSS vars — never hardcode Tailwind color classes

## Core Services (singletons, `providedIn: 'root'`)

| Service | Purpose |
|---------|---------|
| `AuthService` | JWT auth, currentUser$, isAuthenticated$ |
| `ThemeService` | Dark mode, accent presets, init() |
| `CurrencyService` | Money formatting, currency switching |
| `NotificationService` | Notification state, polling |
| `TransactionService` | Transactions CRUD |
| `AccountService` | Accounts CRUD |
| `CategoryService` | Categories CRUD |
| `BudgetService` | Budgets CRUD |
| `DashboardService` | Dashboard cards state |
| `AnalyticsService` | Analytics queries |
| `RecurringTransactionService` | Recurring CRUD + summary |
| `ImportService` | CSV import workflow |
| `StatementService` | Statement parser workflow |
| `LlmConfigService` | LLM config + merchant mappings |
| `FriendService` | Friends CRUD + avatars |
| `SplitService` | Splits CRUD + balances + settle |
| `AdminService` | Admin user/job/settings management |
