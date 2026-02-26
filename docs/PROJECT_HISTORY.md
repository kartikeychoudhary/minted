# PROJECT_HISTORY.md — Minted Development Phases

> Consolidated log of all completed implementation phases.
> For current project status, see `IMPLEMENTATION_STATUS.md` in root.

---

## Phase 1: Project Scaffolding

**Completed:** February 16, 2026

### Deliverables
- Spring Boot 3.2.2 project with Gradle 8.5
- Angular 21 application with module-based (non-standalone) architecture
- All dependencies configured: PrimeNG, AG Grid, Tailwind CSS v3, Font Awesome, Chart.js, jjwt 0.12.x, Jasypt, Flyway
- Tailwind CSS v3 with Minted color palette
- Environment variable configuration for both projects

### Issues Encountered
- **Tailwind CSS v4 Incompatibility**: v4 requires `@tailwindcss/postcss` plugin incompatible with Angular. Downgraded to v3.4.17.

---

## Phase 2: Authentication Backend

**Completed:** February 16, 2026

### Deliverables
- Flyway migration `V0_0_1__create_users_table.sql` with BCrypt-hashed default admin user
- User entity, repository, and service
- JWT utilities using jjwt 0.12.6 (token generation, validation, extraction)
- `JwtAuthFilter` + `SecurityConfig` with CORS support
- `AuthController` with `/login`, `/refresh`, `/change-password` endpoints
- 5 DTOs, 4 custom exceptions, `GlobalExceptionHandler`

### Issues Encountered
- **jjwt 0.12.x API changes**: `parserBuilder()` removed. Migrated to `Jwts.parser().verifyWith().build().parseSignedClaims()` and `Jwts.SIG.HS256`.
- **BCrypt hash mismatch**: Seed data used incompatible hash. Fixed with corrected migration.
- **Flyway checksum mismatch**: Modified applied migration. Learned to never edit applied migrations.

---

## Phase 3: Authentication Frontend

**Completed:** February 2026

### Deliverables
- `CoreModule` with `AuthService`, `AuthGuard`, `AuthInterceptor`, `ErrorInterceptor`
- `AuthModule` with Login and ChangePassword components (PrimeNG)
- JWT storage in localStorage with auto-logout on 401
- Lazy-loaded routing with auth guards
- PrimeNG-themed login form with Minted golden accent (`#c48821`)

---

## Phase 4: Core Entities Backend

**Completed:** February 2026

### Deliverables
- 7 Flyway migrations (V0_0_1 through V0_0_7): users, account_types, accounts, transaction_types, categories, transactions, budgets
- 7 JPA entities with Lombok `@Getter`/`@Setter` (not `@Data`)
- 7 repositories with custom query methods
- 12 DTOs (request + response) using Java Records
- 6 service interfaces + implementations
- 6 REST controllers with `ApiResponse<T>` wrapper
- Lazy loading on all relationships, indexes on transaction_date/user_id/account_id

---

## Phase 5: Settings Module (Frontend)

**Completed:** February 17, 2026

### Deliverables
- **Account Types Tab**: CRUD with data table, pagination, search, modal dialog
- **Accounts Tab**: CRUD with account type selection, balance display, active/inactive toggle
- **Categories Tab**: CRUD with icon selection (12 icons), color selection (9 colors), transaction type filtering
- **Budgets Tab**: Monthly budget management with category assignment, month/year selection
- **Profile Tab**: Personal info management, password change with validation, notification preferences UI
- PrimeNG v21 components (ToggleSwitch, Select, Table, Dialog, Password)
- Bundle size: 72.71 kB (gzipped: 11.24 kB)

### Issues Encountered
- API endpoint correction (`/transaction-categories` to `/categories`)
- PrimeNG v21: `ToggleSwitchModule` replaced deprecated `InputSwitchModule`
- Angular cache fix: cleared `.angular/cache` for path mismatch errors

---

## Phase 6: Transactions, Layout & Docker

**Completed:** February 17, 2026

### Deliverables

#### Transactions Module
- Full CRUD with date/account/category filters and search
- Date filters: This Month, Last Month, Last 3 Months, Custom Range
- PrimeNG Table with pagination, sorting, CSV export
- PrimeNG IconField for search input, DatePicker for custom ranges
- Bundle size: 26.95 kB (lazy-loaded)

#### Sidebar & Layout
- Responsive sidebar with open (256px) / closed (80px) states
- Material Icons navigation with active route highlighting
- User profile section with avatar, dropdown menu, logout
- PrimeNG tooltips in collapsed state
- Sidebar background: `#166534`, accent: `#c48821`

#### Docker Containerization
- Multi-service: Nginx (frontend) + Spring Boot (backend) + MySQL
- `docker-compose.yml` with health checks and service dependencies
- `nginx.conf` for API reverse proxy
- `.env.example` with all configurable environment variables
- Persistent MySQL volume

#### Data & Admin
- `DataInitializer.java`: runtime admin user creation (admin/admin)
- Sample data: 4 accounts, 25 transactions spanning Jan-Feb 2024

### Issues Encountered
- MessageService provider missing in module
- PrimeNG IconField required instead of `<span class="p-input-icon-left">`
- Custom date pickers needed `ChangeDetectorRef.detectChanges()` for immediate rendering
- Flyway: unknown column error from wrong schema assumptions
- Failed migration entries must be deleted from `flyway_schema_history`
- Bundle budget increases needed for AG Grid CSS (~230kB)

---

## Post-Phase Work

### Analytics Overview (February 17, 2026)
- Dashboard with PrimeNG summary cards, charts (spending activity, category breakdown)
- Accordion for recurring payments, tags for status badges, progress bars for forecasts
- All data from existing APIs, no new backend endpoints needed

### AG Grid v35 Migration (February 18, 2026)
- Replaced PrimeNG p-table with AG Grid in Transactions
- Custom `ag-theme-minted` theme matching reference design
- Module registration in `main.ts` before bootstrap (required for v35+)
- Checkbox selection, external filters, cell renderers, native pagination
- Bundle budget: initial 3MB+, anyComponentStyle 500kB+

### Theme System (February 19, 2026)
- `ThemeService`: dark mode class on `<html>`, accent CSS var, PrimeNG preset swapping
- `CurrencyService`: global currency stored in localStorage
- 6 accent color presets (Amber, Emerald, Blue, Violet, Rose, Teal)
- AG Grid v35 Theming API for automatic dark mode support

### Recurring Transactions Module (February 2026)
- Backend: recurring transaction entity, service, controller
- Frontend: recurring transactions management UI

### Job Scheduling & Admin Module (February 2026)
- **Backend Job Framework:**
  - `SchedulerConfig` with `@EnableScheduling` and `ThreadPoolTaskScheduler` (pool=5)
  - `JobSchedulerService`: cron-based scheduling with `ConcurrentHashMap<String, ScheduledFuture<?>>`
  - `JobExecutionService`: execution history CRUD and schedule config management
  - 3 entities: `JobScheduleConfig`, `JobExecution`, `JobStepExecution`
  - 3 enums: `JobStatus`, `JobStepStatus`, `JobTriggerType`
  - 4 DTOs (record classes)
  - Flyway migration `V0_0_16__create_job_tables.sql`
