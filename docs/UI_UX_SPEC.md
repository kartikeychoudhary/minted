# UI_UX_SPEC.md — Minted Visual Design & User Experience

---

## 1. Design Philosophy

Minted should feel like a **clean, modern fintech dashboard** — not a bulky enterprise app. Think: Notion-level simplicity meets a banking app's clarity. The UI should be:

- **Clean:** Plenty of whitespace, no clutter
- **Informative:** Key numbers visible at a glance
- **Actionable:** One-click to add transaction, minimal navigation
- **Consistent:** Same card style, same button style, same spacing everywhere

---

## 2. Color Palette

> All colors are implemented as CSS custom properties (`--minted-*`) in `styles.scss`.
> Components should use `var(--minted-*)` tokens, not raw hex values, to support dark mode and accent switching.

### Accent Colors (switchable via ThemeService)
| Name | Hex | Hover | CSS Variable |
|------|-----|-------|--------------|
| Amber (default) | `#c48821` | `#a87315` | `--minted-accent` |
| Emerald | `#10b981` | `#059669` | — |
| Blue | `#3b82f6` | `#2563eb` | — |
| Violet | `#8b5cf6` | `#7c3aed` | — |
| Rose | `#f43f5e` | `#e11d48` | — |
| Teal | `#14b8a6` | `#0d9488` | — |

### Semantic Colors
| Name | Hex | Subtle | CSS Variable | Usage |
|------|-----|--------|--------------|-------|
| Success | `#16a34a` | `rgba(22,163,74,0.10)` | `--minted-success` | Income amounts, positive values |
| Danger | `#dc2626` | `rgba(220,38,38,0.10)` | `--minted-danger` | Expense amounts, negative values |
| Info | `#3b82f6` | `rgba(59,130,246,0.10)` | `--minted-info` | Transfer type indicator |

### Surface & Text (Light Mode)
| Token | Hex | CSS Variable | Usage |
|-------|-----|--------------|-------|
| Page BG | `#f8fafc` | `--minted-bg-page` | Page background |
| Card BG | `#ffffff` | `--minted-bg-card` | Card/dialog surfaces |
| Surface BG | `#f1f5f9` | `--minted-bg-surface` | Table headers, secondary surfaces |
| Hover BG | `#f8fafc` | `--minted-bg-hover` | Row/item hover state |
| Input BG | `#ffffff` | `--minted-bg-input` | Form input backgrounds |
| Text Primary | `#0f172a` | `--minted-text-primary` | Headings, body text |
| Text Secondary | `#475569` | `--minted-text-secondary` | Labels, descriptions |
| Text Muted | `#94a3b8` | `--minted-text-muted` | Table headers, placeholders |
| Border | `#e2e8f0` | `--minted-border` | Card borders, dividers |
| Border Light | `#f1f5f9` | `--minted-border-light` | Row separators |

### Surface & Text (Dark Mode)
| Token | Hex | Usage |
|-------|-----|-------|
| Page BG | `#0f172a` | Dark page background |
| Card BG | `#1e293b` | Dark card surfaces |
| Surface BG | `#162032` | Dark table headers |
| Hover BG | `#253348` | Dark hover state |
| Text Primary | `#f1f5f9` | Dark mode headings |
| Text Secondary | `#94a3b8` | Dark mode labels |
| Text Muted | `#64748b` | Dark mode placeholders |
| Border | `#334155` | Dark borders |
| Sidebar BG | `#052e16` | Darker sidebar in dark mode |

### Sidebar
| Element | Value |
|---------|-------|
| Sidebar BG (light) | `#166534` via `--minted-sidebar-bg` |
| Sidebar BG (dark) | `#052e16` |

### Tailwind Custom Colors
| Name | Hex | Usage |
|------|-----|-------|
| `primary` | `#c48821` | Utility classes (`text-primary`, `bg-primary`) |
| `primary-dark` | `#9d6d1a` | Hover variant |
| `minted-green` | `#0f3d32` | Brand green (Tailwind only) |
| `minted-green-light` | `#1a5446` | Lighter brand green |
| `background-light` | `#f8f7f6` | Warm background alt |
| `background-dark` | `#201b12` | Warm dark background alt |
| `minted.50`–`minted.900` | Green scale | Green palette (`#f0fdf4` to `#14532d`) |

