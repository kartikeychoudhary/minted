---
title: Theming & Design System — Web
feature: theme
layer: web
module: core (ThemeService, CurrencyService)
related:
  - docs/features/web/layout.md
  - docs/features/web/dashboard.md  (chart color palette)
---

# Theming & Design System — Web

## Overview

CSS custom property (design token) system with dark mode support, 6 accent presets, PrimeNG Aura theming, and AG Grid v35 theming API.

---

## Design Tokens (`styles.scss`)

All colors use `--minted-*` CSS variables defined in `:root` (light mode) and overridden in `.dark-mode` (dark mode applied to `<html>` element).

### Token Categories

| Category | Variables |
|----------|-----------|
| Backgrounds | `--minted-bg-page`, `--minted-bg-card`, `--minted-bg-surface`, `--minted-bg-hover`, `--minted-bg-input` |
| Text | `--minted-text-primary`, `--minted-text-secondary`, `--minted-text-muted` |
| Borders | `--minted-border`, `--minted-border-light` |
| Accent | `--minted-accent`, `--minted-accent-hover`, `--minted-accent-subtle` |
| Semantic | `--minted-success`, `--minted-danger`, `--minted-info`, `--minted-warning` + subtle variants |
| Sidebar | `--minted-sidebar-bg` |
| Misc | 3 radius tokens, 2 shadow tokens, 3 scrollbar tokens |

**Rule:** Always use `var(--minted-text-primary)` — NOT `text-slate-900`. Always use `var(--minted-bg-card)` — NOT `bg-white`. Hardcoded Tailwind color classes break dark mode.

---

## ThemeService (`core/services/theme.service.ts`)

Singleton (`providedIn: 'root'`). Called via `themeService.init()` in `AppComponent.ngOnInit()`.

**Dark mode:**
- Toggles `.dark-mode` class on `<html>`
- Persisted in `localStorage` key `minted-dark-mode`

**Accent presets (6):**
| Name | Color |
|------|-------|
| Amber (default) | `#c48821` |
| Emerald | `#10b981` |
| Blue | `#3b82f6` |
| Violet | `#8b5cf6` |
| Rose | `#f43f5e` |
| Teal | `#14b8a6` |

Sets `--minted-accent`, `--minted-accent-hover`, `--minted-accent-subtle` CSS vars AND updates PrimeNG Aura primary palette via `updatePreset()` from `@primeng/themes`.

---

## PrimeNG Setup

```typescript
// app.module.ts
providePrimeNG({
  theme: {
    preset: Aura,
    options: { darkModeSelector: '.dark-mode' }
  }
})
```

All PrimeNG components overridden in `styles.scss` to use `--minted-*` vars:
- Buttons, cards, inputs, selects, dialogs, tables, tabs, accordions
- Progress bars, skeletons, tooltips, toasts, confirm dialogs, tags, toggle switches

---

## AG Grid v35 Theming API

AG Grid v35+ uses TypeScript theming API — no CSS class themes.

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
});
```

Applied via `[theme]="mintedTheme"` on `<ag-grid-angular>`.

**Critical:** `ModuleRegistry.registerModules([AllCommunityModule])` MUST be called in `main.ts` BEFORE `bootstrapModule()`.

---

## CurrencyService (`core/services/currency.service.ts`)

Singleton (`providedIn: 'root'`). Inject in ALL components that display monetary values.

- `format(value)` — formats number with current currency symbol + locale
- `currentCurrency` — current currency code
- `currentLocale` — current locale string
- `setCurrency(code)` — switch currency (persisted in `localStorage`)
- `currencies[]` — list of supported currencies

---

## Semantic Utility Classes (`styles.scss`)

```scss
.text-income  { color: var(--minted-success); }
.text-expense { color: var(--minted-danger); }
.text-transfer { color: var(--minted-info); }
```

---

## Global Responsive Overrides (`styles.scss`)

```scss
// Mobile: all dialogs max 90vw
@media (max-width: 767px) {
  .p-dialog { max-width: 90vw !important; }
  ag-grid-angular { height: 60vh !important; min-height: 300px; }
}
```

---

## Icons

All icons use PrimeNG Icons (`pi pi-*`) exclusively. Font Awesome / FortAwesome and Material Icons are NOT installed.