- **RecurringTransactionJob:**
  - Cron: `0 0 1 * * ?` (daily at 1 AM)
  - 3-step execution: Fetch due → Process transactions → Update config
  - Per-transaction error handling with context JSON tracking
  - Registered via `@PostConstruct` from DB config
- **Admin Controller** (`/api/v1/admin`):
  - Job execution list/detail, manual trigger (202 Accepted)
  - Schedule config list/update
  - Default categories and account types CRUD
- **Frontend Admin Module** (lazy-loaded, `adminGuard`):
  - `JobsList`: AG Grid with status badges, pagination, manual trigger button
  - `JobDetail`: execution metadata cards + step timeline with context JSON
  - `ServerSettings`: schedule toggle, default categories/account types grids with add/delete modals
  - `AdminService`: HTTP client for all `/admin` endpoints
  - Sidebar conditionally shows admin items for ADMIN role

### Bulk CSV Transaction Importer (February 20, 2026)
- **Backend (13 new files):**
  - Flyway migration `V0_0_20__create_bulk_imports_table.sql` with indexes and job schedule seed
  - Enums: `ImportStatus` (6 states), `ImportType` (CSV, CREDIT_CARD_STATEMENT)
  - Entity: `BulkImport` with `@JdbcTypeCode(Types.VARCHAR)` on enum fields (Hibernate 6.x + MySQL VARCHAR)
  - Repository: `BulkImportRepository` + additions to `TransactionCategoryRepository` (name lookup) and `TransactionRepository` (duplicate check)
  - DTOs: `CsvRowPreview`, `CsvUploadResponse`, `BulkImportConfirmRequest`, `BulkImportResponse`
  - Service: `BulkImportServiceImpl` with CSV parsing (quoted fields), per-row validation, duplicate detection, 5000 row limit
  - Async 4-step processing via `TransactionTemplate` (cron sweep removed Feb 25 — processing is user-action driven)
  - Controller: `BulkImportController` (6 endpoints: template, upload, confirm, list, get, job details)
- **Frontend (14 new files):**
  - Model: `import.model.ts` with TypeScript interfaces
  - Service: `import.service.ts` (providedIn: 'root', map(r => r.data) pattern)
  - Module: `ImportModule` with lazy-loaded routing (wizard, jobs, job detail)
  - Components: `ImportWizard` (3-step PrimeNG Stepper), `ImportJobs` (AG Grid history), `ImportJobDetail` (auto-refresh polling), `StatusCellRendererComponent`
  - AG Grid theme: exact copy of minted theme from transactions-list
  - Sidebar: Added "Import" nav item with `upload_file` icon
- **Modified files (5):** SharedModule (StepperModule), app routing, sidebar, TransactionCategoryRepository, TransactionRepository

### Issues Encountered (Bulk Import)
- **Hibernate schema validation failure:** `@Enumerated(EnumType.STRING)` without `@JdbcTypeCode(Types.VARCHAR)` caused Hibernate 6.x to expect native MySQL ENUM instead of VARCHAR(30). Fixed by adding `@JdbcTypeCode(Types.VARCHAR)` to both enum fields.
- **Self-invocation bypasses @Transactional:** `processImportAsync()` called via `CompletableFuture.runAsync()` from same class. Fixed with `TransactionTemplate` for programmatic transaction.
- **Race condition:** Async task fired before parent `@Transactional` method committed, causing `job_execution_id` to be NULL. Fixed with `TransactionSynchronizationManager.registerSynchronization(afterCommit)` to defer async work until after commit.

### User Management & Signup (February 21, 2026)
- **Backend (13 new files, 4 modified):**
  - Flyway migration `V0_0_21__create_system_settings_table.sql` — key-value system settings with `SIGNUP_ENABLED` default
  - Entity: `SystemSetting` (id, settingKey, settingValue, description, timestamps)
  - Repository: `SystemSettingRepository` with `findBySettingKey()`
  - 6 DTOs: `AdminUserResponse`, `CreateUserRequest`, `ResetPasswordRequest`, `SignupRequest`, `SystemSettingResponse`, `UpdateSettingRequest`
  - Service: `SystemSettingService` / `SystemSettingServiceImpl` — setting CRUD + `isSignupEnabled()`
  - Service: `UserManagementService` / `UserManagementServiceImpl` — full user CRUD with:
    - Password strength validation (same regex as `AuthServiceImpl`)
    - Default data seeding for new users (account types + categories from defaults)
    - Cascading user delete (transactions, recurring, budgets, accounts, account types, categories, imports, dashboard cards)
    - Self-action prevention (cannot disable/delete own account)
    - `forcePasswordChange = true` on admin-created users and password resets
  - `AuthServiceImpl` extended with `signup()` (public registration with auto-login) and `isSignupEnabled()`
  - `AdminController` — 8 new endpoints: user CRUD + toggle + reset-password + system settings get/update
  - `AuthController` — 2 new endpoints: `POST /signup` (public), `GET /signup-enabled` (public)
- **Frontend (2 new components, 7 modified files):**
  - `UserManagement` component (`/admin/users`):
    - AG Grid user list with columns: Username, Display Name, Email, Role (badge), Status (badge), Password status, Created, Actions
    - Signup toggle card (PrimeNG `p-toggleswitch`) for enabling/disabling public registration
    - Create User dialog (username, password, display name, email, role select)
    - Reset Password dialog with info message about forced password change
    - Delete user with confirmation dialog
    - Minted AG Grid theme (same as server-settings)
  - `Signup` component (`/signup`):
    - Matches login page design (same card layout, decorative blurs, logo, color scheme)
    - Fields: Display Name, Email, Username, Password, Confirm Password
    - Cross-field password match validator
    - Warning banner when registration is disabled (submit button disabled)
    - Auto-login on successful signup
  - Login page cleaned up: removed non-functional Google/Facebook social login buttons, "Create Account" link now conditional on signup being enabled and routes to `/signup`
  - Sidebar: "Users" added as first admin nav item (icon: `group`)
  - Models: `AdminUserResponse`, `CreateUserRequest`, `ResetPasswordRequest`, `SignupRequest`, `SystemSettingResponse`
  - Services: `AdminService` extended with user management + settings methods, `AuthService` extended with `signup()` + `isSignupEnabled()`

---

### Notification System (February 23, 2026)

Full industrial-grade notification system with backend services, REST API, and two frontend views.

- **Database:** V0_0_22 migration — `notifications` table with user_id FK (CASCADE), type (VARCHAR 30), title, message, is_read, timestamps
- **Backend (7 new files):**
  - `NotificationType` enum: INFO, SUCCESS, WARNING, ERROR, SYSTEM
  - `Notification` entity with `@JdbcTypeCode(Types.VARCHAR)` on type field
  - `NotificationRepository` with paginated queries, bulk mark-read, bulk delete
  - `NotificationResponse` DTO record with `from()` factory
  - `NotificationService` interface + `NotificationServiceImpl`
  - `NotificationHelper` — Shared `@Component` for creating notifications from any service. Uses `REQUIRES_NEW` propagation, catches exceptions silently (notifications are side-effects)
  - `NotificationController` — 6 REST endpoints under `/api/v1/notifications`
- **Frontend (6 new files, 5 modified):**
  - `notification.model.ts` — Types + interfaces
  - `notification.service.ts` — Singleton with BehaviorSubjects, 30s polling, optimistic UI
  - `NotificationsModule` with `NotificationsList` component (full page at `/notifications`)
  - Layout: header bell icon with unread badge (99+ cap), PrimeNG Drawer notification panel
  - Sidebar: "Notifications" nav item added
  - `styles.scss`: `--minted-warning` tokens + Drawer theme overrides