### Design Token Radius & Shadows
| Token | Value | CSS Variable |
|-------|-------|--------------|
| Small radius | `6px` | `--minted-radius-sm` |
| Medium radius | `10px` | `--minted-radius-md` |
| Large radius | `14px` | `--minted-radius-lg` |
| Small shadow | `0 1px 3px rgba(0,0,0,0.04)` | `--minted-shadow-sm` |
| Medium shadow | `0 4px 12px rgba(0,0,0,0.08)` | `--minted-shadow-md` |

---

## 3. Typography

Primary font is **Inter** (loaded from Google Fonts), with system fallbacks:

```scss
font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
```

### Icons
- **PrimeIcons** (`primeicons/primeicons.css`) — all icons across the app (sidebar, header, grid actions, buttons, cards)
- **Material Icons** (Google Fonts) — sidebar navigation items only

| Element | Size | Weight | Color |
|---------|------|--------|-------|
| Page Title | 24px (`text-2xl`) | 700 (bold) | Text Primary |
| Section Title | 18px (`text-lg`) | 600 (semibold) | Text Primary |
| Card Title | 16px (`text-base`) | 600 | Text Primary |
| Body Text | 14px (`text-sm`) | 400 | Text Primary |
| Label | 12px (`text-xs`) | 500 (medium) | Text Secondary |
| Amount (Large) | 28px (`text-3xl`) | 700 | Income/Expense color |
| Amount (Table) | 14px (`text-sm`) | 600 | Income/Expense color |

---

## 4. Layout Structure

### 4.1 Overall Layout
```
┌──────────────────────────────────────────────────────┐
│  HEADER (fixed top, h-16)                            │
│  [☰ Hamburger] [Minted Logo]      [User Avatar ▼]   │
├─────────┬────────────────────────────────────────────┤
│         │                                            │
│ SIDEBAR │  MAIN CONTENT AREA                         │
│ (w-64)  │  (with padding p-6)                        │
│         │                                            │
│ [🏠 Home] │  Page Title                              │
│ [💳 Trans]│  ┌────────────┐ ┌────────────┐          │
│ [⚙ Sett.] │  │  Card      │ │  Card      │          │
│         │  └────────────┘ └────────────┘          │
│         │  ┌────────────────────────────┐          │
│         │  │  Full Width Card           │          │
│         │  └────────────────────────────┘          │
│         │                                            │
├─────────┴────────────────────────────────────────────┤
│  FOOTER (optional, minimal)                          │
└──────────────────────────────────────────────────────┘
```

### 4.2 Sidebar Navigation Items
| Order | Label | Icon (Material) | Route | Section |
|-------|-------|-----------------|-------|---------|
| 1 | Dashboard | `dashboard` | `/` | Main |
| 2 | Transactions | `receipt_long` | `/transactions` | Main |
| 3 | Recurring | `sync_alt` | `/recurring` | Main |
| 4 | Import | `upload_file` | `/import` | Main |
| 5 | Analytics | `pie_chart` | `/analytics` | Main |
| 6 | Settings | `settings` | `/settings` | Management |
| 7 | Server Jobs | `schedule` | `/admin/jobs` | Admin (ADMIN only) |
| 8 | Server Settings | `dns` | `/admin/settings` | Admin (ADMIN only) |

### 4.3 Sidebar Layout
- **Desktop:** Fixed left sidebar, toggleable between 256px (`w-64`) open and 80px (`w-20`) closed
- **Background:** `var(--minted-sidebar-bg)` — `#166534` light, `#052e16` dark
- **Navigation:** Material Icons + labels (open) or icons-only with PrimeNG tooltips (closed)
- **Sections:** Main (Dashboard, Transactions, Analytics), Management (Settings), Support (Help Center)
- **Active state:** `routerLinkActive` applies `bg-white/10` with border
- **Logo:** `account_balance_wallet` Material Icon + "Minted" text, accent colored
- **User section:** Avatar (initials, accent bg), name, email, dropdown menu (Profile Settings, Logout)
- **Toggle button:** Floating circle at `-right-3`, chevron icon, 300ms cubic-bezier transition

