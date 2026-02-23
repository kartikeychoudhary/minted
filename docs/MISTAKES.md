# MISTAKES.md — Learning From Errors

> **Rule:** Read this entire file at the start of every new task.
> **Rule:** Add entries after every error, retry, or unexpected behavior.
> **Rule:** If a mistake repeats, move its prevention rule to the HIGH PRIORITY section.

---

## 🔴 HIGH PRIORITY (Repeated Mistakes)

<!-- When a mistake from below occurs more than once, move its prevention rule here in bold -->

1. **NEVER generate Angular standalone components. Always use `--standalone=false` flag. Always verify generated component has `standalone: false` or no standalone property in `@Component` decorator.**
2. **NEVER add libraries outside the approved tech stack (PrimeNG, AG Grid, Tailwind, FortAwesome). No Material, No Bootstrap, No NgRx.**
3. **NEVER use `spring.jpa.hibernate.ddl-auto=create` or `update`. Always use `validate`. All schema changes go through Flyway migrations.**
4. **NEVER expose JPA Entity objects directly in REST responses. Always use DTOs.**
5. **NEVER hardcode secrets, passwords, or connection strings. Always use environment variables.**

---

## Build Errors

| Date | Error | Root Cause | Fix | Prevention |
|------|-------|------------|-----|------------|
| 2026-02-16 | Tailwind CSS v4 PostCSS plugin error | Installed Tailwind v4 which requires `@tailwindcss/postcss` plugin | Downgraded to Tailwind CSS v3 and used standard `tailwindcss` PostCSS plugin | Always use Tailwind CSS v3.x for Angular projects until v4 is fully compatible. Check package.json to verify version. |

---

## Runtime Errors

| Date | Error | Root Cause | Fix | Prevention |
|------|-------|------------|-----|------------|
| _template_ | _describe error_ | _why it happened_ | _what fixed it_ | _how to prevent_ |

---

## Angular Errors