- **Welcome notification:** Auto-created on signup (AuthService) and admin user creation (UserManagementService)

---

### Credit Card Statement Parser (February 23, 2026)

Full credit card PDF statement parsing feature with LLM-powered transaction extraction and merchant mapping system.

- **Database (5 migrations, V0_0_23 through V0_0_27):**
  - `llm_models` table seeded with 3 Gemini models
  - `llm_configurations` table for per-user LLM settings
  - `credit_card_statements` table for 4-step parsing workflow
  - `merchant_category_mappings` table for user-defined keyword-to-category rules
  - System settings: `CREDIT_CARD_PARSER_ENABLED`, `ADMIN_LLM_KEY_SHARED`

- **Backend (25+ new files):**
  - New dependency: `org.apache.pdfbox:pdfbox:3.0.2` for PDF text extraction
  - Enum: `StatementStatus` (6 states)
  - 4 entities: `LlmModel`, `LlmConfiguration`, `CreditCardStatement`, `MerchantCategoryMapping`
  - 4 repositories with custom queries
  - 10 DTOs (records + mutable `ParsedTransactionRow`)
  - 5 service interfaces + 5 implementations:
    - `StatementParserServiceImpl` — PDFBox text extraction with password support
    - `GeminiLlmService` — Gemini API integration with structured prompt engineering and merchant hints
    - `MerchantMappingServiceImpl` — CRUD for merchant-category rules
    - `LlmConfigServiceImpl` — Config management with admin key fallback (user key → shared admin key → error)
    - `CreditCardStatementServiceImpl` — 4-step orchestrator with async LLM parsing, merchant mapping pre-pass, duplicate detection, transaction import
  - 3 controllers: `CreditCardStatementController` (6 endpoints), `LlmConfigController` (7 endpoints), `AdminLlmModelController` (4 endpoints)
  - Async pattern: `TransactionSynchronizationManager.afterCommit` + `CompletableFuture.runAsync` + `TransactionTemplate`
  - Notifications fired at each step via `NotificationHelper`

- **Frontend (20+ new files, 6 modified):**
  - Models: `statement.model.ts`, `llm-config.model.ts`
  - Services: `StatementService`, `LlmConfigService` (providedIn: 'root')
  - `StatementModule` (lazy-loaded at `/statements`) with 6 components:
    - `StatementList` — Statement history with status badges
    - `UploadStep` — PDF upload with account selection and password support
    - `StatementDetail` — Stepper host with async polling
    - `TextReviewStep` — Extracted text review with "Send to AI" action
    - `ParsePreviewStep` — AG Grid preview with duplicate highlighting, inline editing
    - `ConfirmStep` — Import summary with navigation
  - Settings: `LlmConfigComponent` (API key + model config) + `MerchantMappingsComponent` (AG Grid inline editing)
  - Admin: Feature toggle cards + LLM Models management (AG Grid with add/edit/toggle/delete)
  - Sidebar: "Statements" nav item added
  - App routing: lazy route for statement module

- **No errors encountered during implementation** — both `./gradlew build` and `ng build` passed on first attempt.

---

### Backend Modularization (February 23, 2026)

Reorganized the entire minted-api from a flat layer-based package structure into feature-based modules.

- **Before:** All 185 Java files in flat packages (`controller/`, `entity/`, `dto/`, `enums/`, `exception/`, `filter/`, `repository/`, `service/`, `service/impl/`, `util/`)
- **After:** 15 feature-based modules, each containing its own `controller/`, `dto/`, `entity/`, `enums/`, `repository/`, `service/` sub-packages

**Module breakdown (185 files total):**

| Module | Files | Contents |
|--------|-------|----------|
| `common/` | 11 | SecurityConfig, SchedulerConfig, DataInitializer, exceptions, JwtAuthFilter, JwtUtil |
| `auth/` | 9 | AuthController, login/signup DTOs, AuthService, CustomUserDetailsService |
| `user/` | 8 | User entity, UserRole enum, UserRepository, profile controller/service/DTOs |
| `account/` | 14 | Account & AccountType entities/repos/services/controllers/DTOs |
| `transaction/` | 15 | Transaction & TransactionCategory entities, TransactionType enum, repos/services/controllers/DTOs |
| `budget/` | 7 | Budget entity/repo/service/controller/DTOs |
| `dashboard/` | 10 | DashboardCard entity, CardWidth/ChartType enums, repo/service/controller/DTOs |
| `analytics/` | 8 | AnalyticsController, 5 response DTOs, AnalyticsService |
| `recurring/` | 11 | RecurringTransaction entity, frequency/status enums, RecurringTransactionJob, repo/service/controller/DTOs |
| `notification/` | 8 | Notification entity, NotificationType enum, NotificationHelper, repo/service/controller/DTO |
| `job/` | 17 | JobExecution/JobScheduleConfig/JobStepExecution entities, job enums, repos/services/DTOs |
| `bulkimport/` | 11 | BulkImport entity, ImportStatus/ImportType enums, repo/service/controller/DTOs (BulkImportJob removed) |
| `statement/` | 12 | CreditCardStatement entity, StatementStatus enum, StatementParser/CreditCardStatementService, controller/DTOs |
| `llm/` | 20 | LlmModel/LlmConfiguration/MerchantCategoryMapping entities, GeminiLlmService, repos/services/controllers/DTOs |
| `admin/` | 22 | AdminController, DefaultCategory/DefaultAccountType/SystemSetting entities, repos, DefaultListsService/SystemSettingService/UserManagementService, DTOs |

**Key decisions:**
- Service implementations placed directly in `<feature>/service/` (no separate `impl/` sub-package)
- Feature-specific jobs live in `<feature>/job/` (e.g., `recurring/job/RecurringTransactionJob.java`)
- Shared job framework entities/services remain in top-level `job/` module
- `common/` holds cross-cutting infrastructure (security, exceptions, JWT filter/util)

**Issues encountered:**
1. **Entity cross-references (69 compilation errors):** Entities that were in the same `com.minted.api.entity` package referenced each other without imports. After splitting into separate modules, explicit imports were needed (e.g., `import com.minted.api.user.entity.User;` in every entity that references User).
2. **Wildcard imports:** Files using `import com.minted.api.dto.*;` or `import com.minted.api.entity.*;` needed replacement with specific imports from the correct module packages.
3. **JPQL named query runtime failure:** A `@NamedQuery` in Transaction.java used `com.minted.api.enums.TransactionType.EXPENSE` as a string literal. Java compiler doesn't catch this — only Hibernate validates it at runtime. Had to update to `com.minted.api.transaction.enums.TransactionType.EXPENSE`.

---

### Structured Logging with MDC (February 24, 2026)

Added 3-layer structured logging infrastructure to the backend. See `docs/LOGGING.md` for full documentation.

- **Layer 1 — MDC Filter + Logback Config:**
  - `MdcFilter` (servlet filter, highest precedence) sets `requestId`, `method`, `uri`, `clientIp` in SLF4J MDC on every request
  - `JwtAuthFilter` enriched to set `MDC.put("userId")` after successful JWT validation
  - `logback-spring.xml` with dev (DEBUG/console), prod (INFO/JSON), and default profiles
  - Log pattern: `[requestId] [userId] [method uri]` in every line
