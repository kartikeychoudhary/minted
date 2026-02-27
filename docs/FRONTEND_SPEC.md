# FRONTEND_SPEC.md — Minted Web (Angular Frontend)

---

## 1. Project Setup

### 1.1 Angular CLI Generation
```bash
ng new minted-web --routing --style=scss --standalone=false --skip-tests=false
```

**CRITICAL:** The `--standalone=false` flag ensures non-standalone component generation by default. Verify `angular.json` does NOT have `"standalone": true` in schematics.

### 1.2 angular.json Schematics Override
Add this to `angular.json` under `projects > minted-web > schematics`:
```json
{
  "@schematics/angular:component": {
    "standalone": false,
    "style": "scss"
  }
}
```

### 1.3 Dependencies to Install
```bash
# PrimeNG & PrimeFlex
npm install primeng primeicons @primeng/themes

# AG Grid
npm install ag-grid-community ag-grid-angular

# Tailwind CSS
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init

# Font Awesome (FortAwesome)
npm install @fortawesome/fontawesome-free
# OR for Angular component-based usage:
npm install @fortawesome/fontawesome-svg-core @fortawesome/free-solid-svg-icons @fortawesome/free-regular-svg-icons @fortawesome/angular-fontawesome

# Chart.js (used by PrimeNG chart component)
npm install chart.js
```

### 1.4 Tailwind Configuration (`tailwind.config.js`)
```javascript
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'primary': '#c48821',
        'primary-dark': '#9d6d1a',
        'minted-green': '#0f3d32',
        'minted-green-light': '#1a5446',
        'background-light': '#f8f7f6',
        'background-dark': '#201b12',
        'minted': {
          50: '#f0fdf4',
          100: '#dcfce7',
          200: '#bbf7d0',
          300: '#86efac',
          400: '#4ade80',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
          800: '#166534',
          900: '#14532d',
        }
      },
      fontFamily: {
        'display': ['Inter', 'sans-serif']
      }
    },
  },
  plugins: [],
  // IMPORTANT: Do not let Tailwind purge PrimeNG or AG Grid classes
  safelist: [
    { pattern: /^p-/ },
    { pattern: /^ag-/ },
  ]
}
```

### 1.5 styles.scss

The global stylesheet is organized into sections:

```scss
// 1. Tailwind CSS
@tailwind base;
@tailwind components;
@tailwind utilities;

// 2. PrimeNG Icons
@import 'primeicons/primeicons.css';

// 3. Font Awesome
@import '@fortawesome/fontawesome-free/css/all.min.css';

// 4. AG Grid — v35 Theming API injects CSS automatically; no manual imports needed

// 5. MINTED DESIGN TOKENS — CSS custom properties (:root + .dark-mode)
//    See docs/UI_UX_SPEC.md Section 2 for full token list

// 6. GLOBAL BASE STYLES — font-family, background, transitions

// 7. SEMANTIC UTILITY CLASSES — .text-income, .text-expense, .text-transfer

// 8. GLOBAL PRIMENG OVERRIDES — All PrimeNG components themed via --minted-* vars
//    Covers: buttons, cards, inputs, selects, dialogs, tables, tabs, accordions,
//    progress bars, skeletons, tooltips, toasts, confirm dialogs, tags, toggle switches

// 9. GLOBAL SCROLLBAR — Custom webkit scrollbar using --minted-scrollbar-* vars
```

**Key design tokens defined in `:root`:**
- 5 background vars (`--minted-bg-page`, `--minted-bg-card`, `--minted-bg-surface`, `--minted-bg-hover`, `--minted-bg-input`)
- 3 text vars (`--minted-text-primary`, `--minted-text-secondary`, `--minted-text-muted`)
- 2 border vars (`--minted-border`, `--minted-border-light`)
- 3 accent vars (`--minted-accent`, `--minted-accent-hover`, `--minted-accent-subtle`)
- 3 semantic vars (`--minted-success`, `--minted-danger`, `--minted-info`) + subtle variants
- 1 sidebar var (`--minted-sidebar-bg`)
- 3 radius, 2 shadow, 3 scrollbar tokens

All tokens swap automatically when `.dark-mode` class is on `<html>`.

### 1.6 Environment Files

**`environment.ts` (development)**
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:5500/api/v1',
  appName: 'Minted',
  version: '0.0.1'
};
```

**`environment.prod.ts` (production)**
```typescript
export const environment = {
  production: true,
  apiUrl: '/api/v1',   // Proxied in production
  appName: 'Minted',
  version: '0.0.1'
};
```

---

## 2. Module Architecture

### 2.1 Module Map

```
AppModule
├── CoreModule (imported once in AppModule)
│   ├── AuthService
│   ├── TransactionService
│   ├── AccountService
│   ├── CategoryService
│   ├── BudgetService
│   ├── DashboardService
│   ├── AnalyticsService
│   ├── PrivacyService
│   ├── AuthGuard
│   ├── ForcePasswordChangeGuard
│   ├── JwtInterceptor
│   └── ErrorInterceptor
│
├── SharedModule (imported by feature modules)
│   ├── AvatarUploadComponent (reusable avatar picker with crop)
│   ├── ConfirmDialogComponent
│   ├── LoadingSpinnerComponent
│   ├── DateRangePickerComponent
│   ├── CurrencyPipe
│   ├── RelativeDatePipe
│   ├── ImageCropperComponent (ngx-image-cropper, standalone import)
│   └── Common PrimeNG module re-exports
│
├── LayoutModule
│   ├── MainLayoutComponent (sidebar + header + router-outlet)
│   ├── SidebarComponent
│   ├── HeaderComponent
│   └── FooterComponent
│
├── AuthModule (lazy loaded: /auth)
│   ├── LoginComponent
│   ├── ChangePasswordComponent
│   └── SignupComponent
│
├── DashboardModule (lazy loaded: /dashboard)
│   ├── DashboardComponent (container)
│   ├── DashboardCardComponent (individual card)
│   ├── CardConfigDialogComponent (edit axes/chart type)
│   └── AddCardDialogComponent
│
├── TransactionsModule (lazy loaded: /transactions)
│   ├── TransactionListComponent (AG Grid based)
│   ├── TransactionFormDialogComponent (add/edit)
│   └── TransactionFilterComponent (filter bar)
│
├── AnalyticsModule (lazy loaded: /analytics)
│   └── AnalyticsOverviewComponent (dashboard overview with summary cards, spending chart, recent transactions, recurring payments, budget status card)
│
├── ImportModule (lazy loaded: /import)
│   ├── ImportWizardComponent (3-step PrimeNG Stepper: account select → upload & preview → confirm)
│   ├── ImportJobsComponent (AG Grid import history table)
│   ├── ImportJobDetailComponent (import metadata + job step timeline with auto-refresh)
│   └── StatusCellRendererComponent (AG Grid cell renderer for color-coded status badges)
│
├── SettingsModule (lazy loaded: /settings)
│   ├── SettingsComponent (container with tabs)
│   ├── AccountTypeSettingsComponent
│   ├── AccountSettingsComponent
│   ├── CategorySettingsComponent
│   ├── BudgetSettingsComponent
│   └── ProfileSettingsComponent
│
└── AdminModule (lazy loaded: /admin, adminGuard)
    ├── UserManagementComponent (AG Grid user list, signup toggle, create/reset/delete dialogs)
    ├── JobsListComponent (AG Grid job execution list)
    ├── JobDetailComponent (execution detail with step timeline)
    └── ServerSettingsComponent (schedules, default categories, default account types)
