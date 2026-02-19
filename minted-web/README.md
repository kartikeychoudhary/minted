# Minted Web — Angular Frontend

The frontend of Minted, built with **Angular 21** using module-based (non-standalone) components, **PrimeNG** for UI primitives, and **AG Grid** for data tables.

---

## 🚀 Quick Start

```bash
# Install dependencies
npm install

# Start dev server (http://localhost:4200)
npm start
```

---

## 🏗 Architecture

```
src/app/
├── core/                  # Singleton services, guards, interceptors
│   ├── services/          # AuthService, ThemeService, TransactionService, etc.
│   ├── guards/            # AuthGuard (route protection)
│   └── interceptors/      # JwtInterceptor, ErrorInterceptor
│
├── shared/                # Shared module (re-exported pipes, components)
│
├── layout/                # App shell — sidebar, header, footer
│   ├── sidebar/           # Brand logo, navigation links
│   └── header/            # Search, notifications, profile, theme toggle
│
└── modules/               # Feature modules (lazy-loaded)
    ├── auth/              # Login page
    ├── dashboard/         # Overview cards, charts, recent transactions
    ├── transactions/      # AG Grid transaction list, add/edit dialog
    ├── analytics/         # Spending analytics charts
    ├── recurring/         # Recurring transaction management
    └── settings/          # Accounts, categories, preferences, profile
```

---

## 🎨 Theming

The app uses a **CSS custom property** design system defined in `src/styles.scss`:

| Token prefix        | Example                     | Purpose                   |
| ------------------- | --------------------------- | ------------------------- |
| `--minted-bg-*`     | `--minted-bg-card`          | Background colors         |
| `--minted-text-*`   | `--minted-text-primary`     | Text colors               |
| `--minted-border*`  | `--minted-border`           | Border colors             |
| `--minted-accent*`  | `--minted-accent`           | Accent / brand color      |
| `--minted-radius-*` | `--minted-radius-sm`        | Border radius             |
| `--minted-shadow-*` | `--minted-shadow-md`        | Box shadows               |

### Dark mode
Toggled via `ThemeService` which adds/removes `.dark-mode` on `<html>`. All `--minted-*` tokens have light and dark variants.

### Accent colors
Six presets: Amber (default), Emerald, Blue, Violet, Rose, Teal. Managed by `ThemeService.setAccentColor()`.

### AG Grid theming
AG Grid v35 uses the **Theming API** (`themeQuartz.withParams()`). All grid colors reference `var(--minted-*)` tokens so they switch automatically with dark mode.

---

## 📦 Key Dependencies

| Package             | Version | Purpose                      |
| ------------------- | ------- | ---------------------------- |
| `@angular/core`     | 21.x    | Framework                    |
| `primeng`           | 19.x    | UI component library         |
| `ag-grid-community` | 35.x    | Data grid                    |
| `ag-grid-angular`   | 35.x    | Angular AG Grid integration  |
| `tailwindcss`       | 3.x     | Utility CSS classes          |
| `chart.js`          | 4.x     | Charts (via PrimeNG Charts)  |
| `primeicons`        | 7.x     | Icon set                     |

---

## 🛠 Scripts

| Command         | Description                     |
| --------------- | ------------------------------- |
| `npm start`     | Dev server on port 4200         |
| `npm run build` | Production build → `dist/`      |
| `npm test`      | Run unit tests (Karma)          |
| `npx ng build`  | Alternative build command       |

---

## 🐳 Docker

The frontend is containerized as a multi-stage build:

1. **Stage 1 (Node 20):** `npm ci` + `npm run build` → production bundle
2. **Stage 2 (Nginx 1.27):** Serves static files, proxies `/api/*` to the backend

See the root [docker-compose.yml](../docker-compose.yml) for full orchestration.

---

## 📖 More Info

- [Root README](../README.md) — Full project overview and setup
- [API README](../minted-api/README.md) — Backend documentation