- **Layer 2 — Request/Response Interceptor:**
  - `RequestLoggingInterceptor` logs `>> METHOD /uri` on entry and `<< METHOD /uri status=200 time=42ms` on completion for all `/api/**` paths
- **Layer 3 — Service-Level Business Logging:**
  - `@Slf4j` + targeted `log.info(...)` added to 17 service implementations
  - Write operations (create/update/delete) logged with entity ID and key attributes
  - Auth events: login success/failure, password change, registration
  - Read operations: no logging (noise reduction)
  - 2 services already had `@Slf4j` (`BulkImportServiceImpl`, `CreditCardStatementServiceImpl`) — enhanced by MDC context automatically

**New files (3):** `MdcFilter.java`, `RequestLoggingInterceptor.java`, `logback-spring.xml`
**Modified files (17):** `JwtAuthFilter`, `SecurityConfig`, + 15 service implementations

**No errors encountered** — `./gradlew compileJava` passed on first attempt.

---

### Mobile Responsive Design (February 25, 2026)

Added mobile-responsive layout while keeping the existing desktop design identical. All changes are behind `max-width: 767px` or Tailwind `md:` breakpoint media queries.

- **Quick Fixes:**
  - Tab title changed from "MintedWeb" to "Minted"
  - Added `<link rel="preconnect">` hints for Google Fonts (faster icon loading)
  - Added `font-display=swap` to Material Icons URLs (prevents invisible text during font load)

- **Mobile Sidebar Drawer:**
  - Desktop sidebar wrapped in `hidden md:block` — unchanged on desktop
  - PrimeNG `<p-drawer>` for mobile sidebar (position left, 280px, modal)
  - Hamburger menu button (`material-icons: menu`) in header, visible only on mobile (`md:hidden`)
  - Sidebar component: `@Input() mobileMode` forces expanded layout, hides toggle chevron
  - Sidebar component: `@Output() navigationClicked` auto-closes drawer on nav link click
  - Layout auto-closes mobile sidebar on `NavigationEnd` router events
  - Header "Minted" text replaced with hamburger button (brand text already in sidebar)

- **Global Responsive Overrides (`styles.scss`):**
  - PrimeNG dialogs capped at `90vw` on mobile (fixes 10+ dialogs at once)
  - AG Grid viewport-relative height (`60vh`, min `300px`) on mobile
  - Notification drawer width: `min(400px, 100vw)`

- **Module-Specific Fixes:**
  - Transactions page: outer padding `p-4 sm:p-8`
  - Auth login/signup: brand text `text-xl sm:text-2xl`, form padding `px-4 sm:px-8`
  - Dashboard/recurring already had responsive breakpoints — no changes needed

- **Files modified (10):** `index.html`, `styles.scss`, `layout.html`, `layout.ts`, `layout.scss`, `sidebar.html`, `sidebar.ts`, `transactions-list.html`, `login.html`, `signup.html`
- **No new dependencies** — PrimeNG Drawer already in SharedModule

---

### Bulk Import: Remove Scheduled Cron Job (February 25, 2026)

Removed the 5-minute scheduled cron sweep for bulk CSV imports. Import processing was already user-action driven via the confirm endpoint.

- **Deleted:** `bulkimport/job/BulkImportJob.java` — the `@PostConstruct`-registered cron sweep that scanned for stuck imports every 5 minutes
- **Added:** `V0_0_29__remove_bulk_import_scheduled_job.sql` — deletes the `BULK_IMPORT_PROCESSOR` row from `job_schedule_configs`
- **Unchanged:** `BulkImportServiceImpl.confirmImport()` — still triggers `processImportAsync()` immediately via `CompletableFuture.runAsync()` after transaction commit
- **Flow remains:** Upload CSV → Preview/Validate → User confirms → Async processing starts immediately → Poll for progress

---

### Import Wizard: Link Credit Card Statement Card (February 25, 2026)

Made the Credit Card Statement card in the import wizard active and clickable, navigating to the existing `/statements` route.

- **Removed:** `opacity-50`, `cursor-not-allowed`, `Coming Soon` p-tag from the statement card
- **Added:** `cursor-pointer`, `hover:shadow-md`, blue icon tint (`--minted-info`), `(click)="navigateToStatements()"`
- **Added:** `navigateToStatements()` method to `ImportWizard` component routing to `/statements`
- **Files modified (2):** `import-wizard.html`, `import-wizard.ts`

---

### Settings Bug Fixes: Soft Delete, Stale Tabs, EMI, LLM Categories (February 25, 2026)

Six fixes and enhancements across Settings, Account management, and LLM integration.

#### Bug Fix: Account Type FK Deletion Error
- **Before:** Deleting an account type linked to accounts threw a raw 500 FK constraint error
- **After:** Account type deletion is now **soft delete** (`isActive = false`), never hard delete
- Frontend shows soft-deleted types at the bottom of the list with strikethrough, reduced opacity, "Deleted" badge, and an "Undo" button to restore
- `AccountTypeServiceImpl.delete()` sets `isActive=false` instead of calling `accountTypeRepository.delete()`
- `AccountTypeService.toggleActive()` endpoint (`PATCH /{id}/toggle`) used for restore

#### Bug Fix: Account Hard Delete → Soft Delete
- `AccountServiceImpl.delete()` now sets `isActive=false` instead of hard-deleting
- `AccountServiceImpl.getAllByUserId()` changed from `findByUserId()` to `findByUserIdAndIsActiveTrue()` — UI never sees soft-deleted accounts
- `AccountServiceImpl.create()` checks for soft-deleted accounts with the same name — restores them instead of throwing `DuplicateResourceException`
- Added `findByNameAndUserIdAndIsActiveFalse()` to `AccountRepository`

#### Bug Fix: Stale Data on Settings Tab Switch
- PrimeNG Tabs keep components alive, so `ngOnInit()` doesn't re-fire on tab switch
- Added `(valueChange)="onTabChange($event)"` on `<p-tabs>` in settings template
- When switching to Accounts tab (value="1"), calls `accountsComponent.refreshData()` via `@ViewChild`
- `Accounts.refreshData()` reloads both accounts and account types

#### Bug Fix: Account Type Delete Error Message
- Frontend `deleteAccountType()` error handler now shows the API error message (`error.error.message`) instead of a generic string

#### Enhancement: "EMI" Default Category
- Added `V0_0_30__add_emi_default_category.sql` — inserts "EMI" as an EXPENSE default category
- Color mapping already existed in `TransactionCategoryServiceImpl.getDefaultColorForCategory()` (`"EMI" -> "#607D8B"`)
- Auto-provisioned to users on next category API call via existing `mergeCategories()` logic

#### Enhancement: LLM Category Hints in Prompt
- `LlmService.parseStatement()` now accepts `List<String> availableCategories` parameter
- `GeminiLlmService.buildPrompt()` includes an "AVAILABLE CATEGORIES" block instructing the LLM: "you MUST pick from this list, do NOT invent new categories"
- `CreditCardStatementServiceImpl.processLlmParseAsync()` fetches all active user categories (including auto-provisioned defaults like EMI) and passes their names to the LLM
- Merchant mapping hints remain as "ABSOLUTE RULES" (highest priority), but for unmapped merchants the LLM now picks from the user's existing category list