```

### 2.2 Routing Configuration

```typescript
// app-routing.module.ts
const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () => import('./modules/auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: '',
    component: MainLayoutComponent,  // Layout wrapper with sidebar
    canActivate: [AuthGuard],
    children: [
      {
        path: 'dashboard',
        loadChildren: () => import('./modules/dashboard/dashboard.module').then(m => m.DashboardModule)
      },
      {
        path: 'transactions',
        loadChildren: () => import('./modules/transactions/transactions.module').then(m => m.TransactionsModule)
      },
      {
        path: 'settings',
        loadChildren: () => import('./modules/settings/settings.module').then(m => m.SettingsModule)
      }
    ]
  },
  { path: '**', redirectTo: '/dashboard' }
];
```

---

## 3. Core Module Details

### 3.1 Models / Interfaces (`core/models/`)

```typescript
// user.model.ts
export interface User {
  id: number;
  username: string;
  displayName: string;
  email: string;
  forcePasswordChange: boolean;
}

// auth.model.ts
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: User;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// account.model.ts
export interface AccountType {
  id: number;
  name: string;
  description: string;
  icon: string;
  isActive: boolean;
}

export interface Account {
  id: number;
  name: string;
  accountType: AccountType;
  balance: number;
  currency: string;
  color: string;
  icon: string;
  isActive: boolean;
}

// category.model.ts
export interface TransactionCategory {
  id: number;
  name: string;
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER';
  icon: string;
  color: string;
  parentId: number | null;
  isActive: boolean;
}

// transaction.model.ts
export interface Transaction {
  id: number;
  amount: number;
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER';
  description: string;
  notes: string;
  transactionDate: string;
  account: Account;
  toAccount: Account | null;
  category: TransactionCategory;
  isRecurring: boolean;
  tags: string[];
  createdAt: string;
}

