# PROJECT SUMMARY — Minted Quick Reference

> **Generated:** February 16, 2026 | **Updated:** February 27, 2026
> **Status:** All core features implemented

---

## 📋 Essential Reading Order (Before ANY Code)

1. ✅ **CLAUDE.md** - Master instructions, tech stack rules, critical constraints
2. ✅ **MISTAKES.md** - Error log and prevention rules (read at start of EVERY task)
3. ✅ **DEVELOPMENT_PROCESS.md** - Step-by-step process for each feature
4. ✅ **QUICKSTART.md** - Implementation phase order
5. 📖 **BACKEND_SPEC.md** - When building backend features
6. 📖 **FRONTEND_SPEC.md** - When building frontend features
7. 📖 **UI_UX_SPEC.md** - When designing/styling UI components
8. 📖 **API_SPEC.md** - When integrating frontend with backend
9. 📖 **STITCH_UI_REFERENCE.md** - When fetching design references

---

## 🎯 Project Goal

Build **Minted** - a personal budget and expense management web application that allows users to:
- Track income, expenses, and transfers across multiple accounts
- Categorize transactions with custom categories and icons
- Set and monitor budgets by category and time period
- Visualize spending patterns with configurable dashboard charts
- Manage accounts, account types, categories, and budgets through a settings interface

---

## 🛠 Tech Stack (LOCKED - DO NOT CHANGE)

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot 3.x
- **Build:** Gradle 8.x (Groovy DSL)
- **Database:** MySQL 8.x
- **Migrations:** Flyway (versioned SQL files)
- **Auth:** JWT (jjwt 0.12.x)
- **Encryption:** Jasypt Spring Boot 3.x
- **API Docs:** SpringDoc OpenAPI 2.x

### Frontend
- **Framework:** Angular 17/18 (**NON-STANDALONE COMPONENTS ONLY**)
- **UI Library:** PrimeNG (latest compatible)
- **Data Grid:** AG Grid Community
- **CSS:** Tailwind CSS 3.x
- **Icons:** FortAwesome (Font Awesome)
- **Charts:** PrimeNG Charts (Chart.js wrapper)

### ❌ FORBIDDEN
- Angular Material, Bootstrap, NgRx, Standalone components
- Lodash, Moment.js, any library not in tech stack

---

## 📁 Project Structure

```
minted/
├── minted-api/              # Spring Boot backend
│   ├── src/main/
│   │   ├── java/com/minted/api/
│   │   │   ├── common/       # Shared: config, exception, filter (MdcFilter, JwtAuthFilter), util
│   │   │   ├── auth/         # Authentication (controller, dto, service)
│   │   │   ├── user/         # User entity & profile
│   │   │   ├── account/      # Accounts & account types
│   │   │   ├── transaction/  # Transactions & categories
│   │   │   ├── budget/       # Budget management
│   │   │   ├── dashboard/    # Dashboard cards & charts
│   │   │   ├── analytics/    # Analytics & reporting
│   │   │   ├── recurring/    # Recurring transactions
│   │   │   ├── notification/ # Notification system
│   │   │   ├── job/          # Job execution framework (shared)
│   │   │   ├── bulkimport/   # CSV bulk import
│   │   │   ├── statement/    # Credit card statement parser
│   │   │   ├── llm/          # LLM config & merchant mappings
│   │   │   ├── split/        # Split transactions & friends
│   │   │   └── admin/        # Admin management & settings
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── logback-spring.xml  # MDC-enriched logging (dev/prod profiles)
│   │       └── db/migration/ # Flyway migrations (V0_0_1 through V0_0_31)
│   └── build.gradle
│
├── minted-web/              # Angular frontend
│   ├── src/app/
│   │   ├── core/            # Singletons: services, guards, interceptors, models
│   │   ├── shared/          # Shared components, pipes, directives
│   │   ├── modules/
│   │   │   ├── auth/        # Login, signup, change password
│   │   │   ├── dashboard/   # Dashboard with charts
│   │   │   ├── transactions/ # Transaction management (AG Grid)
│   │   │   ├── recurring/   # Recurring transactions
│   │   │   ├── analytics/   # Analytics overview
│   │   │   ├── import/      # Bulk CSV importer
│   │   │   ├── notifications/ # Notifications full page
│   │   │   ├── splits/       # Bill splitting with friends
│   │   │   ├── settings/    # Settings tabs (Profile, Accounts, Categories, Budgets)
│   │   │   └── admin/       # User management, jobs, server settings
│   │   └── layout/          # Sidebar, header, notification drawer
│   └── package.json
│
└── docs/
    ├── DEVELOPMENT_PROCESS.md    # Feature dev workflow
    ├── MISTAKES.md              # Error log
    ├── BACKEND_SPEC.md          # Backend details
    ├── FRONTEND_SPEC.md         # Frontend details
    ├── UI_UX_SPEC.md            # Design system
    ├── API_SPEC.md              # REST API contract
    ├── STITCH_UI_REFERENCE.md   # Stitch project info
    ├── PROJECT_HISTORY.md       # Phase completion log
    ├── LOGGING.md               # Structured logging & request tracing
    └── PROJECT_SUMMARY.md       # This file
```