**Files modified (14):** `AccountTypeServiceImpl.java`, `AccountServiceImpl.java`, `AccountRepository.java`, `GeminiLlmService.java`, `LlmService.java`, `CreditCardStatementServiceImpl.java`, `account-type.model.ts`, `account-type.service.ts`, `account-types.html`, `account-types.ts`, `accounts.ts`, `settings.html`, `settings.ts`, `V0_0_30__add_emi_default_category.sql`

**No errors encountered** — both `./gradlew build -x test` and `ng build` passed on first attempt (after fixing PrimeNG `valueChange` event type).

---

## Phase: Splits Feature — Bill Splitting with Friends

**Completed:** February 25, 2026

### Deliverables

#### Database (1 file)
- `V0_0_31__create_friends_and_splits_tables.sql` — 3 tables: `friends`, `split_transactions`, `split_shares` with all FKs, indexes, and unique constraints

#### Backend: `friend` package (7 files)
- `Friend.java` entity with soft delete via `isActive`, `@ManyToOne` to User
- `FriendRepository.java` — user-scoped queries, soft-delete restore pattern
- `FriendRequest.java` / `FriendResponse.java` — Java record DTOs with validation
- `FriendService.java` / `FriendServiceImpl.java` — CRUD with duplicate name check, soft-delete restore
- `FriendController.java` — REST at `/api/v1/friends`

#### Backend: `split` package (15 files)
- `SplitType.java` enum (EQUAL, UNEQUAL, SHARE)
- `SplitTransaction.java` — `@OneToMany` cascade with orphanRemoval, `@JdbcTypeCode(Types.VARCHAR)` on enum
- `SplitShare.java` — `@ManyToOne` to SplitTransaction and Friend (nullable for "Me")
- `SplitTransactionRepository.java` — JPQL for sumOwedToUser/sumUserOwes
- `SplitShareRepository.java` — JPQL for unsettled balances per friend, group-by aggregation
- 7 DTO records: SplitShareRequest/Response, SplitTransactionRequest/Response, SplitBalanceSummaryResponse, FriendBalanceResponse, SettleRequest
- `SplitServiceImpl.java` — EQUAL auto-division with rounding, settle with notification via NotificationHelper
- `SplitTransactionController.java` — 9 endpoints at `/api/v1/splits`

#### Frontend: Models & Services (4 files)
- `friend.model.ts` / `split.model.ts` — TypeScript interfaces
- `friend.service.ts` / `split.service.ts` — HTTP services with client-side CSV export

#### Frontend: Splits Module (7 files)
- `SplitsModule` — lazy-loaded NgModule with AgGridModule, MessageService, ConfirmationService
- `SplitsPage` — main component with friends card, balance summary cards, pending settlements, AG Grid table
- 3 PrimeNG dialogs: Add/Edit Friend, Split Transaction (split type selector + friend management), Settlement Review
- `SplitFriendsCellRendererComponent` — avatar circles for "Split With" AG Grid column
- `SplitActionsCellRendererComponent` — Edit/Delete buttons for AG Grid

#### Frontend: Integration (4 existing files modified)
- `app-routing-module.ts` — added `splits` lazy route
- `sidebar.ts` — added "Splits" nav item (icon: `call_split`)
- `actions-cell-renderer.component.ts` — added "Split" button (pi-users) + `onSplit` callback
- `transactions-list.ts` — added `splitTransaction()` navigating to `/splits` with query params

### Issues Encountered
- **Material Icons**: Used `material-symbols-outlined` from Stitch prototypes instead of `material-icons` loaded in app. Fixed by replacing all icon class references.
- **Dark Theme**: Used hardcoded Tailwind color classes (`bg-white`, `text-slate-900`) instead of CSS variable inline styles. Fixed by using `style="background-color: var(--minted-bg-card);"` pattern matching analytics-overview.

**Files created (33 new):** V0_0_31 migration, 7 friend package files, 15 split package files, 4 frontend models/services, 7 splits module files
**Files modified (4):** app-routing-module.ts, sidebar.ts, actions-cell-renderer.component.ts, transactions-list.ts

---

### Splits: Transaction Integration & Inline Split Dialog (February 25, 2026)

Enhanced the transactions list with an `isSplit` indicator and an inline split dialog, replacing the previous route-based navigation to `/splits`.

#### Backend Changes (3 files modified)

- **`SplitTransactionRepository`**: Added `findSourceTransactionIdsByUserId()` JPQL query — returns all source transaction IDs that have been split
- **`TransactionResponse`**: Added `isSplit: Boolean` field to DTO record; updated `from()` factory to accept `boolean isSplit` parameter
- **`TransactionServiceImpl`**: All read methods (`getAllByUserId`, `getAllByUserIdAndDateRange`, `getAllByFilters`, `getById`) and `update()` now query split IDs via `Set<Long>` lookup and pass `isSplit` flag to each `TransactionResponse.from()` call; `create()` returns `isSplit = false`

#### Frontend Changes (4 files modified)

- **`transaction.model.ts`**: Added `isSplit: boolean` field to `TransactionResponse` interface
- **`actions-cell-renderer.component.ts`**:
  - Added `onSplit` optional callback to `ActionsCallbacks` interface
  - Split button shows `split-active` CSS class (accent border + background) when `params.data.isSplit === true`
  - Dynamic tooltip: "Already split" vs "Split"
- **`transactions-list.ts`**:
  - Imports `FriendService`, `SplitService`, split/friend models
  - `SplitFriendEntry` interface for tracking per-friend share state
  - `initSplitForm()`: Reactive form with description, categoryName, totalAmount, transactionDate, sourceTransactionId
  - `loadFriends()`: Loads friends list on init (parallel with transactions/categories/accounts)
  - `splitTransaction(transaction)`: Opens modal pre-filled with transaction data, initializes "Me" as payer
  - 3 split type calculations:
    - **EQUAL**: Auto-divides total with `Math.floor` rounding, remainder to first entry
    - **UNEQUAL**: Manual amount entry per friend (no auto-recalculation)
    - **SHARE**: Percentage-based with `onSharePercentageChange()` converting % to amounts
  - `saveSplit()`: Validates form + minimum 2 participants + split total matches transaction total, then calls `SplitService.create()`
  - `onSplitDialogHide()`: Resets form, entries, and available friends on close
  - Helper methods: `getInitials()`, `getSplitTotal()`, `recalculateShares()`, `updateAvailableFriends()`
  - Removed `Router` dependency (no longer navigating to `/splits`)
- **`transactions-list.html`**:
  - 640px modal dialog with split form: description, category, amount, date fields
  - 3-button split type selector using PrimeIcons (`pi-equals`, `pi-chart-pie`, `pi-percentage`)
  - Friend list with color avatars, initials, editable share amounts
  - SHARE mode: percentage input with live amount calculation
  - Dashed-border "Add friend" section with friend chips
  - Summary box showing split total vs. transaction total
  - Cancel and "Add to Splits" submit button with `pi-sitemap` icon