export interface TransactionFilter {
  page: number;
  size: number;
  startDate?: string;
  endDate?: string;
  accountId?: number;
  categoryId?: number;
  type?: string;
  search?: string;
  period?: 'LAST_WEEK' | 'LAST_MONTH' | 'THIS_MONTH' | 'LAST_3_MONTHS' | 'CUSTOM';
  sortBy?: string;
  sortDir?: 'ASC' | 'DESC';
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// budget.model.ts
export interface Budget {
  id: number;
  name: string;
  amount: number;
  month: number;
  year: number;
  category: TransactionCategory | null;
  spent?: number;  // calculated field
}

// dashboard.model.ts
export interface DashboardCard {
  id: number;
  title: string;
  chartType: 'BAR' | 'LINE' | 'PIE' | 'DOUGHNUT' | 'AREA' | 'STACKED_BAR';
  xAxisMeasure: string;
  yAxisMeasure: string;
  filters: any;
  positionOrder: number;
  width: 'HALF' | 'FULL';
}

export interface ChartDataResponse {
  labels: string[];
  datasets: ChartDataset[];
}

export interface ChartDataset {
  label: string;
  data: number[];
  backgroundColor?: string[];
}
```

### 3.2 Auth Service (`core/services/auth.service.ts`)
- Store JWT token in `localStorage`.
- Expose `isAuthenticated$: BehaviorSubject<boolean>`.
- Expose `currentUser$: BehaviorSubject<User | null>`.
- On login success, decode JWT for user info.
- On 401 response (via interceptor), clear token and redirect to `/auth/login`.
- `signup(request: SignupRequest)` — POST `/auth/signup`, stores tokens and user (same as login flow).
- `isSignupEnabled()` — GET `/auth/signup-enabled`, returns `Observable<boolean>`.

### 3.3 JWT Interceptor (`core/interceptors/jwt.interceptor.ts`)
- Attach `Authorization: Bearer <token>` to all requests to `environment.apiUrl`.
- Skip for auth endpoints (login).

### 3.4 Error Interceptor (`core/interceptors/error.interceptor.ts`)
- Catch HTTP errors globally.
- On 401: clear auth state, redirect to login.
- On 403 with `forcePasswordChange`: redirect to change-password page.
- On 400/409/500: show PrimeNG toast notification with error message.

---

## 4. Page Specifications

### 4.1 Login Page (`/auth/login`)
- PrimeNG `p-card` centered on screen.
- Username and password inputs (`p-inputtext` with `pInputText` directive).
- "Login" button (`p-button`).
- Form validation: both fields required.
- On success: if `forcePasswordChange`, navigate to `/auth/change-password`; else navigate to `/dashboard`.
- Clean, minimal design. Minted branding/logo at top.
- "Create Account" link conditionally shown when signup is enabled (calls `authService.isSignupEnabled()`), routes to `/signup`.
- Social login buttons (Google/Facebook) removed.

### 4.1b Signup Page (`/signup`)
- Matches login page design exactly (same card layout, decorative blurs, logo, color scheme).
- Fields: Display Name, Email, Username, Password (p-password), Confirm Password (p-password).
- Cross-field password match validator.
- On init: calls `authService.isSignupEnabled()` to check if registration is allowed.
- When signup disabled: warning banner (amber/orange) shown, submit button disabled.
- On successful signup: auto-login (tokens returned from API), navigate to `/dashboard`.
- Footer: "Already have an account? Sign In" link to `/login`.

### 4.2 Change Password Page (`/auth/change-password`)
- Shown after first login or via settings.
- Fields: Current Password, New Password, Confirm Password.
- Validation: min 8 chars, at least one uppercase, one number, passwords must match.
- PrimeNG `p-password` component with strength indicator.
- On success: navigate to `/dashboard`.

### 4.3 Dashboard (`/dashboard`)
- **Summary Cards Row:** 4 summary cards at the top showing:
  - Total Income (this month)
  - Total Expenses (this month)
  - Net Balance
  - Budget Utilization %
  Each card uses PrimeNG `p-card` with colored left border, icon, amount.

- **Chart Cards Grid:** Below summary, a responsive grid of chart cards.
  - Each card is a PrimeNG `p-card` containing a Chart.js chart (via PrimeNG `p-chart`).
  - Cards are laid out in a 2-column grid (HALF width) or full-width (FULL width).
  - Each card has a header with title, and a small settings icon (FortAwesome gear icon).
  - Clicking settings opens `CardConfigDialogComponent` — a PrimeNG `p-dialog` where user can:
    - Change chart type (dropdown)
    - Change X-axis measure (dropdown)
    - Change Y-axis measure (dropdown)
    - Change date range
  - "Add Card" button at the bottom to add new dashboard cards.
  - Cards are draggable for reordering (use PrimeNG `pDraggable`/`pDroppable` or simple position buttons).

- **Date Range Selector** at the top right corner to control the global dashboard date range.

### 4.4 Transactions Page (`/transactions`)
- **Filter Bar** at the top:
  - Date range picker (PrimeNG `p-calendar` with range selection)
  - Quick filters: "Last Week", "Last Month", "This Month", "Last 3 Months", "Custom"
  - Account dropdown (PrimeNG `p-dropdown`, multi-select)
  - Category dropdown (PrimeNG `p-dropdown`, multi-select)
  - Type toggle: All / Income / Expense / Transfer (PrimeNG `p-selectButton`)
  - Search input (PrimeNG `p-inputtext` with debounce)
  - "Add Transaction" button (PrimeNG `p-button`)

- **Transaction Grid** (AG Grid):
  - Columns: Date, Description, Category (with icon+color), Account, Type, Amount (colored: green for income, red for expense), Actions
  - Server-side pagination (AG Grid infinite scroll or pagination)
  - Sortable columns
  - Row click opens edit dialog
  - Actions column: Edit (pencil icon), Delete (trash icon with confirm)
  - Amount column right-aligned with currency formatting
  - Mobile: AG Grid auto-sizes columns, or switch to a card-based list view on small screens

- **Transaction Form Dialog** (`p-dialog`):
  - Fields: Type (radio buttons), Amount, Description, Date (calendar), Account (dropdown), Category (dropdown filtered by type), To Account (shown only for TRANSFER), Notes (textarea), Tags (chips input)
  - Validation: Amount > 0, Date required, Account required, Category required
  - Save and Cancel buttons

### 4.5 Settings Page (`/settings`)
- PrimeNG `p-tabView` with tabs:

#### Tab: Accounts
- Table (PrimeNG `p-table`) listing all accounts with columns: Name, Type, Balance, Currency, Status
- Add/Edit via inline editing or dialog
- Delete performs soft delete (`isActive = false`) — account disappears from UI
- Creating an account with the same name as a previously deleted one restores the soft-deleted record
- `refreshData()` method reloads both accounts and account types (called on tab activation)

#### Tab: Account Types
- Table listing account types: Name, Description, Icon
- Add/Edit/Soft Delete (sets `isActive = false`)
- Soft-deleted types shown at bottom with strikethrough text, reduced opacity (55%), red "Deleted" badge, and "Undo" button to restore
- Active types sorted first, inactive last
- Default system types cannot be deleted (buttons disabled)
- Restore calls `PATCH /account-types/{id}/toggle` via `AccountTypeService.toggleActive()`
- Tab switch from Account Types to Accounts triggers `refreshData()` to prevent stale dropdown data

#### Tab: Categories
- Tree table or grouped table showing categories (with sub-categories)
- Filter by type (Income/Expense/Transfer)
- Add/Edit/Deactivate
- Icon picker and color picker for each category

#### Tab: Budgets
- Month/Year selector at the top
- Table showing: Category, Budgeted Amount, Spent Amount, Remaining, Progress Bar
- Add/Edit budget amounts
- Visual progress indicator (PrimeNG `p-progressBar`)

#### Tab: Profile
- Display name, email, username (read-only)
- Change password button (navigates to change-password)
- Theme preference (light/dark toggle)

---

## 5. Theming System

### 5.1 PrimeNG Theme Setup
Using PrimeNG Aura preset with dark mode selector:

```typescript
// app-module.ts
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';

providePrimeNG({
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '.dark-mode'
    }
  }
})
```

### 5.2 ThemeService (`core/services/theme.service.ts`)
Singleton service (`providedIn: 'root'`) managing:

- **Dark mode:** Toggles `.dark-mode` class on `<html>`, persisted in `localStorage` key `minted-dark-mode`
- **Accent colors:** Sets `--minted-accent`, `--minted-accent-hover`, `--minted-accent-subtle` CSS vars + updates PrimeNG Aura primary palette via `updatePreset()` from `@primeng/themes`
- **6 accent presets:** Amber (`#c48821`), Emerald (`#10b981`), Blue (`#3b82f6`), Violet (`#8b5cf6`), Rose (`#f43f5e`), Teal (`#14b8a6`)
- Initialized in `AppComponent.ngOnInit()` via `themeService.init()`

### 5.3 AG Grid v35 Theming API
AG Grid no longer uses CSS class themes. Instead, use `themeQuartz.withParams()` in TypeScript:

```typescript
import { themeQuartz } from 'ag-grid-community';

mintedTheme = themeQuartz.withParams({
  backgroundColor: 'var(--minted-bg-card)',
  foregroundColor: 'var(--minted-text-primary)',
  borderColor: 'var(--minted-border)',
  headerBackgroundColor: 'var(--minted-bg-card)',
  headerTextColor: 'var(--minted-text-muted)',
  rowHoverColor: 'var(--minted-bg-hover)',
  selectedRowBackgroundColor: 'var(--minted-accent-subtle)',
  accentColor: 'var(--minted-accent)',
  fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
  fontSize: 14,
  rowHeight: 60,
  headerHeight: 48,
  wrapperBorderRadius: 8,
  // ... additional params reference --minted-* CSS vars
});
```

Applied via `[theme]="mintedTheme"` on `<ag-grid-angular>`. Additional CSS overrides in component SCSS via `::ng-deep`.

**Important:** `ModuleRegistry.registerModules([AllCommunityModule])` must be called in `main.ts` BEFORE `bootstrapModule()`.

### 5.4 Global PrimeNG Overrides
All PrimeNG components are overridden in `styles.scss` to use `--minted-*` CSS variables. This ensures:
- Dark mode works automatically when `.dark-mode` class is toggled
- Accent color changes propagate to all PrimeNG components
- Consistent look across buttons, cards, inputs, selects, dialogs, tables, tabs, accordions, tags, progress bars, tooltips, toasts, confirm dialogs, toggle switches, and skeletons

### 5.5 CurrencyService (`core/services/currency.service.ts`)
Singleton service managing global currency display:
- `format(value)` — formats numbers with current currency
- `currentCurrency` / `currentLocale` — current settings
- `setCurrency(code)` — switch currency (persisted in localStorage)
- Injected in ALL components that display monetary values

---

## 6. Responsive Design Rules

