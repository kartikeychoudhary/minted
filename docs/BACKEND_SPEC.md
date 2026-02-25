# BACKEND_SPEC.md — Minted API (Spring Boot Backend)

---

## 1. Project Setup

### 1.1 Gradle Configuration (`build.gradle`)
```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.x'  // latest stable 3.2
    id 'io.spring.dependency-management' version '1.1.x'
    id 'org.flywaydb.flyway' version '10.x'
}

group = 'com.minted'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
    targetCompatibility = '17'
}

dependencies {
    // Core
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'

    // Database
    runtimeOnly 'com.mysql:mysql-connector-j'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'

    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'

    // Encryption
    implementation 'com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5'

    // API Docs
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'

    // Utility
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

### 1.2 Environment Variables

**All sensitive values MUST come from environment variables, never hardcoded.**

| Variable | Description | Example |
|----------|-------------|---------|
| `MINTED_DB_HOST` | MySQL host | `localhost` |
| `MINTED_DB_PORT` | MySQL port | `3306` |
| `MINTED_DB_NAME` | Database name | `minted_db` |
| `MINTED_DB_USER` | DB username | `minted_user` |
| `MINTED_DB_PASSWORD` | DB password | `encrypted_value` |
| `MINTED_JWT_SECRET` | JWT signing key (min 256-bit) | `base64-encoded-secret` |
| `MINTED_JWT_EXPIRATION` | Token expiry in ms | `86400000` (24h) |
| `MINTED_JASYPT_PASSWORD` | Jasypt encryption salt | `your_salt_password` |
| `MINTED_CORS_ORIGINS` | Allowed CORS origins | `http://localhost:4200` |
| `MINTED_ADMIN_PASSWORD` | Initial admin password (encrypted) | `ENC(encrypted_value)` |

### 1.3 `application.properties`
```properties
spring.application.name=minted-api
server.port=5500

# Database
spring.datasource.url=jdbc:mysql://${MINTED_DB_HOST}:${MINTED_DB_PORT}/${MINTED_DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${MINTED_DB_USER}
spring.datasource.password=${MINTED_DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# JWT
app.jwt.secret=${MINTED_JWT_SECRET}
app.jwt.expiration=${MINTED_JWT_EXPIRATION:86400000}

# CORS
app.cors.allowed-origins=${MINTED_CORS_ORIGINS:http://localhost:4200}

# Jasypt
jasypt.encryptor.password=${MINTED_JASYPT_PASSWORD}
jasypt.encryptor.algorithm=PBEWithMD5AndDES
jasypt.encryptor.iv-generator-classname=org.jasypt.iv.NoIvGenerator

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui
```

---

## 1.4 Package Structure (Feature-Based Modules)

The backend is organized into **feature-based modules** where each domain's controller, DTOs, entity, repository, and service live together under `com.minted.api.<module>/`.

```
com.minted.api/
├── MintedApiApplication.java
├── common/                    # Shared infrastructure
│   ├── config/                # SecurityConfig, SchedulerConfig, DataInitializer, RequestLoggingInterceptor
│   ├── exception/             # All exceptions + GlobalExceptionHandler
│   ├── filter/                # JwtAuthFilter, MdcFilter
│   └── util/                  # JwtUtil
├── auth/                      # Authentication & signup
├── user/                      # User entity & profile
├── account/                   # Accounts + Account Types
├── transaction/               # Transactions + Categories
├── budget/                    # Budget management
├── dashboard/                 # Dashboard cards & charts
├── analytics/                 # Analytics & reporting
├── recurring/                 # Recurring transactions
├── notification/              # Notification system
├── job/                       # Job execution framework (shared)
├── bulkimport/                # CSV bulk import
├── statement/                 # Credit card statement parser
├── llm/                       # LLM config, models, merchant mappings
└── admin/                     # Admin management, default lists, system settings
```

Each feature module follows this internal structure:
```
<module>/
├── controller/     # REST endpoint(s)
├── dto/            # Request/Response DTOs
├── entity/         # JPA entity(ies)
├── enums/          # Enumerations (if any)
├── job/            # Scheduled jobs (if any, e.g., bulkimport/job/)
├── repository/     # Spring Data JPA repository(ies)
└── service/        # Interface(s) + implementation(s)
```

**Key cross-module references:**
- Nearly every module imports `com.minted.api.user.entity.User` and `com.minted.api.user.repository.UserRepository`
- `NotificationHelper` (`notification.service`) is used by: auth, bulkimport, statement, admin
- `SystemSettingService` (`admin.service`) is used by: auth, statement, llm
- `TransactionRepository` (`transaction.repository`) is used by: transaction, analytics, bulkimport, statement
- `AccountRepository` (`account.repository`) is used by: account, transaction, analytics, bulkimport, statement, recurring
- Job framework entities (`job.entity`) are used by: bulkimport, statement

**Spring component scanning** works from the `com.minted.api` base package — all sub-packages are auto-scanned.

---

## 2. Database Schema (Flyway Migrations)

### V0_0_1__create_users_table.sql
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    email VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    force_password_change BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Default admin user (password: admin — user MUST change after first login)
INSERT INTO users (username, password, display_name, force_password_change)
VALUES ('admin', '$2a$10$PLACEHOLDER_BCRYPT_HASH', 'Administrator', TRUE);
```

### V0_0_2__create_account_types_table.sql
```sql
CREATE TABLE account_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(50),
    user_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Seed default account types