### Issues Encountered
- **Material Icons in split dialog**: Template initially used `<span class="material-icons">` (balance, pie_chart, percent, close, call_split) from Stitch prototypes. Replaced with PrimeIcons (`pi-equals`, `pi-chart-pie`, `pi-percentage`, `pi-times`, `pi-sitemap`). Project only permits PrimeNG/FortAwesome icons.
- **Missing `>` on `<p-dialog>` tag**: Adding `(onHide)` attribute left the tag unterminated, causing Angular build error `NG5002: Opening tag "p-dialog" not terminated`. Fixed by adding closing `>`.
- **UNEQUAL/SHARE split calculations missing**: Only EQUAL type had recalculation logic. Added percentage-based calculation for SHARE and manual entry for UNEQUAL.

**Files modified (7):** SplitTransactionRepository.java, TransactionResponse.java, TransactionServiceImpl.java, transaction.model.ts, actions-cell-renderer.component.ts, transactions-list.ts, transactions-list.html

---

### Icon Standardization: PrimeNG Icons Only (February 25, 2026)

Migrated the entire app from 3 icon libraries (Google Material Icons ~200+ usages, Font Awesome 1 usage) to PrimeNG Icons (`pi pi-*`) exclusively. Reduces visual inconsistency and eliminates unnecessary CSS/font loads.

#### Backend Changes

- **`V0_0_32__update_icons_to_primeng.sql`** — Converts `fa-*` prefixed icons and short-form names (`bank`, `credit-card`, `wallet`, `chart`) to `pi pi-*` format in `account_types`, `default_categories`, and `transaction_categories`
- **`V0_0_33__fix_category_icons.sql`** — Updates Food & Dining, Groceries, and EMI to better-matching PrimeNG icons
- **`V0_0_34__fix_bare_material_icons.sql`** — Converts bare Material icon names (`restaurant`, `shopping_bag`, `directions_car`, etc.) that the settings UI had been saving to the DB; fixes invalid `pi pi-shop` from V0_0_33; catch-all sets any remaining non-`pi pi-*` icons to `pi pi-tag`
- **`DataInitializer.java`** — `getDefaultIconForAccountType()` now returns `pi pi-*` format

#### Frontend: Layout (4 files)
- **Sidebar** (`sidebar.ts`, `sidebar.html`): All nav icons changed from Material names to `pi pi-*` classes. Logo, toggle chevrons, user menu icons all converted. Template changed from `<span class="material-icons">{{ item.icon }}</span>` to `<i [class]="item.icon">`.
- **Header** (`layout.html`, `layout.ts`): Hamburger, theme toggle, notification bell, drawer icons all converted. `getNotificationIcon()` returns PrimeNG classes.

#### Frontend: Feature Modules (20+ files)
- **Dashboard**: KPI card icons (`trending_up` → `pi pi-arrow-up`, etc.)
- **Recurring Transactions**: All action/status/type icons + category `iconMap`
- **Splits**: 11 Material icon instances replaced
- **Import**: Wizard step icons, job list/detail back arrows
- **Admin**: User management, server settings, job detail icons
- **Notifications**: Type icons, action buttons
- **Settings Profile**: Theme toggle, accent check icons
- **Statement**: `fa fa-tag` → `pi pi-tag` in AG Grid cell renderer

#### Frontend: Category Icon System Fix (3 files)
- **`categories.ts`** (Settings): Root cause fix — `iconOptions` array was storing Material icon names (`restaurant`, `shopping_bag`) as `value` (saved to DB) while displaying PrimeNG icons visually. Changed all `value` fields to `pi pi-*` format. Expanded from 12 to 24 icon options. Updated form defaults.
- **`category-cell-renderer.component.ts`** (Transactions AG Grid): Added `getIconClass()` method with legacy Material→PrimeNG fallback map for old DB values. Ensures icons render even before migration runs.
- **`categories.ts` `getIconClass()`**: Simplified — checks `pi pi-` prefix first, falls back to Material mapping for legacy values.

#### Cleanup (3 files)
- **`index.html`**: Removed Google Material Icons and Material Icons Outlined `<link>` tags
- **`styles.scss`**: Removed `@import '@fortawesome/fontawesome-free/css/all.min.css'`
- **`package.json`**: Uninstalled `@fortawesome/fontawesome-free` dependency

#### Icon Mapping Reference

| Old (Material/FA) | New (PrimeNG) |
|---|---|
| `dashboard` | `pi pi-th-large` |
| `receipt_long` | `pi pi-list` |
| `sync_alt` | `pi pi-sync` |
| `upload_file` | `pi pi-upload` |
| `description` | `pi pi-file` |
| `call_split` | `pi pi-sitemap` |
| `pie_chart` | `pi pi-chart-pie` |
| `notifications` | `pi pi-bell` |
| `settings` | `pi pi-cog` |
| `trending_up/down` | `pi pi-arrow-up/down` |
| `restaurant` | `pi pi-shopping-cart` |
| `shopping_bag` | `pi pi-shopping-bag` |
| `fa-utensils` | `pi pi-shopping-cart` |
| `fa-graduation-cap` | `pi pi-book` |
| `fa fa-tag` | `pi pi-tag` |

#### Issues Encountered
- **`pi pi-shop` is not a valid PrimeNG icon**: Used in V0_0_33 for Food & Dining. Fixed in V0_0_34 by converting to `pi pi-shopping-cart`.
- **Categories settings UI saving Material icon names to DB**: The `iconOptions` array stored Material names as `value` (e.g., `restaurant`) while showing PrimeNG icons in the dropdown. Users creating/editing categories got Material names saved. Fixed by changing `value` to `pi pi-*` format and adding V0_0_34 migration for existing data.
- **Bare Material icon names not caught by V0_0_32**: V0_0_32 only converted `fa-*` prefixed icons. Bare names like `restaurant`, `school` from the settings UI were missed. Fixed in V0_0_34 with explicit mapping + catch-all.
- **Never modify applied Flyway migrations**: V0_0_33 was already applied to the database. Created V0_0_34 as a new migration instead of modifying V0_0_33.

**Files created (3):** V0_0_32, V0_0_33, V0_0_34 migrations
**Files modified (30+):** DataInitializer.java, sidebar.ts/html, layout.html/ts, home.html/scss, recurring-list.html/ts/scss, splits-page.html, import-jobs/wizard/job-detail HTML, admin user-management/server-settings/job-detail HTML, jobs-list.ts, import-jobs.ts, notifications-list.html/ts, profile.html, parse-preview-step.ts, category-cell-renderer.component.ts, categories.ts, transactions-list.scss, index.html, styles.scss, package.json

---

### Bug Fix: Account Dialog Currency Dropdown (February 26, 2026)

Fixed the account creation/edit dialog to use a proper currency dropdown instead of a free-text input, and made the balance field dynamically adapt to the selected currency.

- **`accounts.html`**: Changed currency field from `<input pInputText>` to `<p-select>` dropdown using `CurrencyService.currencies` array. Moved currency field above balance field so user selects currency first. Changed balance `<p-inputnumber>` from hardcoded `currency="USD" locale="en-US"` to dynamic `[currency]` and `[locale]` bindings.
- **`accounts.ts`**: Changed `private currencyService` to `public currencyService` for template access. Added `getLocaleForCurrency(code)` helper method that looks up the locale from CurrencyService.currencies.
- **Files modified (2):** `accounts.html`, `accounts.ts`

---

### Transactions Page Enhancements (February 26, 2026)

Added bulk operations, extended date filters, and default sort to the transactions page.