### Breakpoints (matching Tailwind defaults)
| Screen | Width | Layout |
|--------|-------|--------|
| Mobile | < 768px (`md`) | Sidebar hidden, hamburger opens PrimeNG Drawer, single column grids |
| Desktop | >= 768px (`md`) | Sidebar always visible, normal multi-column layout |

### Implemented Responsive Behaviors
1. **Sidebar:** Hidden on mobile via `hidden md:block`. PrimeNG `<p-drawer>` opens from left with full sidebar content (`mobileMode` input). Hamburger button (`material-icons: menu`) visible only on mobile (`md:hidden`). Auto-closes on navigation.
2. **Dialogs:** Global `@media (max-width: 767px)` in `styles.scss` caps all `.p-dialog` at `90vw`.
3. **AG Grid:** Global mobile override sets `height: 60vh !important; min-height: 300px` on all `ag-grid-angular` elements.
4. **Notification drawer:** Width `min(400px, 100vw)` — fills screen on small devices.
5. **Header:** Padding `px-4 md:px-8`. "Minted" text removed (brand in sidebar only).
6. **Auth pages:** Form padding `px-4 sm:px-8`, brand text `text-xl sm:text-2xl`.
7. **Transactions page:** Container padding `p-4 sm:p-8`. Filters already use `flex-col sm:flex-row`.
8. **Dashboard/Recurring:** Already had responsive breakpoints at 768px and 1024px (KPI grid, charts grid, form grid, summary grid).
9. **Font sizes:** Use Tailwind responsive prefixes (`text-xl sm:text-2xl`).

---

## 7. State Management

**Do NOT use NgRx.** Keep state management simple:

- Use Angular Services with `BehaviorSubject` for shared state.
- `AuthService` manages auth state globally.
- Each feature service (e.g., `TransactionService`) handles its own data fetching.
- Use `shareReplay(1)` on observables that are subscribed to by multiple components.
- Dashboard state (cards, their data) lives in `DashboardService`.

---

## 8. Error & Loading States

Every page must handle these 3 states:
1. **Loading:** Show PrimeNG `p-progressSpinner` or skeleton loader (`p-skeleton`).
2. **Empty:** Show a friendly empty state with icon + message + optional CTA button.
3. **Error:** Show PrimeNG toast (`MessageService`) for API errors. For full-page errors, show an inline error message with retry button.

---

## 9. Recurring Transactions Module

### 9.1 Route
- Path: `/recurring` (lazy-loaded `RecurringModule`)
- Sidebar: `{ label: 'Recurring', icon: 'sync_alt', route: '/recurring' }`

### 9.2 RecurringList Component (`components/recurring-list/`)

**Layout (3 sections):**

1. **Form Card** — "New Recurring Transaction"
   - Row 1: Transaction Name (span-2), Amount (InputNumber, INR), Type Toggle (Expense/Income)
   - Row 2: Category (p-select), Account (p-select), Frequency (fixed "Monthly"), Day of Month (InputNumber 1-31)
   - Row 3: Start Date (p-datePicker), End Date (optional p-datePicker), Submit button
   - Type toggle: custom CSS buttons (active-expense red / active-income green)
   - Supports edit mode with Cancel + Update buttons

2. **Active Schedules Table** — PrimeNG `p-table`
   - Columns: Transaction Name (icon + name + account), Category (badge), Amount (colored), Next Date + countdown, Actions
   - Actions: Edit, Pause/Resume (toggle), Delete (confirm dialog)
   - Paused rows: 0.6 opacity, PAUSED badge, play_circle icon
   - Search filter input in header
   - Paginator: 5/10/20 rows per page

3. **Summary Cards Grid** (3-col)
   - Est. Monthly Expenses (red icon, white bg)
   - Est. Monthly Income (green icon, white bg)
   - Scheduled Net Flux (white icon, dark green bg)

### 9.3 Services
- `RecurringTransactionService` — CRUD, toggleStatus, getSummary, search
- Uses `AccountService.getAll()` and `CategoryService.getAll()` for dropdown data

### 9.4 Models
- `RecurringTransaction`, `RecurringTransactionRequest`, `RecurringSummary`

---

## 10. Import Module

### 10.1 Route & Navigation
- Path: `/import` (lazy-loaded `ImportModule`)
- Sidebar: `{ label: 'Import', icon: 'upload_file', route: '/import' }` (after Recurring, before Analytics)
- Child routes: `''` → ImportWizard, `'jobs'` → ImportJobs, `'jobs/:id'` → ImportJobDetail

### 10.2 Import Service (`core/services/import.service.ts`)
HTTP client (`providedIn: 'root'`) for `/api/v1/imports`:
- `downloadTemplate()` → GET `/imports/template` (responseType: blob)
- `uploadCsv(file, accountId)` → POST `/imports/upload` (FormData)
- `confirmImport(request)` → POST `/imports/confirm`
- `getUserImports()` → GET `/imports`
- `getImportById(id)` → GET `/imports/{id}`
- `getImportJobDetails(id)` → GET `/imports/{id}/job`

### 10.3 Models (`core/models/import.model.ts`)
- `BulkImportResponse` — Import metadata (id, accountId, accountName, status, row counts, jobExecutionId, timestamps)
- `CsvUploadResponse` — Upload result with importId, counts, and `CsvRowPreview[]`
- `CsvRowPreview` — Per-row validation (rowNumber, date, amount, type, description, categoryName, status, errorMessage, matchedCategoryId, isDuplicate)
- `BulkImportConfirmRequest` — `{ importId: number, skipDuplicates: boolean }`
- `ImportStatus` — Type union: `'PENDING' | 'VALIDATING' | 'VALIDATED' | 'IMPORTING' | 'COMPLETED' | 'FAILED'`

### 10.4 Components

#### ImportWizard (`/import`)
- 3-step PrimeNG Stepper (`<p-stepper>`, `<p-step-list>`, `<p-step-panels>` with `#content` template and `activateCallback`)
- **Step 1:** Account selection dropdown (p-select) + Download template button + CSV format instructions card
- **Step 2:** File input (`<input type="file" accept=".csv">`) + Upload button + Summary cards (Total/Valid/Errors/Duplicates) + AG Grid preview with StatusCellRenderer
- **Step 3:** Import summary + Skip duplicates toggle (p-toggleswitch) + Start Import button + Post-import success with navigation to job detail
- AG Grid theme: exact copy of minted theme from transactions-list (`themeQuartz.withParams()` with CSS var references)
- Preview columns: #, Status (badge renderer), Date, Amount (CurrencyService.format), Type, Description, Category, Error
- Import type cards: CSV Import (active) + Credit Card Statement (active, navigates to `/statements`)

#### ImportJobs (`/import/jobs`)
- AG Grid table showing import history
- Columns: ID, File Name, Account, Status (StatusCellRenderer), Rows (imported/total), Created, Actions (View button)
- Same minted theme, pagination, overlayNoRowsTemplate