---

## 🎨 Design Resources

### Stitch UI Project
- **URL:** https://stitch.withgoogle.com/projects/13720741124727703321
- **Project ID:** 13720741124727703321
- **Screens:** 6 desktop screens (1280x1024)
- **Theme:** Light mode, Inter font, #c48821 accent, 8px roundness
- **Usage:** Visual reference for UI implementation (adapt to PrimeNG)

### Color Palette
| Color | Hex | Usage |
|-------|-----|-------|
| Minted Green | #22c55e | Primary buttons, income |
| Expense Red | #ef4444 | Expense amounts |
| Transfer Blue | #3b82f6 | Transfer indicator |
| Warning Amber | #f59e0b | Budget warnings |

---

## 📅 Implementation Phases

### Phase 1: Project Scaffolding ✅
- [x] Backend: Spring Boot + Gradle + Flyway configured
- [x] Frontend: Angular + PrimeNG + Tailwind v3 + AG Grid
- [x] Both projects compile and run

### Phase 2: Authentication (Backend) ✅
- [x] Users table (V0_0_1) with default admin user
- [x] JWT authentication with jjwt 0.12.6
- [x] Spring Security configuration
- [x] Auth endpoints: /login, /refresh, /change-password
- [x] DTOs, exceptions, global error handler

### Phase 3: Authentication (Frontend) ✅
- [x] CoreModule: AuthService, AuthGuard, JWT/Error interceptors
- [x] SharedModule with PrimeNG component exports
- [x] AuthModule: Login + Change Password components
- [x] Routing with lazy loading and guards

### Phase 4: Core Entities (Backend) ✅
- [x] Flyway migrations V0_0_1 through V0_0_8
- [x] All JPA entities, repositories, services, controllers
- [x] 12+ DTOs (request + response records)

### Phase 5: Settings Page (Frontend) ✅
- [x] Settings module with tab navigation
- [x] Account Types, Accounts, Categories, Budgets, Profile tabs

### Phase 6: Transactions, Layout & Docker ✅
- [x] Transactions CRUD with AG Grid + filters
- [x] Sidebar/header layout
- [x] Docker containerization (Nginx + Spring Boot + MySQL)

### Phase 7: Post-Core Features ✅
- [x] Analytics Overview module (charts, summaries)
- [x] AG Grid v35 migration with custom `ag-theme-minted`
- [x] Theme System: dark mode, 6 accent presets, CSS custom properties
- [x] CurrencyService (global currency formatting)
- [x] Recurring Transactions module
- [x] Job Scheduling & Admin Module (cron jobs, schedules, default lists)
- [x] Bulk CSV Transaction Importer (wizard, job processing, history)
- [x] User Management & Signup (admin CRUD, signup toggle, public registration)
- [x] Notification System (NotificationHelper, bell badge, drawer, full page)

### Phase 8: Splits & Polish ⏳
- [x] Bill splitting with friends (backend: friends + split transactions + shares + settlement + balance summary)
- [x] Splits module frontend (dedicated `/splits` page with AG Grid, friend management, split CRUD, settle, CSV export)
- [x] Transaction ↔ Split integration (`isSplit` flag on TransactionResponse, inline split dialog in transactions list)
- [x] Mobile responsiveness (hamburger sidebar drawer, responsive dialogs/grids, auth pages)
- [x] Bulk import cron job removed (processing is user-action driven)
- [x] Import wizard: Credit Card Statement card linked to /statements
- [x] Avatar upload: User profile + friends avatar with crop (ngx-image-cropper), LONGBLOB storage, base64 data URI
- [ ] Configurable dashboard cards
- [ ] Budget tracking polish

---

## 🔄 Development Workflow (for EVERY feature)

### Phase 0: Pre-Flight
1. Read MISTAKES.md in full
2. Re-read relevant spec file
3. Ensure project builds cleanly

### Phase 1: Backend First (feature-based modules)
1. Database migration (Flyway SQL)
2. JPA Entity in `<feature>/entity/`
3. Repository in `<feature>/repository/`
4. DTOs in `<feature>/dto/`
5. Service (interface + impl) in `<feature>/service/`
6. Controller in `<feature>/controller/`
7. Test

### Phase 2: Frontend
1. Model/Interface (TypeScript)
2. API Service (HttpClient)
3. Module Setup (if new)
4. Component (with --standalone=false)
5. Template & Styling (PrimeNG + Tailwind)
6. Verify end-to-end

### Phase 3: Integration & Polish
1. Test full flow
2. Check responsive design
3. Verify error handling
4. Add loading states
5. Add toast notifications