#### Backend Changes (3 files)
- **`TransactionService.java`**: Added `bulkDelete(List<Long> ids, Long userId)` and `bulkUpdateCategory(List<Long> ids, Long categoryId, Long userId)` interface methods
- **`TransactionServiceImpl.java`**: Implemented bulk delete (iterates, reverses account balances, deletes) and bulk category update (validates category, updates each transaction)
- **`TransactionController.java`**: Added `DELETE /bulk` endpoint (accepts `{ "ids": [...] }`) and `PUT /bulk/category` endpoint (accepts `{ "ids": [...], "categoryId": N }`)

#### Frontend Changes (4 files)
- **`transaction.model.ts`**: Added `LAST_6_MONTHS` and `LAST_YEAR` to `DateFilterOption` enum
- **`transaction.service.ts`**: Added `bulkDelete(ids)` and `bulkUpdateCategory(ids, categoryId)` HTTP methods
- **`transactions-list.ts`**: Added `selectedTransactions`, `showBulkCategoryDialog`, `bulkCategoryId` state. Added `sort: 'desc'` to transactionDate column. Added LAST_6_MONTHS/LAST_YEAR cases in `getDateRange()`. Added `onSelectionChanged()`, `bulkDelete()`, `openBulkCategoryDialog()`, `saveBulkCategory()` methods. Added excludeFromAnalysis to form/dialog/save.
- **`transactions-list.html`**: Added bulk action buttons (Delete Selected, Change Category) with selection count badge. Added Last 6 Months and Last Year date filter buttons. Added `(selectionChanged)` event on ag-grid. Added "Exclude from Analysis" checkbox in transaction dialog. Added Bulk Category Dialog.

**Files created (0), modified (7):** TransactionService.java, TransactionServiceImpl.java, TransactionController.java, transaction.model.ts, transaction.service.ts, transactions-list.ts, transactions-list.html

---

### Exclude from Analysis & Dashboard Configuration (February 26, 2026)

Added an "Exclude from Analysis" flag on transactions and a Dashboard Configuration settings tab for excluding categories from analytics.

#### Database (2 migrations)
- **`V0_0_35__add_exclude_from_analysis.sql`**: `ALTER TABLE transactions ADD COLUMN exclude_from_analysis BOOLEAN DEFAULT FALSE;`
- **`V0_0_36__create_dashboard_config.sql`**: Creates `dashboard_configurations` table with `user_id` (UNIQUE FK), `excluded_category_ids` (TEXT, comma-separated), timestamps

#### Backend: Transaction Changes (4 files)
- **`Transaction.java`**: Added `excludeFromAnalysis` field (Boolean, default false). Updated ALL 5 analytics `@NamedQuery` to add `AND (t.excludeFromAnalysis = false OR t.excludeFromAnalysis IS NULL)` filter
- **`TransactionRequest.java`**: Added `Boolean excludeFromAnalysis` field
- **`TransactionResponse.java`**: Added `Boolean excludeFromAnalysis` field, updated `from()` method
- **`TransactionServiceImpl.java`**: Updated `create()` and `update()` to handle excludeFromAnalysis

#### Backend: Dashboard Config (NEW package — 7 files)
- **`dashboardconfig/entity/DashboardConfiguration.java`**: JPA entity with `@OneToOne` User relationship, `excludedCategoryIds` TEXT field
- **`dashboardconfig/dto/DashboardConfigRequest.java`**: Record with `List<Long> excludedCategoryIds`
- **`dashboardconfig/dto/DashboardConfigResponse.java`**: Record with `from()` static method parsing comma-separated IDs
- **`dashboardconfig/repository/DashboardConfigurationRepository.java`**: JPA repo with `findByUserId()`
- **`dashboardconfig/service/DashboardConfigService.java`**: Interface with `getConfig`, `saveConfig`, `getExcludedCategoryIds`
- **`dashboardconfig/service/DashboardConfigServiceImpl.java`**: Implementation storing comma-separated category IDs
- **`dashboardconfig/controller/DashboardConfigController.java`**: `GET` and `PUT` endpoints at `/api/v1/dashboard-config`

#### Frontend (6 files: 4 new, 2 modified)
- **`dashboard-config.model.ts`** (NEW): TypeScript interfaces
- **`dashboard-config.service.ts`** (NEW): HTTP service (`providedIn: 'root'`)
- **`dashboard-config/dashboard-config.ts`** (NEW): Component with category multiselect and save
- **`dashboard-config/dashboard-config.html`** (NEW): PrimeNG multiselect for category exclusion, save button, info card
- **`settings-module.ts`**: Added DashboardConfigComponent import/declaration
- **`settings.html`**: Added "Dashboard" tab (value="6") with `pi-objects-column` icon

**Files created (6 + 2 migrations):** V0_0_35, V0_0_36, DashboardConfiguration.java, DashboardConfigRequest.java, DashboardConfigResponse.java, DashboardConfigurationRepository.java, DashboardConfigService.java, DashboardConfigServiceImpl.java, DashboardConfigController.java, dashboard-config.model.ts, dashboard-config.service.ts, dashboard-config.ts, dashboard-config.html
**Files modified (6):** Transaction.java, TransactionRequest.java, TransactionResponse.java, TransactionServiceImpl.java, settings-module.ts, settings.html

---

### Financial Statements Module Enhancements (February 26, 2026)

Enhanced the statement parser to support CSV/TXT files (not just PDF), added editable text review, new SENT_FOR_AI_PARSING status, category dropdown in preview, and renamed module to "Financial Statements".

#### Database (1 migration)
- **`V0_0_37__add_file_type_to_statements.sql`**: `ALTER TABLE credit_card_statements ADD COLUMN file_type VARCHAR(10) DEFAULT 'PDF';`

#### Backend Changes (6 files)
- **`StatementStatus.java`**: Added `SENT_FOR_AI_PARSING` enum value between `TEXT_EXTRACTED` and `LLM_PARSED`
- **`CreditCardStatement.java`**: Added `fileType` field (String, default "PDF")
- **`StatementResponse.java`**: Added `fileType` field. Added logic to suppress `extractedText` when status is `SENT_FOR_AI_PARSING`, `LLM_PARSED`, `CONFIRMING`, or `COMPLETED` (reduces API payload)
- **`CreditCardStatementController.java`**: Updated parse endpoint to accept optional `@RequestBody Map<String, String> body` with `extractedText` key
- **`CreditCardStatementService.java`**: Updated `triggerLlmParse` signature to accept `String editedText` parameter
- **`CreditCardStatementServiceImpl.java`**:
  - `uploadAndExtract()`: Detects file type from extension/content type. For CSV/TXT: reads file bytes as UTF-8 text directly (no PDFBox). For PDF: delegates to existing `StatementParserService.extractText()`. Sets `fileType` on entity. Max 5MB for CSV/TXT, 20MB for PDF.
  - `triggerLlmParse()`: Accepts `editedText` param — overwrites stored text if provided. Sets `SENT_FOR_AI_PARSING` status before starting async processing.
  - Added `detectFileType()` private helper.