#### ImportJobDetail (`/import/jobs/:id`)
- Import metadata cards (Account, File Size, Created, Import Type)
- Row stat cards (Total, Valid, Duplicates, Errors, Imported) with semantic colors
- Auto-refresh polling: `interval(5000)` with `takeWhile()` while status is IMPORTING
- Job execution step timeline (same pattern as admin JobDetail)
- Error display, polling indicator

#### StatusCellRendererComponent
- AG Grid cell renderer implementing `ICellRendererAngularComp`
- Color-coded badges: VALID (green), ERROR (red), DUPLICATE (amber), COMPLETED (green), FAILED (red), IMPORTING/RUNNING (blue), VALIDATED/PENDING (gray)

---

## 11. Admin Module (Role-Protected)

### 11.1 Route & Security
- Path: `/admin` (lazy-loaded `AdminModule`)
- Protected by `adminGuard` — checks `user.role === 'ADMIN'`, redirects non-admins to `/dashboard`
- Default redirect: `/admin` → `/admin/users`
- Sidebar: Conditionally shows admin items only for ADMIN role users
  - "Users" → `/admin/users` (icon: `group`, section: `Admin`)
  - "Server Jobs" → `/admin/jobs` (icon: `schedule`)
  - "Server Settings" → `/admin/settings` (icon: `dns`)

### 11.2 Admin Service (`core/services/admin.service.ts`)
HTTP client for `/api/v1/admin` endpoints:

**User Management:**
- `getUsers()` → GET `/admin/users` → `Observable<AdminUserResponse[]>`
- `getUserById(id)` → GET `/admin/users/{id}` → `Observable<AdminUserResponse>`
- `createUser(request)` → POST `/admin/users` → `Observable<AdminUserResponse>`
- `toggleUserActive(id)` → PUT `/admin/users/{id}/toggle` → `Observable<AdminUserResponse>`
- `deleteUser(id)` → DELETE `/admin/users/{id}` → `Observable<void>`
- `resetPassword(id, request)` → PUT `/admin/users/{id}/reset-password` → `Observable<void>`

**System Settings:**
- `getSetting(key)` → GET `/admin/settings/{key}` → `Observable<SystemSettingResponse>`
- `updateSetting(key, value)` → PUT `/admin/settings/{key}` → `Observable<SystemSettingResponse>`

**Jobs:**
- `getJobs(page, size)` → GET `/admin/jobs`
- `getJobExecution(id)` → GET `/admin/jobs/{id}`
- `triggerJob(jobName)` → POST `/admin/jobs/{jobName}/trigger`

**Schedules:**
- `getSchedules()` → GET `/admin/schedules`
- `updateSchedule(id, cronExpression, enabled)` → PUT `/admin/schedules/{id}`

**Default Lists:**
- `getDefaultCategories()` / `createDefaultCategory()` / `deleteDefaultCategory()`
- `getDefaultAccountTypes()` / `createDefaultAccountType()` / `deleteDefaultAccountType()`

### 11.3 Models (`core/models/admin.model.ts` + `user.model.ts`)

```typescript
// admin.model.ts
interface SystemSettingResponse {
  id: number;
  settingKey: string;
  settingValue: string;
  description: string;
}
```

```typescript
// user.model.ts — additional interfaces for user management
interface AdminUserResponse {
  id: number;
  username: string;
  displayName: string;
  email: string | null;
  isActive: boolean;
  forcePasswordChange: boolean;
  currency: string;
  role: string;
  createdAt: string;
  updatedAt: string;
}

interface CreateUserRequest {
  username: string;
  password: string;
  displayName?: string;
  email?: string;
  role?: string;
}

interface ResetPasswordRequest {
  newPassword: string;
}

interface SignupRequest {
  username: string;
  password: string;
  confirmPassword: string;
  displayName?: string;
  email?: string;
}
```

```typescript
// admin.model.ts — existing job interfaces
interface JobExecution {
  id: number;
  jobName: string;
  status: string;           // RUNNING | COMPLETED | FAILED
  triggerType: string;       // SCHEDULED | MANUAL
  startTime: string;
  endTime?: string;
  errorMessage?: string;
  totalSteps: number;
  completedSteps: number;
  steps?: JobStepExecution[];
}

interface JobStepExecution {
  id: number;
  stepName: string;
  stepOrder: number;
  status: string;            // PENDING | RUNNING | COMPLETED | FAILED | SKIPPED
  contextJson?: string;
  errorMessage?: string;
  startTime: string;
  endTime?: string;
}

interface JobScheduleConfig {
  id: number;
  jobName: string;
  cronExpression: string;
  enabled: boolean;
  lastRunAt?: string;
  description?: string;
}

interface DefaultCategory {
  id?: number;
  name: string;
  icon?: string;
  type: 'INCOME' | 'EXPENSE';
}

interface DefaultAccountType {
  id?: number;
  name: string;
}
```

### 11.4 Components

#### UserManagement (`/admin/users`)
- **Signup Settings Card** — PrimeNG `p-toggleswitch` for enabling/disabling public registration, loads from `getSetting('SIGNUP_ENABLED')`
- **Users Card** — AG Grid with minted theme (rowHeight: 56)
  - Columns: Username (bold), Display Name, Email, Role (badge: ADMIN=accent, USER=default), Status (badge: Active=green, Disabled=red), Password (MUST CHANGE badge if forcePasswordChange), Created (date formatted), Actions
  - Actions column: Toggle (Enable/Disable button), Reset Password (round key icon), Delete (round trash icon) — uses `data-action` attributes with `onCellClicked` handler
  - "New User" button with `pi pi-plus` icon
- **Create User Dialog** — Fields: Username, Password (p-password), Display Name, Email, Role (p-select: USER/ADMIN). Info banner about forced password change.
- **Reset Password Dialog** — Fields: New Password (p-password). Info banner about forced change on next login.
- **Delete User** — PrimeNG ConfirmDialog with cascading data deletion warning
- All actions trigger toast notifications via MessageService

#### JobsList (`/admin/jobs`)
- AG Grid with Minted theme listing all job executions
- Columns: ID, Job Name, Status (badge), Trigger Type, Started, Ended, Steps (progress), Actions
- Status badges: COMPLETED (green), FAILED (red), RUNNING (blue), PENDING (gray)
- Pagination: 15/30/50 rows per page
- "Run Recurring Txn Job" button for manual trigger
- "Refresh" button to reload data
- Row click navigates to detail view

#### JobDetail (`/admin/jobs/:id`)
- Full execution detail view with back navigation
- Header: Job ID, status badge, job name, trigger type
- Metadata cards: Started, Ended, Duration, Steps Passed
- Error message alert (if failed)
- **Execution Steps Timeline:**
  - Colored activity bar per step status
  - Step order badge, name, duration, timestamp
  - Result context JSON displayed in code block
  - Per-step error messages