-- These will be created programmatically per-user on registration
```

### V0_0_3__create_accounts_table.sql
```sql
CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    account_type_id BIGINT NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'INR',
    color VARCHAR(7),
    icon VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_type_id) REFERENCES account_types(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### V0_0_4__create_transaction_categories_table.sql
```sql
CREATE TABLE transaction_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME', 'EXPENSE', 'TRANSFER') NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(7),
    parent_id BIGINT,
    user_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES transaction_categories(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### V0_0_5__create_transactions_table.sql
```sql
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(15,2) NOT NULL,
    type ENUM('INCOME', 'EXPENSE', 'TRANSFER') NOT NULL,
    description VARCHAR(500),
    notes TEXT,
    transaction_date DATE NOT NULL,
    account_id BIGINT NOT NULL,
    to_account_id BIGINT,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    tags VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (to_account_id) REFERENCES accounts(id),
    FOREIGN KEY (category_id) REFERENCES transaction_categories(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_user_date (user_id, transaction_date),
    INDEX idx_user_account (user_id, account_id)
);
```

### V0_0_6__create_budgets_table.sql
```sql
CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    category_id BIGINT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES transaction_categories(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_budget_month_year_cat (user_id, month, year, category_id)
);
```

### V0_0_7__create_dashboard_cards_table.sql
```sql
CREATE TABLE dashboard_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    chart_type ENUM('BAR', 'LINE', 'PIE', 'DOUGHNUT', 'AREA', 'STACKED_BAR') NOT NULL,
    x_axis_measure VARCHAR(50) NOT NULL,
    y_axis_measure VARCHAR(50) NOT NULL,
    filters JSON,
    position_order INT DEFAULT 0,
    width ENUM('HALF', 'FULL') DEFAULT 'HALF',
    user_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### V0_0_8__seed_default_data.sql
```sql
-- Default account types for admin user (id=1)
INSERT INTO account_types (name, description, icon, user_id) VALUES
('Bank Account', 'Savings and current bank accounts', 'fa-building-columns', 1),
('Credit Card', 'Credit card accounts', 'fa-credit-card', 1),
('Wallet', 'Cash and digital wallets', 'fa-wallet', 1),
('Investment', 'Investment and trading accounts', 'fa-chart-line', 1);

-- Default transaction categories for admin user (id=1)
INSERT INTO transaction_categories (name, type, icon, color, user_id) VALUES
('Salary', 'INCOME', 'fa-briefcase', '#4CAF50', 1),
('Freelance', 'INCOME', 'fa-laptop', '#8BC34A', 1),
('Interest', 'INCOME', 'fa-percent', '#CDDC39', 1),
('Food & Dining', 'EXPENSE', 'fa-utensils', '#FF5722', 1),
('Groceries', 'EXPENSE', 'fa-cart-shopping', '#FF9800', 1),
('Transport', 'EXPENSE', 'fa-car', '#2196F3', 1),
('Utilities', 'EXPENSE', 'fa-bolt', '#FFC107', 1),
('Entertainment', 'EXPENSE', 'fa-film', '#9C27B0', 1),
('Shopping', 'EXPENSE', 'fa-bag-shopping', '#E91E63', 1),
('Health', 'EXPENSE', 'fa-heart-pulse', '#00BCD4', 1),
('Education', 'EXPENSE', 'fa-graduation-cap', '#3F51B5', 1),
('Rent', 'EXPENSE', 'fa-house', '#795548', 1),
('EMI', 'EXPENSE', 'fa-money-bill-transfer', '#607D8B', 1),
('Transfer', 'TRANSFER', 'fa-arrow-right-arrow-left', '#9E9E9E', 1);

-- Default dashboard cards for admin user
INSERT INTO dashboard_cards (title, chart_type, x_axis_measure, y_axis_measure, position_order, width, user_id) VALUES
('Monthly Expenses', 'BAR', 'month', 'total_amount', 1, 'HALF', 1),
('Category Breakdown', 'DOUGHNUT', 'category', 'total_amount', 2, 'HALF', 1),
('Income vs Expense', 'LINE', 'month', 'total_amount', 3, 'FULL', 1),
('Top Spending Categories', 'PIE', 'category', 'total_amount', 4, 'HALF', 1);
```

### V0_0_21__create_system_settings_table.sql
```sql
CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(500) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed default settings
INSERT INTO system_settings (setting_key, setting_value, description)
VALUES ('SIGNUP_ENABLED', 'false', 'Controls whether public user registration is allowed');
```

---

## 3. Authentication & Security

### 3.1 JWT Flow
1. User sends `POST /api/v1/auth/login` with `{ username, password }`.
2. Server validates credentials against BCrypt-hashed password in DB.
3. Server returns `{ token, refreshToken, forcePasswordChange }`.
4. If `forcePasswordChange` is `true`, frontend must redirect to password change screen.
5. All subsequent requests include `Authorization: Bearer <token>` header.
6. `JwtAuthFilter` extracts and validates the token on every request.

### 3.2 Default Admin Setup
- Username: `admin`
- Password: `admin` (BCrypt hashed in migration)
- `force_password_change: true` — frontend MUST prompt password change on first login.
- After password change, set `force_password_change = false`.

### 3.3 Public Signup Flow
1. Frontend calls `GET /api/v1/auth/signup-enabled` to check if registration is open.
2. User submits `POST /api/v1/auth/signup` with `{ username, password, confirmPassword, displayName, email }`.
3. Server validates: signup enabled, username unique, passwords match, password strength.
4. Server creates user (role=USER, forcePasswordChange=false, isActive=true), seeds default data, generates JWT tokens.
5. Returns `LoginResponse` — frontend auto-logs in the new user.

### 3.4 Security Configuration
- Permit: `/api/v1/auth/**`, `/swagger-ui/**`, `/api-docs/**`
- Require auth: everything else under `/api/**`
- CORS: Allow `MINTED_CORS_ORIGINS` env variable values
- CSRF: Disabled (stateless JWT)
- Session: `STATELESS`
- **Filter chain order:** `MdcFilter` → `JwtAuthFilter` → `UsernamePasswordAuthenticationFilter`
- `MdcFilter` sets request tracing context (requestId, method, uri, clientIp) in SLF4J MDC
- `JwtAuthFilter` adds `userId` to MDC after successful JWT validation

### 3.5 Password Change Endpoint
```
PUT /api/v1/auth/change-password
Body: { currentPassword, newPassword, confirmPassword }
```
- Validate current password matches
- Validate newPassword == confirmPassword
- Validate newPassword != currentPassword
- Minimum 8 characters, at least one uppercase, one number
- Set `force_password_change = false` after successful change

---

## 4. API Endpoints

### 4.1 Auth
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/login` | Login, returns JWT |
| POST | `/api/v1/auth/refresh` | Refresh token |
| PUT | `/api/v1/auth/change-password` | Change password |
| POST | `/api/v1/auth/signup` | Public signup (when enabled) |
| GET | `/api/v1/auth/signup-enabled` | Check if signup is enabled |

### 4.2 Accounts
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/accounts` | List all accounts for user |
| GET | `/api/v1/accounts/{id}` | Get account by ID |
| POST | `/api/v1/accounts` | Create new account |
| PUT | `/api/v1/accounts/{id}` | Update account |
| DELETE | `/api/v1/accounts/{id}` | Soft delete account |

### 4.3 Account Types
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/account-types` | List all account types |
| POST | `/api/v1/account-types` | Create new type |
| PUT | `/api/v1/account-types/{id}` | Update type |
| DELETE | `/api/v1/account-types/{id}` | Soft delete type |

### 4.4 Transaction Categories
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/categories` | List all categories |
| GET | `/api/v1/categories?type=EXPENSE` | Filter by type |
| POST | `/api/v1/categories` | Create new category |
| PUT | `/api/v1/categories/{id}` | Update category |
| DELETE | `/api/v1/categories/{id}` | Soft delete category |

### 4.5 Transactions
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/transactions` | List with pagination & filters |
| GET | `/api/v1/transactions/{id}` | Get by ID |
| POST | `/api/v1/transactions` | Create transaction |
| PUT | `/api/v1/transactions/{id}` | Update transaction |
| DELETE | `/api/v1/transactions/{id}` | Delete transaction |

**Transaction Query Parameters:**
| Param | Type | Description |
|-------|------|-------------|
| `page` | int | Page number (0-indexed) |
| `size` | int | Page size (default 20) |
| `startDate` | String (yyyy-MM-dd) | Filter from date |
| `endDate` | String (yyyy-MM-dd) | Filter to date |
| `accountId` | Long | Filter by account |
| `categoryId` | Long | Filter by category |
| `type` | String | INCOME / EXPENSE / TRANSFER |
| `search` | String | Search in description |
| `sortBy` | String | Field to sort (default: transactionDate) |
| `sortDir` | String | ASC / DESC (default: DESC) |

**Predefined Filters (resolved server-side):**
| Param | Value | Resolution |
|-------|-------|------------|
| `period` | `LAST_WEEK` | Last 7 days |
| `period` | `LAST_MONTH` | Last 30 days |
| `period` | `THIS_MONTH` | Current calendar month |
| `period` | `LAST_3_MONTHS` | Last 90 days |
| `period` | `CUSTOM` | Use startDate + endDate |

**Transaction ↔ Split Integration:**
- `TransactionResponse` includes `isSplit: Boolean` field indicating whether the transaction has been split
- `TransactionServiceImpl` queries `SplitTransactionRepository.findSourceTransactionIdsByUserId()` to build a `Set<Long>` of split source IDs, then passes `splitIds.contains(t.getId())` to `TransactionResponse.from()` in all read methods and `update()`
- `create()` always returns `isSplit = false`

### 4.6 Budgets
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/budgets` | List budgets (filter by month/year) |
| POST | `/api/v1/budgets` | Create budget |
| PUT | `/api/v1/budgets/{id}` | Update budget |
| DELETE | `/api/v1/budgets/{id}` | Delete budget |
| GET | `/api/v1/budgets/summary?month=X&year=Y` | Budget vs actual summary |

### 4.7 Dashboard
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/dashboard/cards` | Get user's dashboard cards |
| POST | `/api/v1/dashboard/cards` | Create new card |
| PUT | `/api/v1/dashboard/cards/{id}` | Update card (axes, type, etc.) |
| DELETE | `/api/v1/dashboard/cards/{id}` | Delete card |
| PUT | `/api/v1/dashboard/cards/reorder` | Reorder cards |
| GET | `/api/v1/dashboard/cards/{id}/data` | Get chart data for a specific card |

**Dashboard Card Data Request:**
```
GET /api/v1/dashboard/cards/{id}/data?startDate=2026-01-01&endDate=2026-01-31
```
**Available X-Axis measures:** `day`, `week`, `month`, `year`, `category`, `account`, `type`
**Available Y-Axis measures:** `total_amount`, `count`, `average_amount`

### 4.8 Reports / Analytics
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/analytics/summary` | Total income, expense, balance for period |
| GET | `/api/v1/analytics/category-wise` | Breakdown by category for period |
| GET | `/api/v1/analytics/account-wise` | Breakdown by account for period |
| GET | `/api/v1/analytics/trend` | Monthly trend data |

---

## 5. Error Handling

### Global Exception Handler (`@RestControllerAdvice`)

All API errors return this format:
```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "details": ["amount must be greater than 0", "description must not be blank"],
  "timestamp": "2026-02-15T10:30:00Z",
  "path": "/api/v1/transactions"
}
```

### Custom Exceptions:
- `ResourceNotFoundException` → 404
- `DuplicateResourceException` → 409
- `UnauthorizedException` → 401
- `ForbiddenException` → 403
- `BadRequestException` → 400
- `ForcePasswordChangeException` → 403 (special flag for frontend)

---

## 6. Data Ownership

**Every query MUST filter by the authenticated user's ID.** No user should ever see another user's data. Use a utility method or base service to extract the current user ID from the security context:

```java
public Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return ((UserPrincipal) auth.getPrincipal()).getId();
}
```

---

## 7. Job Scheduling & Execution Framework

### 7.1 Architecture

The job system provides cron-based scheduling with full execution tracking. It is designed as a reusable framework — new jobs can be added by implementing `Runnable` and registering with `JobSchedulerService`.

```
SchedulerConfig (@EnableScheduling)
  └── ThreadPoolTaskScheduler (pool=5, prefix="JobTaskScheduler-")
        └── JobSchedulerService (manages ConcurrentHashMap of ScheduledFuture<?>)
              └── Registered Runnable tasks (e.g. RecurringTransactionJob)

JobExecution → JobStepExecution (1:N, cascade ALL, ordered by stepOrder)
JobExecution → JobScheduleConfig (N:1, optional FK)
```

### 7.2 Database Tables (V0_0_16)

| Table | Purpose |
|-------|---------|
| `job_schedule_configs` | Cron schedule per job name (unique), enabled flag, last run timestamp |
| `job_executions` | One row per run — status (RUNNING/COMPLETED/FAILED), trigger type (SCHEDULED/MANUAL), start/end times, step progress |
| `job_step_executions` | One row per step within an execution — status, order, context JSON, error message |

### 7.3 Entities

| Entity | Key Fields |
|--------|------------|
| `JobScheduleConfig` | jobName (unique), cronExpression, enabled, lastRunAt, description |
| `JobExecution` | jobName, status (enum), triggerType (enum), startTime, endTime, errorMessage, totalSteps, completedSteps, steps (OneToMany) |
| `JobStepExecution` | jobExecution (ManyToOne), stepName, stepOrder, status (enum), contextJson (TEXT), errorMessage, startTime, endTime |

### 7.4 Enums

| Enum | Values |
|------|--------|
| `JobStatus` | RUNNING, COMPLETED, FAILED |
| `JobStepStatus` | PENDING, RUNNING, COMPLETED, FAILED, SKIPPED |
| `JobTriggerType` | SCHEDULED, MANUAL |

### 7.5 Services

**`JobSchedulerService`** — Manages Spring `TaskScheduler` scheduling:
- `registerJob(name, task, cron, enabled)` — Called at `@PostConstruct` by each job
- `rescheduleJob(name, cron, enabled)` — Called when admin updates schedule config
- `triggerJob(name)` — Executes registered task immediately in a new thread

**`JobExecutionService`** — CRUD for execution history and schedule configs:
- `getAllJobExecutions(pageable)` — Paginated list ordered by startTime DESC
- `getJobExecutionById(id)` — Single execution with steps
- `triggerJobManually(jobName)` — Validates config exists, delegates to scheduler
- `getAllScheduleConfigs()` / `updateScheduleConfig(id, request)` — Schedule CRUD

### 7.6 Implemented Jobs

#### RecurringTransactionJob (`recurring/job/RecurringTransactionJob.java`)
- **Cron:** `0 0 1 * * ?` (daily at 1 AM)
- **Purpose:** Processes all active recurring transactions whose `nextExecutionDate <= today`
- **Steps:**
  1. **Fetch** — Query `RecurringTransaction` by status=ACTIVE and nextExecutionDate <= today
  2. **Process** — For each due transaction: create `Transaction` entity, update `nextExecutionDate`
  3. **Update Config** — Set `lastRunAt` on schedule config
- **Error handling:** Per-transaction try/catch; step fails only if all transactions fail
- **Registration:** `@PostConstruct init()` reads config from DB, calls `jobSchedulerService.registerJob()`

#### BulkImportJob (`bulkimport/job/BulkImportJob.java`)
- **Cron:** `0 */5 * * * ?` (every 5 minutes)
- **Purpose:** Sweeps for stuck imports with status=IMPORTING and no active job execution, processes them
- **Processing (via `BulkImportServiceImpl.processImportAsync`):**
  1. **Re-validate CSV** — Re-parse stored csvData, re-run all validations (categories/accounts may have changed)
  2. **Check Duplicates** — Check each valid row against existing transactions, apply skipDuplicates flag
  3. **Insert Transactions** — Create Transaction entities, update account balances (INCOME adds, EXPENSE subtracts)
  4. **Summary** — Update BulkImport with final counts, set status to COMPLETED or FAILED
- **Async processing:** `confirmImport()` uses `TransactionSynchronizationManager.registerSynchronization(afterCommit)` to defer `CompletableFuture.runAsync()` until the parent transaction commits. `processImportAsync()` uses `TransactionTemplate` for a programmatic transaction (self-invocation bypasses `@Transactional`).
- **Registration:** `@PostConstruct init()` reads config from DB, calls `jobSchedulerService.registerJob()`

### 7.8 Bulk Import

#### V0_0_20__create_bulk_imports_table.sql
```sql
CREATE TABLE bulk_imports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    import_type VARCHAR(30) NOT NULL DEFAULT 'CSV',
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT DEFAULT 0,
    total_rows INT NOT NULL DEFAULT 0,
    valid_rows INT NOT NULL DEFAULT 0,
    duplicate_rows INT NOT NULL DEFAULT 0,
    error_rows INT NOT NULL DEFAULT 0,
    imported_rows INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    job_execution_id BIGINT NULL,
    csv_data LONGTEXT NULL,
    validation_result JSON NULL,
    skip_duplicates BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_import_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_import_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_import_job_exec FOREIGN KEY (job_execution_id)
        REFERENCES job_executions(id) ON DELETE SET NULL
);
```

#### Enums
| Enum | Values |
|------|--------|
| `ImportStatus` | PENDING, VALIDATING, VALIDATED, IMPORTING, COMPLETED, FAILED |
| `ImportType` | CSV, CREDIT_CARD_STATEMENT |

#### Entity: `BulkImport`
- Relationships: ManyToOne to User, Account, JobExecution (all LAZY)
- Enum fields use `@JdbcTypeCode(Types.VARCHAR)` (required for Hibernate 6.x + MySQL VARCHAR columns)
- `csvData` (LONGTEXT) stores raw CSV, `validationResult` (JSON) stores per-row validation previews
- `skipDuplicates` persisted for async processing

#### DTOs
| DTO | Type | Purpose |
|-----|------|---------|
| `CsvRowPreview` | record | Per-row validation result (rowNumber, date, amount, type, description, categoryName, status, errorMessage, matchedCategoryId, isDuplicate) |
| `CsvUploadResponse` | record | Upload response with importId, row counts, and row previews |
| `BulkImportConfirmRequest` | record | Confirm request with importId and skipDuplicates |
| `BulkImportResponse` | record | Import metadata with `from(BulkImport)` factory method |

#### Service: `BulkImportService` / `BulkImportServiceImpl`
- `getCsvTemplate()` — Returns sample CSV bytes
- `uploadAndValidate(file, accountId, userId)` — Synchronous CSV parsing + per-row validation + duplicate checking, 5000 row limit
- `confirmImport(request, userId)` — Sets IMPORTING status, creates JobExecution, defers async processing until after transaction commit
- `processImportAsync(bulkImportId)` — 4-step job processing wrapped in `TransactionTemplate`
- `getUserImports(userId)` / `getImportById(importId, userId)` / `getImportJobDetails(importId, userId)`

### 7.7 Adding a New Job

1. Create class implementing `Runnable` in `<feature>/job/` package (e.g., `bulkimport/job/BulkImportJob.java`)
2. Inject `JobExecutionRepository`, `JobScheduleConfigRepository`, `JobSchedulerService` from the `job` module
3. In `@PostConstruct`, call `jobSchedulerService.registerJob(JOB_NAME, this, cron, enabled)`
4. In `run()`, create `JobExecution`, add `JobStepExecution` entries, handle errors
5. Add seed row to a Flyway migration for `job_schedule_configs`

### 7.9 Admin Controller (`/api/v1/admin`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/admin/users` | List all users |
| GET | `/admin/users/{id}` | Get user by ID |
| POST | `/admin/users` | Create new user (201) |
| PUT | `/admin/users/{id}/toggle` | Toggle user active/inactive |
| DELETE | `/admin/users/{id}` | Delete user + all data (204) |
| PUT | `/admin/users/{id}/reset-password` | Reset user password |
| GET | `/admin/settings/{key}` | Get system setting by key |
| PUT | `/admin/settings/{key}` | Update system setting |
| GET | `/admin/jobs?page=&size=` | List job executions (paginated) |
| GET | `/admin/jobs/{id}` | Get execution details with steps |
| POST | `/admin/jobs/{jobName}/trigger` | Manually trigger a job (returns 202) |
| GET | `/admin/schedules` | List all schedule configs |
| PUT | `/admin/schedules/{id}` | Update cron expression and enabled flag |
| GET | `/admin/defaults/categories` | List default categories |
| POST | `/admin/defaults/categories` | Create default category |
| DELETE | `/admin/defaults/categories/{id}` | Delete default category |
| GET | `/admin/defaults/account-types` | List default account types |
| POST | `/admin/defaults/account-types` | Create default account type |
| DELETE | `/admin/defaults/account-types/{id}` | Delete default account type |

### 7.10 User Management Service

**`UserManagementService` / `UserManagementServiceImpl`** — Admin-only user CRUD:

- **`getAllUsers()`**: Returns all users mapped to `AdminUserResponse` DTOs
- **`createUser(request)`**: Validates username uniqueness and password strength, creates user with `forcePasswordChange=true`, seeds default account types and categories
- **`toggleUserActive(userId, currentUsername)`**: Flips `isActive` flag. Prevents admin from disabling their own account
- **`deleteUser(userId, currentUsername)`**: Cascading delete of all user data (transactions, recurring transactions, budgets, accounts, account types, categories, bulk imports, dashboard cards), then deletes the user. Prevents self-deletion
- **`resetPassword(userId, request)`**: Validates password strength, encodes password, sets `forcePasswordChange=true`

### 7.11 System Settings

**Entity:** `SystemSetting` — Key-value store for app-wide settings.

| Field | Type | Notes |
|-------|------|-------|
| id | BIGINT | Auto-increment PK |
| settingKey | VARCHAR(100) | Unique |
| settingValue | VARCHAR(500) | |
| description | VARCHAR(255) | |
| createdAt | DATETIME | @CreationTimestamp |
| updatedAt | DATETIME | @UpdateTimestamp |

**`SystemSettingService` / `SystemSettingServiceImpl`:**
- `getValue(key)` — Returns setting value string
- `getSetting(key)` — Returns `SystemSettingResponse` DTO
- `updateSetting(key, value)` — Updates and returns DTO
- `isSignupEnabled()` — Convenience: `"true".equalsIgnoreCase(getValue("SIGNUP_ENABLED"))`

### 7.12 User Management DTOs

| DTO | Type | Fields |
|-----|------|--------|
| `AdminUserResponse` | record | id, username, displayName, email, isActive, forcePasswordChange, currency, role, createdAt, updatedAt. Factory: `from(User)` |
| `CreateUserRequest` | record | username (@NotBlank, 3-50), password (@NotBlank, min 8), displayName, email (@Email), role |
| `ResetPasswordRequest` | record | newPassword (@NotBlank, min 8) |
| `SignupRequest` | record | username, password, confirmPassword, displayName, email |
| `SystemSettingResponse` | record | id, settingKey, settingValue, description. Factory: `from(SystemSetting)` |
| `UpdateSettingRequest` | record | value (@NotBlank) |

## 8. Notification System

### 8.1 Notification Entity

**Table:** `notifications` (V0_0_22 migration)

| Field | Type | Notes |
|-------|------|-------|
| id | BIGINT | Auto-increment PK |
| user_id | BIGINT | FK → users(id) ON DELETE CASCADE |
| title | VARCHAR(255) | Not null |
| message | TEXT | Not null |
| type | VARCHAR(30) | INFO, SUCCESS, WARNING, ERROR, SYSTEM |
| is_read | BOOLEAN | Default false |
| created_at | TIMESTAMP | @CreationTimestamp |
| updated_at | TIMESTAMP | @UpdateTimestamp |

**Entity notes:** `@Enumerated(EnumType.STRING)` + `@JdbcTypeCode(Types.VARCHAR)` on type field (VARCHAR column, not MySQL ENUM).

### 8.2 NotificationType Enum

`com.minted.api.notification.enums.NotificationType` — Values: `INFO`, `SUCCESS`, `WARNING`, `ERROR`, `SYSTEM`

### 8.3 NotificationRepository

| Method | Return | Notes |
|--------|--------|-------|
| `findByUserIdOrderByCreatedAtDesc(userId, Pageable)` | `Page<Notification>` | Paginated list |
| `countByUserIdAndIsReadFalse(userId)` | `long` | Badge count |
| `findByIdAndUserId(id, userId)` | `Optional<Notification>` | Ownership check |
| `markAllAsReadByUserId(userId)` | `int` | @Modifying bulk update |
| `deleteByIdAndUserId(id, userId)` | `int` | @Modifying hard delete |
| `deleteAllReadByUserId(userId)` | `int` | @Modifying bulk delete |

### 8.4 NotificationHelper (Shared Creation Utility)

**File:** `com.minted.api.notification.service.NotificationHelper` — Concrete `@Component` (not interface+impl).

- `notify(userId, type, title, message)` — Primary overload
- `notify(userId, type, title, message, actionUrl)` — With optional action URL
- `notifyAll(type, title, message)` — Broadcasts to all users
- `@Transactional(propagation = Propagation.REQUIRES_NEW)` — Own transaction, failures never roll back caller
- Exceptions caught + logged but **never rethrown** — notifications are side-effects, not critical path

**Usage example:**
```java
notificationHelper.notify(userId, NotificationType.SUCCESS, "Welcome!", "Your account is ready.");
```

### 8.5 NotificationService / NotificationServiceImpl

| Method | Description |
|--------|-------------|
| `getNotifications(userId, Pageable)` | Paginated list (readOnly) |
| `getUnreadCount(userId)` | Count for badge (readOnly) |
| `markAsRead(id, userId)` | Single notification |
| `markAllAsRead(userId)` | Bulk update |
| `dismiss(id, userId)` | Hard DELETE single |
| `dismissAllRead(userId)` | Hard DELETE all read |

### 8.6 Notification DTOs

| DTO | Type | Fields |
|-----|------|--------|
| `NotificationResponse` | record | id, type (String), title, message, isRead, createdAt. Factory: `from(Notification)` |

---

## 9. Credit Card Statement Parser

### 9.1 Overview

Allows users to upload credit card PDF statements, extract text using Apache PDFBox, send the text to Google Gemini LLM for structured transaction extraction, and import the parsed transactions into their account.

**New dependency:** `org.apache.pdfbox:pdfbox:3.0.2` (Apache 2.0 licensed, Java-native PDF text extraction)

### 9.2 Database Schema

#### V0_0_23 — `llm_models`
Stores available LLM models (seeded with 3 Gemini models). Admin manages via `/admin/llm-models`.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | PK |
| name | VARCHAR(100) | Display name |
| provider | VARCHAR(50) | Default 'GEMINI' |
| model_key | VARCHAR(200) | API model identifier |
| description | VARCHAR(255) | |
| is_active | BOOLEAN | Hidden from users when false |
| is_default | BOOLEAN | Default model selection |
| created_at/updated_at | TIMESTAMP | |

#### V0_0_24 — `llm_configurations`
Per-user LLM settings. One row per user (user_id UNIQUE).

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | PK |
| user_id | BIGINT | FK → users (UNIQUE, CASCADE) |
| provider | VARCHAR(50) | Default 'GEMINI' |
| api_key | VARCHAR(500) | User's Gemini API key |
| model_id | BIGINT | FK → llm_models (SET NULL) |
| is_enabled | BOOLEAN | |

#### V0_0_25 — `credit_card_statements`
Tracks the 4-step statement parsing workflow.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | PK |
| user_id | BIGINT | FK → users (CASCADE) |
| account_id | BIGINT | FK → accounts |
| file_name | VARCHAR(255) | Original PDF filename |
| file_size | BIGINT | |
| status | VARCHAR(30) | UPLOADED, TEXT_EXTRACTED, LLM_PARSED, CONFIRMING, COMPLETED, FAILED |
| current_step | INT | 1-4 |
| extracted_text | LONGTEXT | PDFBox output |
| llm_response_json | LONGTEXT | Serialized ParsedTransactionRow list |
| parsed_count | INT | |
| duplicate_count | INT | |
| imported_count | INT | |
| error_message | TEXT | |
| job_execution_id | BIGINT | FK → job_executions (SET NULL) |
| pdf_password_hint | VARCHAR(20) | |

#### V0_0_26 — `merchant_category_mappings`
User-defined keyword-to-category rules for improving LLM accuracy.

| Column | Type | Notes |
|--------|------|-------|
| id | BIGINT | PK |
| user_id | BIGINT | FK → users (CASCADE) |
| snippets | VARCHAR(500) | Comma-separated keywords (e.g. "ZEPTO,BLINKIT") |
| category_id | BIGINT | FK → transaction_categories (CASCADE) |

#### V0_0_27 — System Settings Seed
Adds `CREDIT_CARD_PARSER_ENABLED` (true) and `ADMIN_LLM_KEY_SHARED` (false) to `system_settings`.

#### V0_0_30 — Add EMI Default Category
Inserts "EMI" (`pi pi-calculator`, EXPENSE) into `default_categories`. Auto-provisioned to users on next category API call via `mergeCategories()`.

### 9.3 Entities

| Entity | Table | Key Features |
|--------|-------|--------------|
| `LlmModel` | llm_models | Standard Lombok entity |
| `LlmConfiguration` | llm_configurations | ManyToOne LAZY → User, LlmModel |
| `CreditCardStatement` | credit_card_statements | `@JdbcTypeCode(Types.VARCHAR)` on status enum (Hibernate 6.x + MySQL VARCHAR) |
| `MerchantCategoryMapping` | merchant_category_mappings | `getSnippetList()` helper splits comma-separated snippets |

### 9.4 Enum

`StatementStatus`: UPLOADED, TEXT_EXTRACTED, LLM_PARSED, CONFIRMING, COMPLETED, FAILED

### 9.5 Services

#### StatementParserService / StatementParserServiceImpl
- Uses PDFBox 3.x `Loader.loadPDF(bytes, password)` + `PDFTextStripper`
- Handles password-protected PDFs; throws `BadRequestException` on wrong password
- Truncates text at 100,000 characters

#### LlmService (Interface) / GeminiLlmService (Implementation)
- Generic `LlmService` interface for future provider extensibility
- `parseStatement()` accepts `List<String> availableCategories` — the user's active category names (defaults + custom)
- `GeminiLlmService` calls `https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`
- Uses Spring `RestClient` (no new dependencies)
- Structured prompt engineering with:
  - **Available Categories block** — tells LLM "you MUST pick from this list, do NOT invent new categories"
  - **Merchant category hints** — injected as "ABSOLUTE RULES" (highest priority, overrides category list)
- JSON response parsing with markdown cleanup

#### MerchantMappingService / MerchantMappingServiceImpl
- CRUD for merchant-category mappings
- `getRawMappings(userId)` returns entities for internal use (pre-pass matching)
- Validates category ownership

#### LlmConfigService / LlmConfigServiceImpl
- Config CRUD with merchant mappings embedded in response
- Key resolution order: user key → admin shared key → throw exception
- `getEffectiveConfig(userId)` resolves API key and model for statement parsing

#### CreditCardStatementService / CreditCardStatementServiceImpl
Orchestrates the 4-step workflow:

1. **`uploadAndExtract`** — Validates PDF, extracts text via PDFBox, saves statement with TEXT_EXTRACTED status
2. **`triggerLlmParse`** — Resolves LLM config, creates JobExecution, fires async processing after commit
3. **`processLlmParseAsync`** — Fetches user's active categories via `TransactionCategoryService`, calls GeminiLlmService with category names + merchant mappings, applies merchant mapping pre-pass, runs duplicate detection, saves results
4. **`confirmImport`** — Creates Transaction entities from parsed rows, updates account balance, sets COMPLETED status

**Async pattern:** Uses `TransactionSynchronizationManager.registerSynchronization(afterCommit)` + `CompletableFuture.runAsync()` + `TransactionTemplate` (same proven pattern as BulkImportServiceImpl).

**Duplicate detection:** Matches by same accountId + same amount + date ±1 day + first 10 chars of description (case-insensitive).

**Merchant mapping pre-pass:** Before returning LLM results, iterates merchant mappings and overrides categoryName for matching descriptions. `mappedByRule = true` flag indicates rule-assigned categories.

### 9.6 Controllers

| Controller | Base Path | Endpoints |
|------------|-----------|-----------|
| `CreditCardStatementController` | `/api/v1/statements` | upload, parse, parsed-rows, confirm, list, getById |
| `LlmConfigController` | `/api/v1/llm-config` | getConfig, saveConfig, getModels, mapping CRUD |
| `AdminLlmModelController` | `/api/v1/admin/llm-models` | CRUD for LLM models (admin-only) |

### 9.7 Notifications

All steps fire notifications via `NotificationHelper`:
- Step 1: INFO — "Statement Uploaded" with extracted text confirmation
- Step 2: SUCCESS — "AI Parsing Complete" with parsed/duplicate counts
- Step 2 (error): ERROR — "AI Parsing Failed" with error message
- Step 4: SUCCESS — "Import Complete" with imported count and account name

---

## 10. Structured Logging & Request Tracing

### 10.1 Overview

The backend uses a 3-layer structured logging architecture with MDC (Mapped Diagnostic Context). Every log line includes `requestId`, `userId`, HTTP `method`, and `uri` — enabling filtering and correlation in log management tools (ELK, Loki, Datadog).

**Full documentation:** `docs/LOGGING.md`

### 10.2 MDC Keys

| Key | Set By | Example | Purpose |
|-----|--------|---------|---------|
| `requestId` | `MdcFilter` | `a3f8c1e2` | Correlate all logs in one request |
| `userId` | `JwtAuthFilter` | `admin` | Filter by authenticated user |
| `method` | `MdcFilter` | `POST` | Filter by HTTP method |
| `uri` | `MdcFilter` | `/api/v1/transactions` | Filter by endpoint |
| `clientIp` | `MdcFilter` | `192.168.1.5` | Track client origin |

### 10.3 Infrastructure Components

| Component | File | Purpose |
|-----------|------|---------|
| `MdcFilter` | `common/filter/MdcFilter.java` | Servlet filter (`HIGHEST_PRECEDENCE`) — sets MDC keys, clears in `finally` |
| `JwtAuthFilter` (enhanced) | `common/filter/JwtAuthFilter.java` | Adds `MDC.put("userId")` after JWT validation |
| `RequestLoggingInterceptor` | `common/config/RequestLoggingInterceptor.java` | Spring `HandlerInterceptor` — logs `>> METHOD /uri` on entry, `<< METHOD /uri status=N time=Xms` on exit |
| `logback-spring.xml` | `src/main/resources/logback-spring.xml` | Logback config with MDC pattern, dev/prod/default profiles |

### 10.4 Logback Profiles

| Profile | App Level | Root Level | Output Format |
|---------|-----------|------------|---------------|
| `dev` | DEBUG | INFO | Console (human-readable) |
| `prod` | INFO | WARN | JSON (for log aggregation) |
| `default` | INFO | INFO | Console (human-readable) |

### 10.5 Log Pattern

```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{requestId}] [%X{userId:-anonymous}] [%X{method} %X{uri}] %logger{36} - %msg%n
```

Example:
```
2026-02-24 14:30:22.123 [http-nio-5500-exec-1] INFO  [a3f8c1e2] [admin] [POST /api/v1/transactions] c.m.a.t.s.TransactionServiceImpl - Transaction created: id=42, type=EXPENSE, amount=150.00
```

### 10.6 Service Logging Policy

- **Write operations (create/update/delete):** `log.info(...)` with entity ID and key attributes
- **Auth events:** `log.info(...)` for success, `log.warn(...)` for failures
- **Read operations:** No logging (noise reduction)
- **Analytics queries:** `log.debug(...)` only (visible in dev profile)
- **Error paths:** `log.error(...)` with context on catch blocks

All service implementations use Lombok `@Slf4j`. MDC context is automatically included — no manual enrichment needed in service code.

### 10.7 Adding Logging to New Services

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class NewServiceImpl implements NewService {
    public Entity create(Request request, Long userId) {
        Entity saved = repository.save(entity);
        log.info("Entity created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }
}
```

---

## 11. Splits Feature

### 11.1 Friend Package (`com.minted.api.friend`)

Manages the user's friend list for split transactions. Follows the soft-delete pattern.

**Entity:** `Friend.java`
- `@ManyToOne` to User (FK user_id, ON DELETE CASCADE)
- Soft delete via `isActive` boolean
- Fields: id, name, email (nullable), phone (nullable), avatarColor (default `#6366f1`)
- Unique constraint: `uk_user_friend_name(user_id, name)`

**Repository:** `FriendRepository.java`
- `findByUserIdAndIsActiveTrue()` — list active friends
- `findByIdAndUserId()` — single friend scoped to user
- `existsByNameAndUserIdAndIsActiveTrue()` — duplicate check
- `findByNameAndUserIdAndIsActiveFalse()` — soft-delete restore

**Service:** `FriendServiceImpl.java`
- `create()` — checks for soft-deleted friend with same name first (restores), then checks duplicate active, then creates
- `update()` — validates name uniqueness if changed
- `delete()` — soft-delete (sets isActive=false)

**Controller:** `FriendController.java` at `/api/v1/friends`
- Standard CRUD: GET (list), GET/{id}, POST, PUT/{id}, DELETE/{id}

### 11.2 Split Package (`com.minted.api.split`)

**Enum:** `SplitType` — EQUAL, UNEQUAL, SHARE

**Entity:** `SplitTransaction.java`
- `@ManyToOne` to User and Transaction (source, nullable, ON DELETE SET NULL)
- `@Enumerated(EnumType.STRING)` + `@JdbcTypeCode(Types.VARCHAR)` on splitType (matches VARCHAR column in Flyway)
- `@OneToMany(cascade = ALL, orphanRemoval = true)` to SplitShare
- Fields: description, categoryName (stored as string, not FK), totalAmount, splitType, transactionDate, isSettled (denormalized)

**Entity:** `SplitShare.java`
- `@ManyToOne` to SplitTransaction (NOT NULL, ON DELETE CASCADE)
- `@ManyToOne` to Friend (nullable — null means "Me"/the authenticated user)
- Fields: shareAmount, sharePercentage (nullable), isPayer, isSettled, settledAt

**Repository Queries:**
- `SplitTransactionRepository`: sumOwedToUser (JPQL), sumUserOwes (JPQL), findSourceTransactionIdsByUserId (returns List<Long> of source transaction IDs that have been split)
- `SplitShareRepository`: findUnsettledBalancesByUserId (GROUP BY friend, net balance), findUnsettledByUserIdAndFriendId

**Service:** `SplitServiceImpl.java`
- `create()` — validates shares sum = totalAmount (or auto-calculates for EQUAL). For EQUAL: divides evenly with remainder to first share.
- `update()` — clears shares via orphanRemoval, rebuilds from request
- `settleFriend()` — marks all unsettled shares for a friend as settled, checks if parent split_transaction is fully settled, sends notification via NotificationHelper
- `getBalanceSummary()` — JPQL aggregation of owed/owing amounts
- `getFriendBalances()` — GROUP BY friend with net balance per friend

**Controller:** `SplitTransactionController.java` at `/api/v1/splits`
- CRUD: GET, GET/{id}, POST, PUT/{id}, DELETE/{id}
- Analytics: GET /summary, GET /balances
- Settlement: POST /settle
- Export: GET /friend/{friendId}/shares

### 11.3 Database Migration

**`V0_0_31__create_friends_and_splits_tables.sql`**
- `friends` table: user-scoped, soft-delete, unique(user_id, name)
- `split_transactions` table: FK to users (CASCADE) and transactions (SET NULL)
- `split_shares` table: FK to split_transactions (CASCADE) and friends (SET NULL)
- Indexes on: user_id, is_settled, transaction_date, friend_id, split_transaction_id