### 4.3 Header
- Height: 64px (`h-16`)
- Background: `var(--minted-bg-card)` with `var(--minted-border)` bottom border
- Left: "Minted" title
- Right: Dark mode toggle (Material Icons `dark_mode`/`light_mode`) + notification bell with red indicator dot

---

## 5. Component Design Patterns

### 5.1 Summary Cards (Dashboard Top Row)
```
┌─────────────────────────────┐
│ ● Total Income              │  ← Label (text-xs, text-secondary)
│                             │
│ ₹1,25,000                  │  ← Amount (text-3xl, bold, green)
│                             │
│ ↑ 12% from last month      │  ← Trend (text-xs, green/red)
└─────────────────────────────┘
```
- PrimeNG `p-card` with custom styling
- Left color accent bar (4px border-left with category color)
- Icon in top-right corner (FortAwesome)
- Responsive: 4 columns on desktop, 2 on tablet, 1 on mobile

### 5.2 Chart Cards (Dashboard)
```
┌──────────────────────────────┐
│ Monthly Expenses        ⚙   │  ← Title + Settings gear
│                              │
│   ┌──────────────────────┐   │
│   │                      │   │
│   │    [Chart Area]      │   │
│   │                      │   │
│   └──────────────────────┘   │
│                              │
└──────────────────────────────┘
```
- PrimeNG `p-card` containing `p-chart`
- Settings icon opens dialog for modifying axes/type
- Minimum height: 300px for chart area
- HALF cards: 50% width on desktop, 100% on mobile
- FULL cards: 100% width always