#### Frontend Changes (10 files)
- **`statement.model.ts`**: Added `SENT_FOR_AI_PARSING` to `StatementStatus` type. Added `fileType: string` to `CreditCardStatement` interface.
- **`statement.service.ts`**: Updated `triggerParse()` to accept optional `editedText` parameter, sends as `{ extractedText }` in request body.
- **`upload-step.ts`**: Added `selectedFileType` state (`'PDF' | 'CSV' | 'TXT'`), `fileTypes` array with icon/accept/description per type, `onFileTypeSelect()` method, `currentFileTypeConfig` getter. Updated `onFileSelect()` validation for dynamic max size.
- **`upload-step.html`**: Added 3 file type selection cards (PDF/CSV/TXT) with visual active state. Dynamic file input accept attribute. Password section shown only for PDF. Updated header text to "Financial Statements".
- **`text-review-step.ts`**: Implemented `OnChanges`. Added `editedText`, `textModified` state. Added `onTextChange()` and `ngOnChanges()`. Updated `triggerParse()` to send edited text only if modified.
- **`text-review-step.html`**: Changed textarea from `readonly` with `[value]` to editable with `[ngModel]`/`(ngModelChange)`. Added "Modified" badge. Added info text about editing.
- **`statement-detail.ts`**: Updated polling condition to continue during `SENT_FOR_AI_PARSING` status.
- **`statement-list.ts`**: Added `SENT_FOR_AI_PARSING` to `getStatusSeverity()` (warn) and `getStatusLabel()` ("AI Parsing...").
- **`statement-list.html`**: Renamed "Credit Card Statements" to "Financial Statements". Updated empty state text.
- **`parse-preview-step.ts`**: Injected `CategoryService`. Added `categories` and `categoryNames` arrays. Added `loadCategories()` in `ngOnInit()`. Updated categoryName column to use `agSelectCellEditor` with dynamic `cellEditorParams` from loaded categories. Added `onCellValueChanged` to update `matchedCategoryId` when category is selected.

#### Import Module (1 file modified)
- **`import-wizard.html`**: Renamed "Credit Card Statement" card to "Financial Statement"

#### Shared Module Fix (1 file modified)
- **`shared.module.ts`**: Added `MultiSelectModule` import/export (required by dashboard-config component)

**Files created (1 migration):** V0_0_37
**Files modified (17):** StatementStatus.java, CreditCardStatement.java, StatementResponse.java, CreditCardStatementController.java, CreditCardStatementService.java, CreditCardStatementServiceImpl.java, statement.model.ts, statement.service.ts, upload-step.ts, upload-step.html, text-review-step.ts, text-review-step.html, statement-detail.ts, statement-list.ts, statement-list.html, parse-preview-step.ts, import-wizard.html, shared.module.ts

**No errors encountered during backend build.** Frontend had one build error (MultiSelectModule not imported) — fixed immediately.

---

### Avatar Upload Feature (February 27, 2026)

Added avatar image upload with crop support for both user profiles and friends. Avatars are stored as LONGBLOB in the database (max 2MB) and served as base64 data URIs.

#### Database (2 migrations)
- **`V0_0_38__add_avatar_to_users.sql`**: Adds `avatar_data` (LONGBLOB), `avatar_content_type` (VARCHAR 50), `avatar_file_size` (INT), `avatar_updated_at` (TIMESTAMP) to `users` table
- **`V0_0_39__add_avatar_to_friends.sql`**: Same 4 columns added to `friends` table

#### Backend Changes (11 files)
- **`User.java`**: Added `avatarData` (byte[], @Lob LONGBLOB), `avatarContentType`, `avatarFileSize`, `avatarUpdatedAt` fields
- **`UserResponse.java`**: Added `avatarBase64` field (data URI string)
- **`UserProfileService.java`**: Added `uploadAvatar(username, MultipartFile)` and `deleteAvatar(username)` interface methods
- **`UserProfileServiceImpl.java`**: Implemented avatar upload (2MB max, image/* validation, base64 encoding in `toResponse()`) and delete (nullifies all avatar fields)
- **`UserProfileController.java`**: Added `POST /api/v1/profile/avatar` (multipart) and `DELETE /api/v1/profile/avatar` endpoints
- **`AuthServiceImpl.java`**: Updated 3 `UserResponse` constructor calls to include `null` for avatarBase64 (auth flow doesn't return avatar data)
- **`Friend.java`**: Added same 4 avatar fields as User entity
- **`FriendResponse.java`**: Added `avatarBase64` field with base64 encoding in `from()` method
- **`FriendService.java`**: Added `uploadAvatar(id, userId, MultipartFile)` and `deleteAvatar(id, userId)` interface methods
- **`FriendServiceImpl.java`**: Implemented friend avatar upload/delete with same validation (2MB, image/*)
- **`FriendController.java`**: Added `POST /api/v1/friends/{id}/avatar` and `DELETE /api/v1/friends/{id}/avatar` endpoints

#### Frontend: Shared Component (3 new files)
- **`shared/components/avatar-upload/avatar-upload.component.ts`**: Reusable `AvatarUploadComponent` (non-standalone) with inputs: `currentAvatarUrl`, `initials`, `color`, `label`, `size` (sm/md/lg). Outputs: `avatarSelected` (File), `avatarRemoved`. Uses `ngx-image-cropper` for 1:1 aspect ratio crop, 512px output, JPEG format. Includes file validation (image/*, 2MB max).
- **`avatar-upload.component.html`**: Avatar ring with hover camera overlay, remove button, crop dialog with preview
- **`avatar-upload.component.scss`**: Styled avatar ring (3px accent border, hover glow), crop dialog layout, preview row

#### Frontend: Integration (8 files modified)
- **`package.json`**: Added `ngx-image-cropper` dependency
- **`shared.module.ts`**: Imported `ImageCropperComponent`, declared and exported `AvatarUploadComponent`
- **`profile.service.ts`**: Added `uploadAvatar(file)` and `deleteAvatar()` HTTP methods
- **`profile.ts`**: Added `onAvatarSelected(file)`, `onAvatarRemoved()`, `getInitials()`. Avatar stored in localStorage for sidebar access.
- **`profile.html`**: Replaced static camera icon with `<app-avatar-upload>` component
- **`friend.service.ts`**: Added `uploadAvatar(id, file)` and `deleteAvatar(id)` HTTP methods
- **`friend.model.ts`**: Added `avatarBase64: string | null` to `FriendResponse`
- **`splits-page.ts`**: Added `onFriendAvatarSelected()`, `onFriendAvatarRemoved()`, `getFriend()` helper. Avatar upload in edit friend dialog (edit mode only).
- **`splits-page.html`**: Updated all avatar circles (friend ring, balance cards, split shares, settle dialog, available friends pills) to show `<img>` when `avatarBase64` exists, fallback to initials. Added `<app-avatar-upload>` in edit friend dialog.
- **`sidebar.html`**: Shows user avatar image when available, falls back to initials
- **`sidebar.ts`**: Added `userAvatar` getter reading from localStorage

**Files created (5 + 2 migrations):** V0_0_38, V0_0_39, avatar-upload.component.ts, avatar-upload.component.html, avatar-upload.component.scss
**Files modified (17):** User.java, UserResponse.java, UserProfileService.java, UserProfileServiceImpl.java, UserProfileController.java, AuthServiceImpl.java, Friend.java, FriendResponse.java, FriendService.java, FriendServiceImpl.java, FriendController.java, shared.module.ts, package.json, profile.service.ts, profile.ts, profile.html, friend.service.ts, friend.model.ts, splits-page.ts, splits-page.html, sidebar.html, sidebar.ts

---

## Current Status

All core features are implemented. See root `IMPLEMENTATION_STATUS.md` for details.
Remaining work: Configurable dashboard cards, budget tracking polish.
