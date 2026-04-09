# FRONTEND_SPEC.md — Minted Web (Angular Frontend)

> Detailed feature docs have been moved to [docs/features/web/](features/web/README.md).
> This file covers project setup and architecture conventions only.

---

## 1. Project Setup

### Angular CLI Generation
```bash
ng new minted-web --routing --style=scss --standalone=false --skip-tests=false
```

**`angular.json` schematics override** — ensures non-standalone components by default:
```json
{
  "@schematics/angular:component": {
    "standalone": false,
    "style": "scss"
  }
}
```

### Dependencies
```bash
npm install primeng primeicons @primeng/themes
npm install ag-grid-community ag-grid-angular
npm install -D tailwindcss postcss autoprefixer
npm install ngx-image-cropper chart.js
```

> **Font Awesome / FortAwesome is NOT installed.** All icons use PrimeNG Icons (`pi pi-*`) exclusively.

### Tailwind Configuration (`tailwind.config.js`)
```javascript
module.exports = {
  content: ["./src/**/*.{html,ts}"],
  theme: {
    extend: {
      colors: {
        'primary': '#c48821',
        'minted-green': '#0f3d32',
      }
    }
  },
  safelist: [
    { pattern: /^p-/ },   // PrimeNG classes
    { pattern: /^ag-/ },  // AG Grid classes
  ]
}
```

### `styles.scss` Structure
```scss
@tailwind base;
@tailwind components;
@tailwind utilities;
@import 'primeicons/primeicons.css';
// AG Grid v35: no manual CSS imports needed
// --minted-* design tokens (:root + .dark-mode)
// Global base styles, utility classes, PrimeNG overrides, scrollbar
```

For full token list and dark mode: [features/web/theme.md](features/web/theme.md)

### Environment Files
```typescript
// environment.ts (dev)
export const environment = {
  production: false,
  apiUrl: 'http://localhost:5500/api/v1',
  appName: 'Minted',
  version: '1.0.4'
};

// environment.prod.ts
export const environment = {
  production: true,
  apiUrl: '/api/v1',  // proxied in production
  appName: 'Minted',
  version: '1.0.4'
};
```

---

## 2. Module Architecture

```
AppModule
├── CoreModule (singleton services, guards, interceptors, models)
├── SharedModule (AvatarUploadComponent, pipes, PrimeNG re-exports)
├── LayoutModule (MainLayout, Sidebar, Header, Footer)
└── Feature Modules (all lazy-loaded)
    ├── AuthModule          /auth
    ├── DashboardModule     /dashboard
    ├── TransactionsModule  /transactions
    ├── AnalyticsModule     /analytics
    ├── RecurringModule     /recurring
    ├── ImportModule        /import
    ├── StatementsModule    /statements
    ├── SplitsModule        /splits
    ├── IntegrationsModule  /integrations
    ├── NotificationsModule /notifications
    ├── SettingsModule      /settings
    └── AdminModule         /admin  (adminGuard)
```

### Core Singleton Services (`providedIn: 'root'`)
| Service | Purpose |
|---------|---------|
| `AuthService` | JWT auth, `currentUser$`, `isAuthenticated$` |
| `ThemeService` | Dark mode, accent presets, `init()` |
| `CurrencyService` | Money formatting — inject in ALL components that display amounts |
| `NotificationService` | Notification state + 30s polling |
| `TransactionService` | Transactions CRUD |
| `AccountService` | Accounts CRUD |
| `CategoryService` | Categories CRUD |
| `BudgetService` | Budgets CRUD |
| `DashboardService` | Dashboard card state |
| `AnalyticsService` | Analytics queries |
| `RecurringTransactionService` | Recurring CRUD + summary |
| `ImportService` | CSV import workflow |
| `StatementService` | Statement parser workflow |
| `LlmConfigService` | LLM config + merchant mappings |
| `FriendService` | Friends CRUD + avatars |
| `SplitService` | Splits CRUD + balances + settle |
| `AdminService` | Admin user/job/settings management |

