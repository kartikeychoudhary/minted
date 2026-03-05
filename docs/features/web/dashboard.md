---
title: Dashboard — Web
feature: dashboard
layer: web
module: DashboardModule (lazy, /dashboard)
components:
  - DashboardComponent
  - DashboardCardComponent
  - CardConfigDialogComponent
  - AddCardDialogComponent
related:
  - docs/features/api/analytics.md
  - docs/features/web/settings.md  (Dashboard Config tab for chart colors)
---

# Dashboard — Web

## Overview

Home page with KPI summary cards, configurable PrimeNG Chart.js chart cards, account/period filters, and a custom chart color palette.

---

## Module

`DashboardModule` — lazy-loaded at `/dashboard`.

---

## Layout

### Filters (top-right)
- **Account filter** (`p-select`) — filters all KPI cards and charts by account; passes `accountId` to `/summary`, `/category-wise`, `/trend`
- **Period selector** (`p-select`) — This Month, Last Month, Last 3 Months, Last 6 Months, This Year, Custom Range
- **Custom date range** — Two `p-datepicker` components shown when "Custom Range" is selected. Data loads automatically when both dates are filled.

### Summary Cards Row (4 cards)
Each is a `p-card` with colored left border, icon, and amount:
- Total Income (this month)
- Total Expenses (this month)
- Net Balance
- Budget Utilization %

### Chart Cards Grid
- 2-column grid for HALF-width cards, full-width for FULL cards
- Each card: `p-card` with title header + settings icon (PrimeIcon) + PrimeNG `p-chart` (Chart.js)
- Clicking settings opens `CardConfigDialogComponent` (change chart type, X-axis, Y-axis, date range)
- "Add Card" button at bottom → `AddCardDialogComponent`
- Cards draggable for reorder (`pDraggable`/`pDroppable` or position buttons)

---

## Chart Color Palette

Charts use the configurable palette from `DashboardConfigService` (not per-category colors from DB).
- Default: 8 colors
- 9 presets: Minted, Pastel, Vibrant, Ocean, Sunset, Forest, Berry, Earth, Neon
- **Stored client-side only in `localStorage`** — NOT persisted to backend
- Colors are normalized with `#` prefix on read/save (handles PrimeNG ColorPicker hex format)
- Managed in Settings → Dashboard Config tab

---

## Available Axes

**X-axis:** `day`, `week`, `month`, `year`, `category`, `account`, `type`
**Y-axis:** `total_amount`, `count`, `average_amount`
**Chart types:** `BAR`, `LINE`, `PIE`, `DOUGHNUT`, `AREA`, `STACKED_BAR`

---

## Services

**`DashboardService`** — manages dashboard card state and chart data fetching (BehaviorSubject-based)
**`AnalyticsService`** — summary, category-wise, trend queries (accepts accountId, startDate, endDate)
**`DashboardConfigService`** — chart color palette CRUD in localStorage + excluded categories via backend