| Date | Error | Root Cause | Fix | Prevention |
|------|-------|------------|-----|------------|
| 2026-02-16 | "Could not resolve @angular/animations/browser" build error | @angular/animations package not installed. PrimeNG requires Angular animations but it wasn't in package.json | Installed @angular/animations: `npm install @angular/animations` | Always install @angular/animations when using PrimeNG. It's a required peer dependency. Check package.json after Angular project creation. |
| 2026-02-16 | "Cannot find module 'primeng/sidebar'" | Attempted to import non-existent PrimeNG module. Not all PrimeNG v17+ modules follow the same pattern | Removed SidebarModule import from SharedModule | Always check PrimeNG documentation for correct module imports. PrimeNG v17+ reorganized some modules. Verify imports before using. |
| 2026-02-16 | "NG01052: formGroup expects a FormGroup instance" runtime error | FormGroup declared with `!` operator but initialized in ngOnInit(). Template rendered before initialization completed | Changed to optional `loginForm?: FormGroup`, added `*ngIf="loginForm"` to template, added null checks | When using reactive forms, either initialize in constructor OR make the FormGroup optional and use `*ngIf` in template. Never use `!` operator with forms initialized in lifecycle hooks. |
| 2026-02-17 | "NG0201: No provider for MessageService" in TransactionsModule | PrimeNG services (MessageService, ConfirmationService) injected in component but not provided at module level | Added `providers: [MessageService, ConfirmationService]` to TransactionsModule's `@NgModule` decorator | **ALWAYS provide PrimeNG services (MessageService, ConfirmationService) in the module's providers array when components inject them. These are NOT provided in root by default.** Every feature module that uses PrimeNG toasts or confirm dialogs needs these providers. |
| 2026-02-17 | "NG8001: 'p-accordionTab' is not a known element" build error | Used old PrimeNG v17 accordion syntax (`<p-accordionTab>`) instead of PrimeNG v18 syntax | Changed to use `<p-accordion-panel>`, `<p-accordion-header>`, and `<p-accordion-content>` components | **PrimeNG v18 changed accordion syntax. Use `<p-accordion-panel>` NOT `<p-accordionTab>`. Header and content now use separate components: `<p-accordion-header>` and `<p-accordion-content>`. Use `[value]` property on accordion to set initial open panels (array of strings for multiple mode). Always check PrimeNG v18 documentation for component syntax changes.** |
| 2026-02-18 | Custom date pickers not appearing on first click in transactions page | Change detection not triggered when `showCustomDatePickers` was set to true. Angular didn't re-render the template immediately to show the `*ngIf="showCustomDatePickers"` section | Added `this.cdr.detectChanges()` call after setting `showCustomDatePickers = true` in `onDateFilterChange()` method | **When dynamically showing/hiding UI elements with property changes, explicitly call `ChangeDetectorRef.detectChanges()` if the change doesn't trigger automatically. This is especially important for conditional rendering with `*ngIf` that needs immediate visual feedback. Always inject ChangeDetectorRef and use it when toggling UI visibility.** |
| 2026-02-18 | AG Grid not visible when no transactions exist | AG Grid was rendering but showing blank screen when rowData was empty. Missing overlay template configuration for empty state | Added `overlayNoRowsTemplate` to gridOptions: `overlayNoRowsTemplate: '<span class="ag-overlay-no-rows-center">No transactions found. Add a transaction to get started.</span>'` | **Always configure `overlayNoRowsTemplate` in AG Grid gridOptions to provide user feedback when no data exists. AG Grid shows a default message but custom templates provide better UX. The grid will still render headers and structure even with empty rowData array.** |
| 2026-02-18 | "AG Grid: error #272 No AG Grid modules are registered!" runtime error | AG Grid v35+ requires explicit module registration. Breaking change from v34 where modules were auto-registered. Without registration, grid fails to initialize and throws "Cannot read properties of undefined (reading 'dispatchEvent')" errors | Added module registration in main.ts BEFORE bootstrapping: `import { ModuleRegistry, AllCommunityModule } from 'ag-grid-community'; ModuleRegistry.registerModules([AllCommunityModule]);`. Also increased initial bundle budget from 2MB to 3MB as AG Grid modules (~1MB) are now in initial bundle | **CRITICAL: AG Grid v35+ requires ModuleRegistry.registerModules([AllCommunityModule]) to be called before grid initialization. Always register in main.ts before bootstrapModule() for global availability. This adds ~1MB to initial bundle size - adjust angular.json budgets accordingly (initial: 3MB/4MB recommended). Check AG Grid migration guide when upgrading major versions.** |
| 2026-02-19 | Angular lazy loaded module routing returns blank page or routes don't exist | `admin-module.ts` imported an empty generated file `admin-routing-module.ts` instead of the manually configured `admin-routing.module.ts` | Corrected the import statement to point to `admin-routing.module.ts` | **ALWAYS verify the exact filename when importing routing modules in Angular. IDE auto-imports might link to empty/scaffolded files instead of your configured route module, causing silent routing failures.** |
| 2026-02-20 | Data not appearing on screen / screen stuck spinning "loading" after API call success | Angular's default `OnPush` change detection strategy does not automatically update views after asynchronous callbacks (like HTTP subscriptions) finish processing | Injected `ChangeDetectorRef` into the component and called `this.cdr.detectChanges();` securely inside the `.subscribe({ next: () => {...}, error: () => {...} })` blocks | **CRITICAL: When using `OnPush` change detection, you MUST explicitly call `ChangeDetectorRef.detectChanges()` inside any asynchronous callback (Promises, `setTimeout`, `RxJS` Observables) that modifies UI-bound properties (like `loading = false` or `data = res`). Otherwise, the UI will appear frozen or stuck loading until the user clicks somewhere on the screen.** |
| 2026-02-20 | `NullPointerException: Cannot invoke "JobExecution.getSteps()" because "jobExecution" is null` in async import processing | **Two issues:** (1) `processImportAsync()` self-invocation bypassed `@Transactional`; (2) `CompletableFuture.runAsync()` fired BEFORE the parent `@Transactional` method committed, so `job_execution_id` was still NULL in the database when the async thread loaded the entity | (1) Wrapped `processImportAsync()` body in `TransactionTemplate.executeWithoutResult(...)` for a programmatic transaction. (2) Used `TransactionSynchronizationManager.registerSynchronization(afterCommit)` to defer the `CompletableFuture.runAsync()` until after the parent transaction commits | **CRITICAL: Two rules for async processing from @Transactional methods: (A) `@Transactional` does NOT work on self-invocations — use `TransactionTemplate` programmatically. (B) NEVER fire `CompletableFuture.runAsync()` inside a `@Transactional` method if the async task reads data written in that transaction — the transaction hasn't committed yet! Use `TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() { afterCommit() { ... } })` to defer async work until after commit.** |

---

## Spring Boot Errors