### 5.3 Transaction Row (AG Grid)
```
│ 15 Feb │ 🍕 Food & Dining │ Lunch at cafe    │ HDFC Credit Card │ EXPENSE │  -₹450.00 │ ✏️ 🗑️ │
```
- Date formatted: `dd MMM yyyy`
- Category: Icon + Name with colored dot
- Amount: Right-aligned, green for income, red for expense, blue for transfer
- Amount format: `₹XX,XXX.XX` (Indian formatting by default, user's currency)
- Actions: Edit pencil, delete trash

### 5.4 Soft-Deleted Row Pattern (Settings Tables)
```
│ 🏦 │ ~~My Custom Type~~ [Deleted] │ ~~Description text~~ │ [↩ Undo] │
```
- Row opacity: 55% (`opacity: 0.55`)
- Name and description: `text-decoration: line-through`, color demoted to `--minted-text-secondary`
- "Deleted" badge: red background (`bg-red-50 text-red-800`), inline after name
- Actions column: Edit/Delete buttons replaced by single "Undo" button (PrimeNG `p-button` with `pi pi-undo` icon, success style)
- Sorted to bottom of table (active items first, soft-deleted last)
- Used in: Account Types tab (Settings)

### 5.5 Form Fields
- All inputs use PrimeNG components with `p-float-label` pattern
- Validation errors shown below field in red text
- Required fields marked with asterisk
- Consistent spacing: `mb-4` between fields
- Buttons: Primary action (PrimeNG `p-button` raised), Secondary (outlined)

### 5.5 Empty States
```
┌──────────────────────────────────────┐
│                                      │
│         📋                           │
│                                      │
│   No transactions found              │
│   Start by adding your first         │
│   transaction                        │
│                                      │
│   [+ Add Transaction]                │
│                                      │
└──────────────────────────────────────┘
```
- Centered icon (FortAwesome, 48px, muted color)
- Message in secondary text color
- CTA button below

---

## 6. Page-Specific Designs

### 6.1 Login Page
```
┌──────────────────────────────────────────┐
│                                          │
│                                          │
│          ┌──────────────────┐            │
│          │  🪙 Minted       │            │
│          │                  │            │
│          │  Username        │            │
│          │  [____________]  │            │
│          │                  │            │
│          │  Password        │            │
│          │  [____________]  │            │
│          │                  │            │
│          │  [   Login    ]  │            │
│          │                  │            │
│          └──────────────────┘            │
│                                          │
│                                          │
└──────────────────────────────────────────┘
```
- Centered card, max-width 400px
- Subtle gradient or minted-green accent
- No sidebar, no header — standalone page
- Full viewport height, vertically centered

### 6.2 Dashboard Page
```
┌──────────────────────────────────────────────────────┐
│ Financial Overview  [Account▼] [Period▼] [Start] to [End] │  ← Title + filters
│ Good morning, User                                    │
├──────────────────────────────────────────────────────┤
│ ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────┐│
│ │ Income    │ │ Expenses  │ │ Balance   │ │ Count ││  ← KPI cards
│ │₹1.25L     │ │₹89K       │ │₹36K       │ │ 42    ││
│ └───────────┘ └───────────┘ └───────────┘ └───────┘│
│                                                      │
│ ┌──────────────────┐ ┌──────────────────┐            │
│ │ Monthly Expenses │ │ Category Split   │            │  ← Chart cards
│ │   [Bar Chart]    │ │   [Doughnut]     │            │
│ └──────────────────┘ └──────────────────┘            │
│                                                      │
│ ┌────────────────────────────────────────────────┐   │
│ │ Income vs Expense Trend (Last 6 Months)        │   │  ← Full width
│ │   [Line Chart]                                 │   │
│ └────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────┘
```
- **Account filter:** PrimeNG `p-select` with clearable option — filters KPIs and charts by account (passes `accountId` to backend analytics APIs)
- **Period selector:** PrimeNG `p-select` — This Month, Last Month, Last 3/6 Months, This Year, Custom Range
- **Custom date range:** Two `p-datepicker` components shown inline when "Custom Range" is selected. Start/end dates mutually constrained. Data loads on date selection.
- **Chart colors:** Always use the configurable palette from Settings → Dashboard Config (category-level colors from DB are not used). 9 presets: Minted, Pastel, Vibrant, Ocean, Sunset, Forest, Berry, Earth, Neon.

### 6.3 Transactions Page
```
┌──────────────────────────────────────────────┐
│ Transactions                [+ Add]          │
├──────────────────────────────────────────────┤
│ [Last Week] [Last Month] [This Month] [Custom]│  ← Quick filter pills
│                                              │
│ Account: [All ▼]  Category: [All ▼]  🔍     │  ← Filter row
├──────────────────────────────────────────────┤
│ Date      │ Category    │ Desc.  │ Account│ ₹│  ← AG Grid
│───────────┼─────────────┼────────┼────────┼──│
│ 15 Feb    │ 🍕 Food     │ Lunch  │ HDFC   │-450│
│ 14 Feb    │ 💼 Salary   │ Feb Sal│ SBI    │+80K│
│ 14 Feb    │ 🏠 Rent     │ Feb    │ SBI    │-15K│
│ ...       │ ...         │ ...    │ ...    │...│
├──────────────────────────────────────────────┤
│ ◀ 1 2 3 ... 10 ▶           Showing 1-20/195 │  ← Pagination
└──────────────────────────────────────────────┘
```

### 6.4 Settings Page
```
┌──────────────────────────────────────────────┐
│ Settings                                     │
├──────────────────────────────────────────────┤
│ [Accounts] [Account Types] [Categories]      │  ← PrimeNG TabView
│ [Budgets] [Profile]                          │
├──────────────────────────────────────────────┤
│                                              │
│  Account Types                    [+ Add]    │
│  ┌──────────┬──────────────┬───────┬───────┐ │
│  │ Name     │ Description  │ Icon  │ Actions│ │
│  ├──────────┼──────────────┼───────┼───────┤ │
│  │ Bank Acc │ Savings/Curr │ 🏦    │ ✏️ 🗑️  │ │
│  │ Credit   │ Credit cards │ 💳    │ ✏️ 🗑️  │ │
│  │ Wallet   │ Cash/Digital │ 👛    │ ✏️ 🗑️  │ │
│  │ ~~Custom~~ [Deleted]    │ ...   │ [↩ Undo]│ │  ← soft-deleted, strikethrough
│  └──────────┴──────────────┴───────┴───────┘ │
│                                              │
└──────────────────────────────────────────────┘
```

---

## 7. Interaction Patterns

### 7.1 Adding a Transaction (Primary CTA)
1. User clicks "+ Add Transaction" (FAB on mobile, button on desktop).
2. PrimeNG dialog opens with form.
3. User selects type → Category dropdown filters accordingly.
4. User fills in amount, description, date, account.
5. User clicks "Save" → Toast success → Dialog closes → Grid refreshes.

### 7.2 Filtering Transactions
1. User clicks quick filter pill (e.g., "Last Month") → Grid immediately refreshes.
2. For custom range: calendar range picker appears → User selects dates → Grid refreshes.
3. Multiple filters are AND-combined.
4. Active filters are shown as removable chips above the grid.

### 7.3 Editing Dashboard Card
1. User clicks gear icon on card header.
2. Dialog opens with: Chart Type dropdown, X-Axis dropdown, Y-Axis dropdown.
3. Preview updates live as user changes options.
4. User clicks "Save" → Card refreshes with new configuration.

### 7.4 Notifications
- **Success:** Green toast (bottom-right), auto-dismiss 3s. E.g., "Transaction added successfully"
- **Error:** Red toast (bottom-right), manual dismiss. E.g., "Failed to save transaction"
- **Warning:** Amber toast. E.g., "Budget exceeded for Food & Dining"
- **Info:** Blue toast. E.g., "Password changed successfully"

---

## 8. Mobile-Specific Adaptations

### 8.1 Navigation
- Bottom tab bar with 3 icons: Dashboard, Transactions, Settings (alternative to sidebar)
- OR: Hamburger menu opening a slide-in sidebar overlay
- Use PrimeNG `p-sidebar` in overlay mode

### 8.2 Transaction Add (Mobile)
- Floating Action Button (FAB) at bottom-right corner
- Opens full-screen form instead of dialog

### 8.3 Dashboard (Mobile)
- Summary cards in a horizontal scrollable row (2 visible, swipe for more)
- Chart cards stack vertically, all full-width
- Reduce chart height to 200px on mobile

### 8.4 AG Grid (Mobile)
- Hide columns: Notes, Tags, Created Date
- Show only: Date, Category, Amount
- Enable horizontal scroll for additional columns
- OR: Switch to a simple card-based list view for < 640px

---

## 9. Theming System

### Architecture
- **`ThemeService`** (`core/services/theme.service.ts`): singleton, `providedIn: 'root'`
- Initialized in `AppComponent.ngOnInit()` via `themeService.init()`
- All state persisted in localStorage (`minted-dark-mode`, `minted-accent`)

### Dark Mode
- Toggle button in header (Material Icons: `dark_mode` / `light_mode`)
- Applies `.dark-mode` class on `<html>` element
- PrimeNG Aura theme picks up dark mode via `darkModeSelector: '.dark-mode'` in `providePrimeNG()` config
- All `--minted-*` CSS variables swap automatically via `.dark-mode {}` block in `styles.scss`
- AG Grid inherits theme via CSS variable references in `themeQuartz.withParams()`
- Palette: modern elevated slate (not pure black) — `#0f172a` page, `#1e293b` cards
- Default: Light mode

### Accent Color Switching
- 6 presets (see Section 2)
- `ThemeService.setAccentColor(hex)` updates:
  1. `--minted-accent`, `--minted-accent-hover`, `--minted-accent-subtle` CSS variables
  2. PrimeNG Aura primary palette via `updatePreset()` from `@primeng/themes`
- All PrimeNG buttons, focus rings, active tabs, toggle switches, progress bars pick up the accent automatically
- AG Grid checkboxes, sort icons, selection background use `var(--minted-accent)`

### PrimeNG Theme
- Base preset: **Aura** (`@primeng/themes/aura`)
- Configured in `app-module.ts` via `providePrimeNG({ theme: { preset: Aura, options: { darkModeSelector: '.dark-mode' } } })`
- Global overrides in `styles.scss` for: buttons, cards, inputs, selects, dialogs, tables, tabs, accordions, progress bars, tooltips, toasts, tags, toggle switches, skeletons, confirm dialogs, date pickers

### AG Grid v35 Theming
- Uses `themeQuartz.withParams()` in component TypeScript (not SCSS imports)
- All params reference `var(--minted-*)` CSS variables
- Applied via `[theme]="mintedTheme"` on `<ag-grid-angular>`
- Additional CSS overrides in `transactions-list.scss` via `::ng-deep`
- Key params: backgroundColor, foregroundColor, borderColor, headerBackgroundColor, rowHoverColor, accentColor, fontFamily, fontSize, rowHeight (60px), headerHeight (48px)

### Privacy Mode (Blur)
- **`PrivacyService`** (`core/services/privacy.service.ts`): singleton, `providedIn: 'root'`
- Initialized in `AppComponent.ngOnInit()` via `privacyService.init()`
- State persisted in localStorage (`minted-privacy-mode`)
- Toggle button in header between theme toggle and notifications bell (icon: `pi-eye` / `pi-eye-slash`)
- Applies `.privacy-mode` class on `<html>` element
- CSS rule: `.privacy-mode .minted-sensitive { filter: blur(8px); user-select: none; pointer-events: none; transition: filter 0.3s ease; }`
- Financial data annotated with `minted-sensitive` class across all pages: dashboard KPIs, analytics summary cards, transaction amounts (AG Grid cellClass), recurring amounts/summaries, account balances, budget amounts, split balances/settlements, import amounts, statement account names

### Global PrimeNG Overrides (styles.scss)
All PrimeNG components are overridden to use `--minted-*` tokens so they respond to dark mode and accent changes:
- **Buttons**: accent bg/border, hover darken, focus ring
- **Cards**: card bg, border, shadow, radius
- **Inputs/Selects**: input bg, border, focus accent ring
- **Dialogs**: card bg, border, header/footer/content
- **Tables**: surface header, muted header text, hover rows, light row borders
- **Tabs**: accent active tab, secondary inactive text
- **Tags**: success/danger/info with subtle backgrounds
- **Scrollbars**: thumb/hover/track via CSS variables

---

## 10. Sidebar Implementation

### Structure
- **Location:** `minted-web/src/app/layout/`
- **Components:** Layout (wrapper), Sidebar (navigation)

### Open State (w-64 / 256px)
- Logo with icon and "Minted" text
- Full navigation labels visible
- Section headers: Main (Dashboard, Transactions, Analytics), Management (Settings), Support (Help Center)
- User profile: avatar with initials, name, email, dropdown menu (Profile Settings, Logout)

### Closed State (w-20 / 80px)
- Logo icon only
- Navigation icons only (Material Icons)
- PrimeNG tooltips on hover (position: right)
- User avatar only
- Section separators (horizontal lines)

### Colors
| Element | Value |
|---------|-------|
| Sidebar background | `#166534` (minted-green) |
| Primary accent | `#c48821` (gold) |
| Text | White with opacity variations |
| Hover | `rgba(255, 255, 255, 0.1)` |
| Active item | `rgba(255, 255, 255, 0.1)` with left border accent |

### Behavior
- Toggle button at `-right-3`, chevron changes direction
- Smooth 300ms cubic-bezier transition
- `routerLink` + `routerLinkActive` for navigation and active state
- User data loaded from localStorage (username, email, displayName)

---

## 11. Stitch UI Design Reference

### Project Details
| Property | Value |
|----------|-------|
| Stitch Project ID | `13720741124727703321` |
| URL | https://stitch.withgoogle.com/projects/13720741124727703321 |
| Color Mode | LIGHT |
| Custom Color | `#c48821` (Golden/Amber accent) |
| Font | Inter |
| Roundness | 8px (ROUND_EIGHT) |
| Canvas Size | 1280x1024 per screen |

### Available Screens (6)
1. `1b135fbf05ff4445be98db5d9b3744f0`
2. `46e76aa7f41c429ab6ef2beffaa29dc4`
3. `c4efa010dfa7413f9d411e1c1c124b1a`
4. `ff2f24fb529d4e2ba68f4e21b55c20f6`
5. `3794909bb82f482b95a0856e6d94e465`
6. `2f35b9b77ecc46e18bd44ae1c34cb4a3`

### Design Adaptation Rules
1. Fetch screen previews to understand layout
2. Adapt to PrimeNG components (not direct HTML copy)
3. Apply Minted color palette from this spec
4. Use Tailwind CSS for layout
5. Follow Angular module-based architecture (no standalone)
6. Use only approved UI libraries

---

## 12. Transaction UI Patterns

### Filter Bar
- Quick date filters as pills: This Month, Last Month, Last 3 Months, Custom Range
- Dropdown filters: All Accounts, All Categories
- Search: PrimeNG `<p-iconfield>` + `<p-inputicon>` (never raw `<span class="p-input-icon-left">`)

### Custom Date Range
- Shown conditionally when "Custom Range" is selected
- Two PrimeNG `<p-datepicker>` components (start/end)
- Call `ChangeDetectorRef.detectChanges()` after toggling visibility

### AG Grid Configuration
- Custom `ag-theme-minted` theme
- Module registration: `ModuleRegistry.registerModules([AllCommunityModule])` in `main.ts` before bootstrap
- Checkbox selection, external filters, native pagination
- Cell renderers for category icons and edit buttons
- `overlayNoRowsTemplate` for empty state messaging
- Bundle budget: initial 3MB+, anyComponentStyle 500kB+

### PrimeNG Component Best Practices
| Use Case | Component |
|----------|-----------|
| Input with icon | `<p-iconfield>` + `<p-inputicon>` |
| Date selection | `<p-datepicker>` |
| Dropdown | `<p-select>` |
| Currency input | `<p-inputNumber mode="currency">` |
| Data table | AG Grid (transactions) or `<p-table>` (settings) |
| Modal | `<p-dialog>` |
| Notifications | `<p-toast>` (requires MessageService in module providers) |
| Confirmations | `<p-confirmDialog>` (requires ConfirmationService in module providers) |

### Color Standards (All Pages)

> Prefer CSS custom properties over Tailwind color classes for theme-aware styling.
> Use `[style.color]="'var(--minted-text-primary)'"` in templates or `var(--minted-*)` in SCSS.

| Element | CSS Variable | Tailwind Fallback |
|---------|-------------|-------------------|
| Primary text | `var(--minted-text-primary)` | `text-slate-900` |
| Labels | `var(--minted-text-secondary)` | `text-slate-600` |
| Muted/meta text | `var(--minted-text-muted)` | `text-slate-500` |
| Card background | `var(--minted-bg-card)` | `bg-white` |
| Card border | `var(--minted-border)` | `border-slate-200` |
| Page background | `var(--minted-bg-page)` | `bg-slate-50` |
| Primary accent | `var(--minted-accent)` | — |
| Income amount | `var(--minted-success)` | class `text-income` |
| Expense amount | `var(--minted-danger)` | class `text-expense` |
| Transfer amount | `var(--minted-info)` | class `text-transfer` |
| Container padding | — | `p-8` |

**Rule:** Tailwind color classes (`text-slate-*`, `bg-white`) do NOT respond to dark mode.
For any element that must adapt to dark mode, use `var(--minted-*)` tokens or the inline style binding pattern used in layout components.

---

## 13. Import Page Designs

### 13.1 Import Wizard (`/import`)
```
┌──────────────────────────────────────────────────────┐
│ Import Transactions            [Import History →]    │
│ Bulk import transactions from CSV files              │
├──────────────────────────────────────────────────────┤
│                                                      │
│ ┌──────────────────────┐ ┌──────────────────────┐   │
│ │ 📄 CSV Import        │ │ 💳 Credit Card       │   │  ← Import type cards
│ │ Import from CSV file │ │ Statement (Soon)     │   │
│ │ [active, bordered]   │ │ [disabled, opacity]  │   │
│ └──────────────────────┘ └──────────────────────┘   │
│                                                      │
│  ① Download Template  ──  ② Upload & Preview  ──  ③ Confirm  │  ← PrimeNG Stepper
│                                                      │
│ ┌──────────────────────────────────────────────────┐ │
│ │  Step 1: Select Account                         │ │
│ │  Account: [HDFC Savings ▼]                      │ │
│ │  [📥 Download Template]                         │ │
│ │  ┌────────────────────────────────────────────┐ │ │
│ │  │ CSV Format Instructions                   │ │ │
│ │  │ Columns: date, amount, type, description, │ │ │
│ │  │ categoryName, notes, tags                 │ │ │
│ │  └────────────────────────────────────────────┘ │ │
│ │                               [Next →]         │ │
│ └──────────────────────────────────────────────────┘ │
│                                                      │
│ Step 2 (after Next):                                 │
│ ┌──────────────────────────────────────────────────┐ │
│ │  [Choose File] [Upload]                         │ │
│ │  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐          │ │
│ │  │ 10   │ │  8   │ │  1   │ │  1   │          │ │  ← Summary mini-cards
│ │  │Total │ │Valid │ │Error │ │Dupl  │          │ │
│ │  └──────┘ └──────┘ └──────┘ └──────┘          │ │
│ │  ┌────────────────────────────────────────────┐ │ │
│ │  │ AG Grid Preview Table                     │ │ │
│ │  │ #│Status│Date│Amount│Type│Desc│Cat│Error  │ │ │
│ │  │ 1│VALID │... │1500  │EXP │... │...│       │ │ │
│ │  │ 2│ERROR │... │...   │... │... │...│Bad dt │ │ │
│ │  │ 3│DUPL  │... │500   │INC │... │...│       │ │ │
│ │  └────────────────────────────────────────────┘ │ │
│ │  [← Back]                         [Next →]     │ │
│ └──────────────────────────────────────────────────┘ │
│                                                      │
│ Step 3 (after Next):                                 │
│ ┌──────────────────────────────────────────────────┐ │
│ │  Summary: Account, File, Rows to import         │ │
│ │  [Toggle] Skip Duplicate Rows                   │ │
│ │  [← Back]              [▶ Start Import]         │ │
│ │  (after import: success card + link to detail)   │ │
│ └──────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────┘
```

### 13.2 Import History (`/import/jobs`)
```
┌──────────────────────────────────────────────────────┐
│ [← Back] Import History                              │
├──────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────────────────┐ │
│ │ AG Grid Import History Table                     │ │
│ │ ID│File Name│Account│Status│Rows│Created│Actions │ │
│ │ 3 │txns.csv │HDFC   │DONE  │8/10│Feb 20 │[View] │ │
│ │ 2 │data.csv │SBI    │FAIL  │0/5 │Feb 19 │[View] │ │
│ │ 1 │test.csv │HDFC   │DONE  │3/3 │Feb 18 │[View] │ │
│ └──────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────┘
```

### 13.3 Import Job Detail (`/import/jobs/:id`)
```
┌──────────────────────────────────────────────────────┐
│ [←] Import #3  [COMPLETED]                          │
│     transactions.csv                                 │
├──────────────────────────────────────────────────────┤
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐        │
│ │Account │ │File Sz │ │Created │ │Import  │        │  ← Metadata cards
│ │HDFC    │ │2.1 KB  │ │Feb 20  │ │CSV     │        │
│ └────────┘ └────────┘ └────────┘ └────────┘        │
│                                                      │
│ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐      │
│ │  10  │ │   8  │ │   1  │ │   1  │ │   8  │      │  ← Row stat cards
│ │Total │ │Valid │ │Dupl  │ │Error │ │Imprt │      │     (semantic colors)
│ └──────┘ └──────┘ └──────┘ └──────┘ └──────┘      │
│                                                      │
│ 🔄 Import in progress. Auto-refreshing every 5s...  │  ← Polling indicator
│                                                      │
│ Job Execution Steps                                  │
│ ┌──────────────────────────────────────────────────┐ │
│ │ ▬▬▬▬▬▬▬ (green bar)                             │ │
│ │ ① Re-validate CSV Data              [COMPLETED] │ │
│ │    245ms • 10:00:01.123                         │ │
│ │    {"totalRows":10,"validRows":8,"errorRows":1} │ │
│ └──────────────────────────────────────────────────┘ │
│ ┌──────────────────────────────────────────────────┐ │
│ │ ▬▬▬▬▬▬▬ (green bar)                             │ │
│ │ ② Check Duplicates                  [COMPLETED] │ │
│ │    120ms • 10:00:01.368                         │ │
│ │    {"duplicatesFound":1,"skipped":1}            │ │
│ └──────────────────────────────────────────────────┘ │
│ ... more steps ...                                   │
└──────────────────────────────────────────────────────┘
```

### 13.4 Design Notes
- All styling uses `var(--minted-*)` CSS variables for dark mode support
- AG Grid uses same minted theme as transactions page (`themeQuartz.withParams()`)
- Status badges are color-coded: VALID/COMPLETED (green), ERROR/FAILED (red), DUPLICATE (amber), IMPORTING/RUNNING (blue), PENDING/VALIDATED (gray)
- Auto-refresh polling uses RxJS `interval(5000)` with `takeWhile()` and proper `ngOnDestroy` cleanup
- Step timeline cards have colored top bars matching step status
