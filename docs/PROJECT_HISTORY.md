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
  - Job: `BulkImportJob` (sweep for stuck imports) + async 4-step processing via `TransactionTemplate`
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
| `bulkimport/` | 12 | BulkImport entity, ImportStatus/ImportType enums, BulkImportJob, repo/service/controller/DTOs |
| `statement/` | 12 | CreditCardStatement entity, StatementStatus enum, StatementParser/CreditCardStatementService, controller/DTOs |
| `llm/` | 20 | LlmModel/LlmConfiguration/MerchantCategoryMapping entities, GeminiLlmService, repos/services/controllers/DTOs |
| `admin/` | 22 | AdminController, DefaultCategory/DefaultAccountType/SystemSetting entities, repos, DefaultListsService/SystemSettingService/UserManagementService, DTOs |

**Key decisions:**
- Service implementations placed directly in `<feature>/service/` (no separate `impl/` sub-package)
- Feature-specific jobs live in `<feature>/job/` (e.g., `bulkimport/job/BulkImportJob.java`)
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

## Current Status

All core features are implemented. See root `IMPLEMENTATION_STATUS.md` for details.
Remaining work: Configurable dashboard cards, budget tracking polish, mobile responsiveness.