#### ServerSettings (`/admin/settings`)
- Three-section layout:

**A. Automated Jobs (Schedules)**
- Cards per schedule config showing: job name, description, cron expression, last run time
- Enable/disable toggle button per schedule
- Active/Disabled status badge

**B. Default Categories**
- AG Grid: Name, Type (INCOME/EXPENSE), Icon columns
- Add modal: name, type dropdown, icon class input
- Delete with ConfirmationService

**C. Default Account Types**
- AG Grid: Name column
- Add modal: name input
- Delete with ConfirmationService

### 11.5 Sidebar Integration
The sidebar component (`layout/components/sidebar/sidebar.ts`) uses `buildNavigation(role)` to conditionally add admin items:
- Base navigation: Dashboard, Transactions, Recurring, Import, Analytics, Notifications, Settings
- Admin items (role=ADMIN only): Users, Server Jobs, Server Settings
- Items appear under a separate "Admin" section header
- "Users" is the first admin item (most common admin action)

## 12. Notifications Module

### 12.1 Model (`core/models/notification.model.ts`)

- `NotificationType` — `'INFO' | 'SUCCESS' | 'WARNING' | 'ERROR' | 'SYSTEM'`
- `NotificationResponse` — `id`, `type`, `title`, `message`, `isRead`, `createdAt`
- `NotificationPage` — Spring Page wrapper: `content[]`, `totalElements`, `totalPages`, `size`, `number`, `first`, `last`

### 12.2 NotificationService (`core/services/notification.service.ts`)

Singleton (`providedIn: 'root'`) managing all notification state reactively:

| Observable / Property | Description |
|----------------------|-------------|
| `unreadCount$` | `BehaviorSubject<number>` — badge count |
| `notifications$` | `BehaviorSubject<NotificationResponse[]>` — accumulated list |
| `hasMore` | `boolean` — whether more pages exist |

| Method | Description |
|--------|-------------|
| `startPolling()` | 30-second `interval()` for unread count |
| `stopPolling()` | Cancels polling subscription |
| `fetchUnreadCount()` | Single GET for badge |
| `loadNotifications(page)` | GET paginated, replaces list |
| `loadMore()` | GET next page, appends to list |
| `markAsRead(id)` | PUT + optimistic local update |
| `markAllAsRead()` | PUT + optimistic local update |
| `dismiss(id)` | DELETE + remove from local list |
| `dismissAllRead()` | DELETE + remove all read from list |

**Polling lifecycle:** Managed in `Layout` component via `authService.currentUser$` subscription — starts on login, stops on logout. Avoids circular dependency (AuthService ↔ NotificationService ↔ HttpClient ↔ JwtInterceptor ↔ AuthService).

### 12.3 Route Loading Animation

The `Layout` component tracks `isRouteLoading` state by subscribing to Angular Router events:
- `NavigationStart` → `isRouteLoading = true`
- `NavigationEnd` / `NavigationCancel` / `NavigationError` → `isRouteLoading = false`

A 3px animated bar (`route-loading-bar`) renders at the top of the content area when loading. Uses `var(--minted-accent)` color with a CSS sliding animation (`route-loading-slide` keyframes in `layout.scss`).

### 12.4 Layout Integration (Header Bell + Drawer)

**Header bell icon** (`layout/layout.html`):
- Material icon `notifications` with unread badge (capped at 99+)
- Click toggles PrimeNG `<p-drawer>` (right side, 400px width)

**Notification drawer** contents:
- Custom header: "Notifications" title + "Mark all read" + "Clear all read" buttons
- Empty state: bell icon + "No notifications yet"
- List: notification cards with type icon (colored by type), title, message, relative time, unread accent bar, hover dismiss button
- Click marks as read + navigates to `actionUrl` if present
- "See all notifications" link → `/notifications`
- "Load more" button when `hasMore`

### 12.4 Full Notifications Page (`/notifications`)

**Module:** `modules/notifications/notifications-module.ts` (lazy-loaded)

**NotificationsList component** — Full-page notification center:
- Title: "Notifications" with subtitle
- Global actions: "Mark all as read" (shown when unread exist), "Clear all" (with confirmation dialog)
- Skeleton loading state (3 skeleton cards)
- Empty state: "All caught up!" with back-to-dashboard button
- Date-grouped sections: Today, Yesterday, Earlier this Week, Older
- Notification cards: type icon with colored background, title (bold if unread), message, relative time, hover dismiss button, unread accent bar
- "Load more" pagination button

### 12.5 Design Tokens

Added `--minted-warning` and `--minted-warning-subtle` CSS custom properties (both light and dark mode). PrimeNG Drawer overrides added to `styles.scss` using `--minted-*` variables.

---

## 13. Credit Card Statement Parser Module

### 13.1 Route & Navigation
- Path: `/statements` (lazy-loaded `StatementModule`)
- Sidebar: `{ label: 'Statements', icon: 'description', route: '/statements' }` (after Import)
- Child routes: `''` → StatementList, `'new'` → UploadStep, `':id'` → StatementDetail

### 13.2 Models (`core/models/statement.model.ts` + `core/models/llm-config.model.ts`)

**Statement Models:**
- `StatementStatus` — Type union: `'UPLOADED' | 'TEXT_EXTRACTED' | 'LLM_PARSED' | 'CONFIRMING' | 'COMPLETED' | 'FAILED'`
- `CreditCardStatement` — id, accountId, accountName, fileName, fileSize, status, currentStep, extractedText, parsedCount, duplicateCount, importedCount, errorMessage, jobExecutionId, createdAt, updatedAt
- `ParsedTransactionRow` — tempId, amount, type, description, transactionDate, categoryName, matchedCategoryId, notes, tags, isDuplicate, duplicateReason, mappedByRule
- `ConfirmStatementRequest` — statementId, skipDuplicates

**LLM Config Models:**
- `LlmModel` — id, name, provider, modelKey, description, isActive, isDefault
- `LlmConfig` — id, provider, hasApiKey, selectedModel, merchantMappings[]
- `LlmConfigRequest` — apiKey?, modelId?
- `MerchantMapping` — id, snippets, snippetList[], categoryId, categoryName, categoryIcon, categoryColor
- `MerchantMappingRequest` — snippets, categoryId
- `LlmModelRequest` — name, provider?, modelKey, description?, isActive?, isDefault?

### 13.3 Services

#### StatementService (`core/services/statement.service.ts`)
- `upload(file, accountId, pdfPassword?)` → POST multipart `/statements/upload`
- `triggerParse(statementId)` → POST `/statements/{id}/parse`
- `getParsedRows(statementId)` → GET `/statements/{id}/parsed-rows`
- `confirmImport(request)` → POST `/statements/confirm`
- `getStatements()` → GET `/statements`
- `getStatement(id)` → GET `/statements/{id}`