| Date | Error | Root Cause | Fix | Prevention |
|------|-------|------------|-----|------------|
| 2026-02-16 | JWT `parserBuilder()` method not found | jjwt 0.12.x has different API than 0.11.x | Updated to use `Jwts.parser().verifyWith().build().parseSignedClaims()` instead of `parserBuilder()`, and `Jwts.SIG.HS256` instead of `SignatureAlgorithm.HS256` | Always check jjwt documentation for version-specific API. jjwt 0.12+ uses different builder patterns. |
| 2026-02-20 | `Schema-validation: wrong column type encountered in column [import_type] in table [bulk_imports]; found [varchar (Types#VARCHAR)], but expecting [enum ('csv','credit_card_statement') (Types#ENUM)]` — Docker container crash-loops on startup | Hibernate 6.4.1 on MySQL maps `@Enumerated(EnumType.STRING)` to native MySQL ENUM type by default. The Flyway migration creates `VARCHAR(30)` columns, causing a schema validation mismatch | Added `@JdbcTypeCode(Types.VARCHAR)` annotation (from `org.hibernate.annotations.JdbcTypeCode` + `java.sql.Types`) to both `importType` and `status` enum fields in `BulkImport.java`, matching the existing pattern in `JobExecution.java` | **CRITICAL: When using `@Enumerated(EnumType.STRING)` with Hibernate 6.x on MySQL and VARCHAR columns in Flyway migrations, you MUST also add `@JdbcTypeCode(Types.VARCHAR)` to prevent Hibernate from expecting a native MySQL ENUM type. Always check existing entities (e.g., `JobExecution.java`) for the correct pattern before adding new enum fields.** |
| 2026-02-23 | 69 compilation errors after package refactoring: `cannot find symbol` for entity cross-references (User, Account, TransactionCategory, JobExecution, etc.) | Entities that were in the same `com.minted.api.entity` package referenced each other without imports (same-package access). After moving to separate feature packages (e.g., `account.entity`, `user.entity`), these implicit references broke | Added explicit cross-module imports to all entity files (e.g., `import com.minted.api.user.entity.User;` in Account.java) | **When refactoring packages: entities/classes in the same package reference each other without imports. After splitting into different packages, ALL cross-references need explicit imports. Also: wildcard imports like `import com.minted.api.dto.*` must be replaced with specific imports from the new module packages. Always do a full build after package refactoring.** |
| 2026-02-23 | `NamedQueryValidationException: Could not interpret path expression 'com.minted.api.enums.TransactionType.EXPENSE'` — app fails to start after package refactoring | JPQL `@NamedQuery` annotations use fully-qualified enum class names as string literals (e.g., `com.minted.api.enums.TransactionType.EXPENSE`). These are NOT caught by Java compilation — they only fail at runtime when Hibernate validates the queries | Updated the JPQL string from `com.minted.api.enums.TransactionType.EXPENSE` to `com.minted.api.transaction.enums.TransactionType.EXPENSE` | **CRITICAL: When refactoring packages, JPQL/HQL queries with fully-qualified class names in string literals (inside `@NamedQuery`, `@Query` annotations) are NOT caught by the Java compiler. Always grep the entire codebase for old package paths in string literals after a package refactoring. Use `grep -r "com.minted.api.enums"` (or the old package prefix) to find these.** |

---

## SQL / Migration Errors

| Date | Error | Root Cause | Fix | Prevention |
|------|-------|------------|-----|------------|
| 2026-02-16 | Login failing with "Invalid username or password" despite correct credentials | The BCrypt hash `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy` in V0_0_1 migration was incorrect and did not match the password "admin" | Created V0_0_2 migration to update admin password with correct BCrypt hash generated from Spring's BCryptPasswordEncoder | **CRITICAL:** Always generate BCrypt hashes using the SAME BCryptPasswordEncoder that will be used in production. Never use online generators or different BCrypt implementations as they may produce incompatible hashes. Test password verification immediately after creating seed users. |
| 2026-02-16 | "Flyway checksum mismatch for migration version 0.0.1" - Server fails to start | Modified V0_0_1 migration file after it was already applied to database. Flyway stores checksums of applied migrations and validates them on startup | Reverted V0_0_1 file to exact original content (removed added comments). Checksum matched and server started successfully | **NEVER modify a migration file after it has been applied to ANY database (even dev). If you need to fix data, create a NEW migration (e.g., V0_0_2). Only modify migrations that have never been applied. To fix checksum mismatches: use `flyway repair` or restore the file to its exact original state.** |
| 2026-02-17 | "Unknown column 'account_number' in 'field list'" in V0_0_11 migration | Migration SQL referenced `account_number` column that doesn't exist in accounts table schema (V0_0_5). Did not verify table schema before writing INSERT statement | Read V0_0_5__create_accounts_table.sql to check actual schema. Removed `account_number` column from INSERT and incorporated account numbers into the `name` field instead | **ALWAYS read the table creation migration before writing INSERT statements in seed data migrations. Never assume column names - verify the actual schema. Use `SHOW COLUMNS FROM table_name;` or read the CREATE TABLE migration file to confirm column names and types.** |
| 2026-02-17 | "Validate failed: Detected failed migration to version 0.0.11" - Backend fails to start after fixing migration | Flyway records failed migrations in `flyway_schema_history` with `success=0`. Even after fixing the SQL file, Flyway sees the failed entry and refuses to retry | Deleted the failed entry from flyway_schema_history table: `DELETE FROM flyway_schema_history WHERE version = '0.0.11';` Then restarted backend which successfully applied the corrected migration | **When a Flyway migration fails, you MUST delete the failed entry from flyway_schema_history before retrying. Use: `DELETE FROM flyway_schema_history WHERE version = 'X.X.X' AND success = 0;` or use `./gradlew flywayRepair` (though direct SQL is more reliable). Always verify deletion with `SELECT * FROM flyway_schema_history WHERE version = 'X.X.X';` before restarting.** |