### Phase 4: Commit Checkpoint
1. Ensure builds pass
2. Update MISTAKES.md if needed
3. Write clear summary

---

## 🚨 Critical Rules (TOP 5)

1. **NEVER generate standalone Angular components** - Always use `--standalone=false`
2. **NEVER add libraries outside approved tech stack** - No Material, No Bootstrap, No NgRx
3. **NEVER use Hibernate auto-DDL** - Always use Flyway migrations with `validate`
4. **NEVER expose JPA entities in REST responses** - Always use DTOs
5. **NEVER hardcode secrets** - Always use environment variables

---

## 🔧 Quick Commands

### Backend
```bash
cd minted-api
./gradlew bootRun              # Start dev server
./gradlew build                # Build
./gradlew test                 # Run tests
./gradlew flywayMigrate        # Run migrations
```

### Frontend
```bash
cd minted-web
ng serve                        # Dev server (localhost:4200)
ng build                        # Production build
ng generate module modules/<name> --routing
ng generate component modules/<name>/components/<comp> --module=modules/<name> --standalone=false
```

---

## 📚 Reference Repositories (Study for Patterns)

- **Backend:** https://github.com/kartikeychoudhary/wheremybuckgoes
  - Spring Boot backend with Gradle, MySQL, JWT, Jasypt, Flyway
  - Transaction CRUD, account management, Gemini AI integration

- **Frontend:** https://github.com/kartikeychoudhary/expense_track
  - Angular frontend (TypeScript 66%, HTML 31%, CSS 3%)
  - Expense tracking UI, transaction views, settings, dashboard

**Extract all features from these repos into Minted.**

---

## 🎯 Current Status

**Phases 1–7 complete.** All core features are implemented and working.

- **Backend:** 39 Flyway migrations (V0_0_1 through V0_0_39), 15+ entities, full REST API
- **Frontend:** 10 feature modules (auth, dashboard, transactions, recurring, analytics, import, notifications, splits, settings, admin), layout with sidebar + notification drawer + mobile responsive
- **Infrastructure:** Docker Compose (Nginx + Spring Boot + MySQL), ports 7800 (web) / 7801 (API)
- **Remaining:** Configurable dashboard cards, budget tracking polish

### Key Architectural Highlights
- **Feature-based backend modules:** 18 modules (auth, user, account, transaction, budget, dashboard, analytics, recurring, notification, job, bulkimport, statement, llm, split, friend, dashboardconfig, admin, common) — each module has its own controller/dto/entity/repository/service
- **Design tokens:** All colors via `--minted-*` CSS custom properties (light + dark mode)
- **AG Grid v35:** Custom `ag-theme-minted` theme with CSS var references
- **Theme system:** Dark mode toggle, 6 accent presets, PrimeNG Aura preset overrides
- **Notification system:** `NotificationHelper` shared backend component, 30s polling, header bell + drawer + full page
- **Auth:** JWT with force-password-change flow, signup toggle, admin user management
- **Structured logging:** MDC-enriched logs with requestId/userId/method/uri on every line; `MdcFilter` → `JwtAuthFilter` pipeline; `logback-spring.xml` with dev (DEBUG/console), prod (INFO/JSON) profiles; `RequestLoggingInterceptor` for request timing; `@Slf4j` on all service impls for business event logging (see `docs/LOGGING.md`)
- **Bulk import:** CSV wizard with user-action-driven async processing and step-level tracking (no cron job)
- **Splits:** Friend management with soft-delete, 3 split types (Equal/Unequal/Share), balance tracking, settlement with notifications, inline split dialog in transactions, `isSplit` flag on transaction responses, CSV export per friend
- **Avatar upload:** Reusable `AvatarUploadComponent` (shared) with ngx-image-cropper (1:1, 512px JPEG). LONGBLOB storage in DB, base64 data URIs. Integrated in: user profile, friends, sidebar. Two-stage loading: friends list fetched without avatars first (fast), then with avatars (fade-in).
- **Route loading:** Animated accent-colored bar at top of content area during lazy module navigation (Router events in Layout component)
- **Auth pages:** Warm golden gradient background on login/signup screens with enhanced decorative blurs
- **Mobile responsive:** PrimeNG Drawer sidebar on mobile, hamburger menu, global dialog/grid overrides, responsive auth pages

---

## 📝 Next Steps

1. Configurable dashboard cards (drag-and-drop, chart type selection)
2. Budget tracking improvements (progress bars, alerts)

---

## 🤝 Decision Matrix

### ✅ Proceed Without Asking:
- Standard CRUD for defined entities
- Flyway migrations for new tables
- Components within defined module structure
- Fixing build/test errors
- Adding validation annotations

### ⛔ STOP and Ask:
- Adding new dependency not in tech stack
- Changing database schema outside spec
- Removing/replacing existing features
- Changing auth mechanism
- Any architectural changes
- Ambiguous features with multiple implementations

---

**Remember:** Read MISTAKES.md at the start of EVERY task!