#### LlmConfigService (`core/services/llm-config.service.ts`)
- User config: `getConfig()`, `saveConfig(request)`, `getAvailableModels()`
- Merchant mappings: `getMappings()`, `createMapping()`, `updateMapping()`, `deleteMapping()`
- Admin models: `getAllModels()`, `createModel()`, `updateModel()`, `deleteModel()`

### 13.4 Statement Module Components

#### StatementList (`/statements`)
- Lists all user statements with status badges (PrimeNG p-tag)
- Cards showing filename, account, file size, date, step, status
- "Parse New Statement" button → navigates to `/statements/new`
- Empty state with CTA

#### UploadStep (`/statements/new`)
- Native file input for PDF selection (drag-to-select styling)
- Account dropdown (p-select, populated from AccountService)
- Optional password field (toggle via checkbox)
- Validation: PDF only, max 20MB
- On submit: calls `statementService.upload()`, navigates to `/statements/{id}`

#### StatementDetail (`/statements/:id`)
- Stepper host showing 4-step progress indicator (custom step indicator, not PrimeNG stepper)
- Dynamically renders child components based on `statement.currentStep`
- Polls statement status every 3 seconds while waiting for async LLM parsing
- Error banner for FAILED status

#### TextReviewStep (Step 2)
- Read-only textarea showing extracted text with character count
- "Send to AI for Parsing" button → calls `triggerParse()`
- Spinner with message while parsing
- Triggers parent polling on parse start

#### ParsePreviewStep (Step 3)
- AG Grid with minted theme showing parsed transactions
- Columns: Duplicate indicator, Date, Description (editable), Amount (colored), Type (editable select), Category (editable, shows rule icon for mappedByRule), Notes (editable)
- Duplicate rows highlighted with warning background + left border
- "Skip duplicate transactions" checkbox (default checked)
- "Import Transactions" button with confirmation dialog
- Summary showing total/duplicate/import counts

#### ConfirmStep (Step 4)
- Success summary with check icon
- Stats cards: Parsed, Duplicates, Imported
- Navigation buttons to Transactions page or Statements list

### 13.5 Settings Integration

#### LLM Configuration Tab (Settings → LLM Configuration)
New tab (value="5") added to existing Settings page.

**LlmConfigComponent** — Two sections:
1. **API & Model Configuration**
   - Model dropdown (p-select, populated from active models)
   - Password input for API key (placeholder changes based on hasApiKey)
   - Info card with Gemini API key instructions
   - Save button

2. **Merchant Mappings** (sub-component: `MerchantMappingsComponent`)
   - AG Grid with minted theme (inline editing, domLayout autoHeight)
   - Columns: Keywords (editable text), Category (editable select with color dot), Delete button
   - Row-by-row save: onCellValueChanged calls create/update API
   - "Add Mapping" button adds empty row in edit mode
   - Delete with confirmation dialog
   - Empty state message

### 13.6 Admin Integration

#### Server Settings Page — New Sections

**Feature Toggles:**
- "Credit Card Statement Parser" — p-toggleSwitch for `CREDIT_CARD_PARSER_ENABLED`
- "Share Admin LLM Key" — p-toggleSwitch for `ADMIN_LLM_KEY_SHARED`

**LLM Models Management:**
- AG Grid listing all models (including disabled)
- Columns: Name, Model Key, Provider, Status (Active/Disabled), Default (star icon), Actions
- Actions: Edit (dialog), Toggle active/disabled, Delete (confirmation)
- "Add Model" button opens dialog with: Name, Provider (locked GEMINI), Model Key, Description, Set as Default checkbox

### 13.7 Modified Files
- `app-routing-module.ts` — Added lazy route for `statements` → `StatementModule`
- `sidebar.ts` — Added "Statements" nav item (icon: `description`)
- `settings-module.ts` — Added `LlmConfigComponent`, `MerchantMappingsComponent`, `AgGridModule`
- `settings.html` — Added LLM Configuration tab (value="5")
- `admin server-settings.ts/html` — Added feature toggles, LLM models grid, model dialog
- `shared.module.ts` — Imports `ImageCropperComponent` (ngx-image-cropper), declares+exports `AvatarUploadComponent`

---

## 14. Splits Module

Bill splitting with friends. Users can add friends, split transactions (standalone or from existing transactions), track who owes whom, settle debts, and export per-friend CSV reports.

### 14.1 Models

**`core/models/friend.model.ts`**
- `FriendRequest` — name (required), email, phone, avatarColor
- `FriendResponse` — id, name, email, phone, avatarColor, avatarBase64 (data URI or null), isActive, createdAt, updatedAt

**`core/models/split.model.ts`**
- `SplitType` — `'EQUAL' | 'UNEQUAL' | 'SHARE'`
- `SplitShareRequest` — friendId (null=Me), shareAmount, sharePercentage, isPayer
- `SplitShareResponse` — includes splitDescription, splitCategoryName, splitTransactionDate
- `SplitTransactionRequest` — description, categoryName, totalAmount, splitType, transactionDate, shares[]
- `SplitTransactionResponse` — includes yourShare (computed), shares list
- `SplitBalanceSummaryResponse` — youAreOwed, youOwe
- `FriendBalanceResponse` — friendId, friendName, avatarColor, balance (+/-)
- `SettleRequest` — friendId

### 14.2 Services

**`core/services/friend.service.ts`** (`providedIn: 'root'`)
- Standard CRUD for `/api/v1/friends`
- Methods: getAll, getById, create, update, delete

**`core/services/split.service.ts`** (`providedIn: 'root'`)
- CRUD for `/api/v1/splits`
- Methods: getAll, getById, create, update, delete
- Analytics: getBalanceSummary, getFriendBalances
- Settlement: settle
- Export: getSharesByFriend, exportFriendShares (client-side CSV generation)

### 14.3 Module Structure

```
modules/splits/
├── splits-module.ts                    # NgModule (CommonModule, SharedModule, AgGridModule)
├── splits-routing-module.ts            # { path: '', component: SplitsPage }
└── components/
    ├── splits-page/
    │   ├── splits-page.ts              # Main component — all UI logic
    │   ├── splits-page.html            # Template
    │   └── splits-page.scss            # AG Grid theme overrides
    └── cell-renderers/
        ├── split-friends-cell-renderer.component.ts   # Avatar circles for "Split With" column
        └── split-actions-cell-renderer.component.ts   # Edit/Delete buttons
```

**Module providers:** `[MessageService, ConfirmationService]` — per MISTAKES.md rule.

### 14.4 SplitsPage Component

**Template layout** (matches Stitch reference `split-transactions.html`):