---

## API Integration Errors

| Date | Error | Root Cause | Fix | Prevention |
|------|-------|------------|-----|------------|
| 2026-02-19 | TS2729: Property used before initialization | Initialized class fields using `this.themeService` at declaration level, before the constructor runs | Moved assignments into the constructor body; declared fields with `!` and explicit types (`Observable<boolean>`) | **NEVER reference `this.injectedService` in class field initializers. Constructor parameters are only set after `constructor()` runs. Either declare with `!` and assign in constructor, OR use `ngOnInit()` for initialization.**

---

## UI / Styling Errors

| Date | Error | Root Cause | Fix | Prevention |
|------|-------|------------|-----|------------|
| 2026-02-17 | Search input with icon misaligned with dropdown filters in transactions page | Used basic HTML `<span class="p-input-icon-left">` instead of proper PrimeNG component for icon input | Replaced with PrimeNG `<p-iconfield iconPosition="left"><p-inputicon styleClass="pi pi-search" /><input pInputText /></p-iconfield>` components | **ALWAYS use PrimeNG IconField component for inputs with icons. Never use basic span with `p-input-icon-left` class. Always check SharedModule for available PrimeNG components before implementing UI. Always prefer PrimeNG components over custom HTML/CSS implementations.** |

---

## Implementation Notes

| Date | Feature | Implementation Details | Notes |
|------|---------|----------------------|-------|
| 2026-02-17 | Analytics Overview Dashboard | Implemented full analytics dashboard with PrimeNG components including: p-card for summary cards, p-chart for spending activity, p-accordion for recurring payments, p-tag for status badges, p-progressbar for forecast visualization, p-skeleton for loading states. All data fetched from existing backend APIs: /analytics/total-balance, /analytics/summary, /analytics/spending-activity, /transactions/date-range, /recurring-transactions, /recurring-transactions/summary. No new backend APIs needed. | Successfully used all PrimeNG components from SharedModule. Added AccordionModule, BadgeModule, TagModule, ProgressBarModule to SharedModule. All APIs already exist in backend - no backend changes required. Component is fully dynamic and responsive. |
| 2026-02-17 | Analytics UI Color Scheme Update | Updated analytics overview to match consistent color scheme across all pages. Main changes: text-slate-900 for headings, text-slate-600 for labels, text-slate-500 for secondary text, bg-white with border-slate-200 for cards, #c48821 (amber) for primary actions, added p-8 padding to main container. Removed greeting header ("Good evening, System Administrator") and subtitle. Changed washed-out colors to vibrant amber-600, green-700, red-700 for better contrast and consistency. | **Color scheme standards for new pages: Primary text: text-slate-900, Labels: text-slate-600, Secondary/meta text: text-slate-500, Cards: bg-white border-slate-200, Primary color: #c48821 (amber), Container padding: p-8. Always match existing pages' color schemes before creating new UI.** |
| 2026-02-18 | Transactions Page AG Grid Migration | Replaced PrimeNG p-table with AG Grid (ag-grid-community v35.1.0). Created custom "ag-theme-minted" theme matching reference design. Implemented features: checkbox selection, external filters (date, account, category, search), AG Grid native pagination, edit functionality via cell click, custom cell renderers for category icons and edit button, hover-to-show edit button, Material Icons Outlined integration. Kept existing PrimeNG dialog for add/edit/delete. Increased Angular budget for anyComponentStyle from 16kB to 500kB to accommodate AG Grid CSS (~230kB). | **AG Grid Integration: Import AgGridModule in feature module, use custom theme for consistent styling, cell renderers for complex UI elements, external filters work with rowData updates, pagination built-in to AG Grid. AG Grid CSS is large (~230kB) - adjust budgets in angular.json. Use Material Icons Outlined for icon consistency. Cell renderers return HTML strings for performance.** |
