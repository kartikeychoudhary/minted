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
| Backgrounds | `--minted-bg-page`, `--minted-bg-card`, `--minted-bg-surface`, `--minted-bg-hover`, `--minted-bg-input`, `--minted-bg-elevated` (menus/overlays) |
| Text | `--minted-text-primary`, `--minted-text-secondary`, `--minted-text-muted` |
| Borders | `--minted-border`, `--minted-border-light`, `--minted-border-strong` (hover) |
| Accent | `--minted-accent`, `--minted-accent-hover`, `--minted-accent-subtle` (10%), `--minted-accent-soft` (16%), `--minted-accent-ring` (22%, focus rings) |
| Semantic | `--minted-success`, `--minted-danger`, `--minted-info`, `--minted-warning`, `--minted-violet` + `-subtle` variants |
| Sidebar | `--minted-sidebar-bg`, `--minted-sidebar-bg-2` (gradient end), `--minted-sidebar-text`, `--minted-sidebar-muted`, `--minted-sidebar-hover`, `--minted-sidebar-active`, `--minted-sidebar-border` |
| Misc | radius `sm/md/lg/xl` (8/12/16/20px), shadows `xs/sm/md/lg/xl`, `--minted-mask`, `--minted-ease`, scrollbar tokens |
| Legacy aliases | `--minted-surface`, `--minted-primary`, `--minted-error`, `--minted-error-subtle` |

Tailwind also exposes these tokens (`tailwind.config.js`): `text-primary` / `bg-primary/10` follow the accent, and
`text-minted-text-primary`, `border-minted-border`, `bg-minted-surface`, `bg-minted-card` etc. follow light/dark mode.

### Layout primitives (`styles.scss`)

| Class | Use |
|-------|-----|
| `.page-container` (+ `--narrow`) | Page wrapper: max width, padding, fade-in |
| `.page-header`, `.page-title`, `.page-subtitle`, `.page-eyebrow`, `.page-actions` | Consistent page headings |
| `.surface-card` (+ `__header`, `__title`, `__body`), `.grid-card` | Card surfaces; `.grid-card` frames AG Grid |
| `.icon-tile` (`is-success/danger/info/warning/violet/neutral`) | Tinted icon squares |
| `.segmented` / `.segmented__item.is-active` | Segmented control (date range filters) |
| `.pill` (`is-accent/success/danger/info/warning`) | Status / count badges |
| `.empty-state` (+ `__icon`, `__title`, `__text`) | Empty states |
| `.callout`, `.field-label`, `.back-btn` | Info boxes, form labels, detail-page back button |

In dark mode, light Tailwind tints (`bg-red-100`, `text-green-700`, …) are automatically softened.

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

Sets `--minted-accent`, `--minted-accent-hover`, `--minted-accent-subtle`, `--minted-accent-soft`, `--minted-accent-ring` CSS vars AND updates PrimeNG Aura primary palette via `updatePreset()` from `@primeng/themes`.

---

## PrimeNG Setup

```typescript
// app-module.ts
providePrimeNG({
  theme: {
    preset: MintedPreset,          // core/theme/minted-preset.ts
    options: { darkModeSelector: '.dark-mode' }
  }
})
```

`MintedPreset` extends Aura and maps every colour-scheme token (primary, highlight, form fields, content,
overlays, lists, navigation) to `--minted-*` variables — the same map is used for light and dark, since the
variables themselves switch. It also sets radii, form-field padding and the accent focus ring.
`styles.scss` adds shape refinements (buttons, dialogs, tables, toasts, tooltips, tags, stepper) on top.

---

## AG Grid v35 Theming API

AG Grid v35+ uses TypeScript theming API — no CSS class themes.

All grids share one theme from `shared/theme/grid-theme.ts` (colours are `--minted-*` vars):

```typescript
import { mintedGridTheme } from '../../../../shared/theme/grid-theme';

mintedTheme = mintedGridTheme.withParams({ rowHeight: 60 }); // per-grid sizing only
```

Wrap grids in `.grid-card` for the card frame.

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
// Mobile: all dialogs max 94vw
@media (max-width: 767px) {
  .p-dialog { max-width: 94vw !important; }
  ag-grid-angular { height: 60vh !important; min-height: 300px; }
}
```

---

## Icons

All icons use PrimeNG Icons (`pi pi-*`) exclusively. Font Awesome / FortAwesome and Material Icons are NOT installed.