### Interceptors
- `JwtInterceptor` — Attaches `Authorization: Bearer <token>` to all API requests
- `ErrorInterceptor` — On 401 → redirect to login; on 403 forcePasswordChange → redirect to change-password; on 400/409/500 → PrimeNG toast

### Guards
- `AuthGuard` — requires authenticated user
- `ForcePasswordChangeGuard` — requires `forcePasswordChange=false`
- `adminGuard` — requires `user.role === 'ADMIN'`

---

## 3. Critical Rules

- **Non-standalone components ONLY** — never use `standalone: true`
- **Icons: `pi pi-*` exclusively** — no Material Icons, no Font Awesome
- **UI: PrimeNG + AG Grid + Tailwind** — no Angular Material, no Bootstrap
- **State: Services + BehaviorSubject** — no NgRx
- **Colors: `var(--minted-*)` CSS vars** — never hardcode Tailwind color classes (breaks dark mode)
- **AG Grid:** `ModuleRegistry.registerModules([AllCommunityModule])` in `main.ts` BEFORE `bootstrapModule()`
- **PrimeNG MessageService/ConfirmationService:** must be in `providers[]` of each feature module (not root)
- **PrimeNG v18 Accordion:** use `<p-accordion-panel>`, `<p-accordion-header>`, `<p-accordion-content>`
- **Currency:** Inject `CurrencyService` in every component that shows monetary values — use `format(value)`

---

## 4. Responsive Design

| Screen | Width | Behavior |
|--------|-------|---------|
| Mobile | < 768px (`md`) | Sidebar hidden, hamburger + `p-drawer` |
| Desktop | ≥ 768px (`md`) | Sidebar always visible |

Global mobile overrides in `styles.scss`:
- `p-dialog`: `max-width: 90vw` on `< 768px`
- `ag-grid-angular`: `height: 60vh; min-height: 300px` on mobile
- Notification drawer: `min(400px, 100vw)`

---

## 5. AG Grid Theme Pattern

Used consistently across all grids (transactions, import, splits, admin, etc.):

```typescript
import { themeQuartz } from 'ag-grid-community';

mintedTheme = themeQuartz.withParams({
  backgroundColor: 'var(--minted-bg-card)',
  foregroundColor: 'var(--minted-text-primary)',
  borderColor: 'var(--minted-border)',
  headerBackgroundColor: 'var(--minted-bg-card)',
  rowHoverColor: 'var(--minted-bg-hover)',
  selectedRowBackgroundColor: 'var(--minted-accent-subtle)',
  accentColor: 'var(--minted-accent)',
  fontSize: 14,
  rowHeight: 60,
  headerHeight: 48,
});
```

Apply with `[theme]="mintedTheme"` on `<ag-grid-angular>`.

---

## 6. Feature Documentation

| Feature | Doc |
|---------|-----|
| Authentication | [features/web/auth.md](features/web/auth.md) |
| App Shell (Sidebar, Header, Routing) | [features/web/layout.md](features/web/layout.md) |
| Design System (tokens, dark mode, PrimeNG, AG Grid) | [features/web/theme.md](features/web/theme.md) |
| Dashboard | [features/web/dashboard.md](features/web/dashboard.md) |
| Transactions | [features/web/transactions.md](features/web/transactions.md) |
| Analytics | [features/web/analytics.md](features/web/analytics.md) |
| Recurring Transactions | [features/web/recurring.md](features/web/recurring.md) |
| Bulk CSV Import | [features/web/import.md](features/web/import.md) |
| Credit Card Statement Parser | [features/web/statements.md](features/web/statements.md) |
| Splits & Friends | [features/web/splits.md](features/web/splits.md) |
| 3rd Party Integrations | [features/web/integrations.md](features/web/integrations.md) |
| Notifications | [features/web/notifications.md](features/web/notifications.md) |
| Settings | [features/web/settings.md](features/web/settings.md) |
| Admin Panel | [features/web/admin.md](features/web/admin.md) |