1. **Header** — "Splits" title + "Add Friend" / "Add Split" buttons
2. **Friends card** — horizontal scroll of friend avatar circles (initials in colored circles) + dashed "Add" button
3. **Summary cards** (2-col) — "You are owed" (green) / "You owe" (orange) with formatted amounts
4. **Pending settlements** — Cards per friend showing avatar, name, balance (+/-), "Settle" + "Export CSV" buttons
5. **AG Grid** — split transactions table: Date, Description, Category, Split With (avatar circles), Total, Your Share, Actions

**Dialogs (all `<p-dialog>`):**
- **Add/Edit Friend** — name, email, phone, avatar color picker (8 preset colors)
- **Split Transaction** — description, category, total amount, date, split type radios (Equal/Unequal/By Share), friend multi-select with share amounts, summary box
- **Settlement Review** — friend avatar, total amount, itemized unsettled shares list, confirm button

**AG Grid** uses the same `mintedTheme` pattern (`themeQuartz.withParams()` with `--minted-*` CSS vars) as transactions-list.

**Dark mode:** All colors use inline `style` attributes with CSS variables (`var(--minted-bg-card)`, `var(--minted-text-primary)`, etc.) — never hardcoded Tailwind color classes.

**Icons:** Uses PrimeIcons (`pi pi-*`) for all icons in the splits module. Material Icons are NOT permitted per project rules.

### 14.5 Integration with Transactions Page

The transactions list has an **inline split dialog** that allows splitting a transaction without navigating away.

#### Backend: `isSplit` Flag
- `TransactionResponse` DTO includes `isSplit: Boolean` field
- `TransactionServiceImpl` queries `SplitTransactionRepository.findSourceTransactionIdsByUserId()` to build a `Set<Long>` of split transaction IDs
- All read methods and `update()` pass `isSplit` flag to `TransactionResponse.from()`
- `create()` returns `isSplit = false` (newly created transactions are never split)

#### Frontend: Split Button in AG Grid
- `ActionsCallbacks` interface extended with optional `onSplit` callback
- "Split" button (pi-users icon) added to transactions AG Grid actions column
- When `isSplit === true`: button shows `split-active` CSS class (accent border + accent-subtle background) and "Already split" tooltip
- When `isSplit === false`: default styling with "Split" tooltip

#### Frontend: Inline Split Dialog
- `splitTransaction(transaction)` opens a 640px `<p-dialog>` pre-filled with transaction data
- Reactive form: description, categoryName, totalAmount, transactionDate, sourceTransactionId
- **Split type selector** (3 buttons with PrimeIcons):
  - **Equal** (`pi-equals`): Auto-divides total evenly with `Math.floor` rounding; remainder goes to first entry (payer)
  - **Unequal** (`pi-chart-pie`): Manual amount entry per friend, no auto-recalculation
  - **By Share** (`pi-percentage`): Percentage input per friend with live amount calculation via `onSharePercentageChange()`
- **Friend management**: Add/remove friends from split; "Me" entry (payer) cannot be removed
- **Validation**: Form validity + minimum 2 participants + split total must match transaction total (within 0.01 tolerance)
- **Submit**: Calls `SplitService.create()` with `SplitTransactionRequest`
- **Dialog cleanup**: `onSplitDialogHide()` resets form, entries, split type, and available friends list
- Helper methods: `getInitials()`, `getSplitTotal()`, `recalculateShares()`, `updateAvailableFriends()`

#### `SplitFriendEntry` Interface (local to TransactionsList)
```typescript
interface SplitFriendEntry {
  friendId: number | null;   // null = "Me" (payer)
  friendName: string;
  avatarColor: string;
  shareAmount: number;
  sharePercentage: number | null;
  isPayer: boolean;
}
```

### 14.6 Modified Files
- `app-routing-module.ts` — Added lazy route for `splits` → `SplitsModule`
- `sidebar.ts` — Added "Splits" nav item (icon: `call_split`, after Statements)
- `actions-cell-renderer.component.ts` — Added `onSplit` callback + Split button with `split-active` styling
- `transactions-list.ts` — Inline split dialog with `FriendService`, `SplitService`, split form, 3 split type calculations
- `transactions-list.html` — Split dialog template with type selector, friend list, share inputs, summary box
- `transaction.model.ts` — Added `isSplit: boolean` to `TransactionResponse`

---

## 15. Avatar Upload Feature

Reusable avatar upload with image cropping for user profiles and friends.

### 15.1 Shared Component: `AvatarUploadComponent`

**Location:** `shared/components/avatar-upload/`

**Inputs:**
| Input | Type | Default | Description |
|-------|------|---------|-------------|
| `currentAvatarUrl` | `string \| null` | `null` | Existing avatar as data URI or image URL |
| `initials` | `string` | `'U'` | Fallback initials when no avatar |
| `color` | `string` | `'#6366f1'` | Background color for initials fallback |
| `label` | `string` | `'Profile Picture'` | Label shown in crop dialog header |
| `size` | `'sm' \| 'md' \| 'lg'` | `'md'` | Avatar ring size (40px / 64px / 96px) |

**Outputs:**
| Output | Type | Description |
|--------|------|-------------|
| `avatarSelected` | `File` | Emits cropped JPEG file ready for API upload |
| `avatarRemoved` | `void` | Emits when user clicks remove button |

**Behavior:**
- Displays circular avatar ring with hover camera overlay
- Click opens native file picker (accepts `image/*`)
- File selection opens crop dialog (`ngx-image-cropper`, 1:1 aspect ratio, 512px output, JPEG)
- Crop dialog shows live circular preview
- "Use this photo" emits cropped `File`, "Cancel" discards
- Remove button (shown only when avatar exists) emits `avatarRemoved`
- Validates: image/* content type, max 2MB

**Dependencies:** `ngx-image-cropper` (standalone component imported in SharedModule)

### 15.2 Profile Integration

- `profile.service.ts` — Added `uploadAvatar(file)` and `deleteAvatar()` methods
- `profile.ts` — Added `onAvatarSelected(file)`, `onAvatarRemoved()` handlers. Avatar stored in `localStorage('avatarBase64')` for sidebar access.
- `profile.html` — Uses `<app-avatar-upload>` with size="lg"
- `sidebar.ts` — `userAvatar` getter reads from localStorage
- `sidebar.html` — Shows `<img>` when avatar exists, initials fallback otherwise

### 15.3 Friends/Splits Integration

- `friend.service.ts` — Added `uploadAvatar(id, file)` and `deleteAvatar(id)` methods
- `friend.model.ts` — Added `avatarBase64: string | null` to `FriendResponse`
- `splits-page.ts` — Added `onFriendAvatarSelected()`, `onFriendAvatarRemoved()`, `getFriend()` helper
- `splits-page.html` — All avatar circles (friend ring, balance cards, split shares, settle dialog, available friends pills) check `avatarBase64` for image, fallback to initials. Edit friend dialog includes `<app-avatar-upload>`.

