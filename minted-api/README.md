# Minted API — Spring Boot Backend

The backend of Minted, built with **Java 17** and **Spring Boot 3.2**. Provides RESTful APIs for authentication, transactions, accounts, budgets, analytics, notifications, bulk import, credit card statement parsing, and admin management.

---

## Quick Start

### 1. Prerequisites

- Java 17
- MySQL 8.x (or use Docker)
- Gradle 8.x (wrapper included)

### 2. Environment Variables

```bash
# Required
export MINTED_DB_HOST=localhost
export MINTED_DB_PORT=3306
export MINTED_DB_NAME=minted_db
export MINTED_DB_USER=root
export MINTED_DB_PASSWORD=your_password
export MINTED_JWT_SECRET=your-256-bit-secret-key
export MINTED_JASYPT_PASSWORD=your-jasypt-password
```

> **Windows PowerShell:** Use `$env:VARIABLE_NAME = "value"` syntax.

### 3. Database Setup

```sql
CREATE DATABASE minted_db;
```

### 4. Run Migrations & Start

```bash
# Apply Flyway migrations
./gradlew flywayMigrate

# Start the server (port 5500)
./gradlew bootRun
```

---

## API Documentation

Swagger UI is available at: [http://localhost:5500/swagger-ui](http://localhost:5500/swagger-ui)

### API Endpoints

| Group                | Endpoint Prefix                | Description                                  |
| -------------------- | ------------------------------ | -------------------------------------------- |
| **Auth**             | `/api/v1/auth/*`               | Login, signup, refresh token, change password |
| **User Profile**     | `/api/v1/profile/*`            | User profile management                     |
| **Accounts**         | `/api/v1/accounts/*`           | CRUD for bank accounts / wallets             |
| **Account Types**    | `/api/v1/account-types/*`      | CRUD for account types                       |
| **Categories**       | `/api/v1/categories/*`         | CRUD for transaction categories              |
| **Transactions**     | `/api/v1/transactions/*`       | CRUD with filtering and pagination           |
| **Budgets**          | `/api/v1/budgets/*`            | Monthly budget management                    |
| **Dashboard**        | `/api/v1/dashboard/*`          | Configurable chart cards                     |
| **Analytics**        | `/api/v1/analytics/*`          | Summary, trends, category-wise, spending     |
| **Recurring**        | `/api/v1/recurring-transactions/*` | Recurring transaction management         |
| **Notifications**    | `/api/v1/notifications/*`      | User notification management                 |
| **Bulk Import**      | `/api/v1/imports/*`            | CSV bulk transaction import                  |
| **Statements**       | `/api/v1/statements/*`         | Credit card statement parsing (LLM-powered)  |
| **LLM Config**       | `/api/v1/llm-config/*`         | LLM settings and merchant mappings           |
| **Admin**            | `/api/v1/admin/*`              | User management, jobs, settings, defaults    |

---

## Architecture

The backend uses a **feature-based module structure** where each domain's controller, DTOs, entity, repository, and service live together.

```
src/main/java/com/minted/api/
├── MintedApiApplication.java
│
├── common/                    # Shared infrastructure
│   ├── config/                #   SecurityConfig, SchedulerConfig, DataInitializer
│   ├── exception/             #   Custom exceptions + GlobalExceptionHandler
│   ├── filter/                #   JwtAuthFilter
│   └── util/                  #   JwtUtil
│
├── auth/                      # Authentication & signup
│   ├── controller/            #   AuthController
│   ├── dto/                   #   LoginRequest/Response, SignupRequest, etc.
│   └── service/               #   AuthService, CustomUserDetailsService
│
├── user/                      # User entity & profile
│   ├── controller/            #   UserProfileController
│   ├── dto/                   #   UserResponse, UserProfileUpdateRequest
│   ├── entity/                #   User
│   ├── enums/                 #   UserRole
│   ├── repository/            #   UserRepository
│   └── service/               #   UserProfileService
│
├── account/                   # Accounts + Account Types
│   ├── controller/            #   AccountController, AccountTypeController
│   ├── dto/                   #   Account*Request/Response, AccountType*Request/Response
│   ├── entity/                #   Account, AccountType
│   ├── repository/            #   AccountRepository, AccountTypeRepository
│   └── service/               #   AccountService, AccountTypeService + impls
│
├── transaction/               # Transactions + Categories
│   ├── controller/            #   TransactionController, TransactionCategoryController
│   ├── dto/                   #   Transaction*Request/Response, Category*Request/Response
│   ├── entity/                #   Transaction, TransactionCategory
│   ├── enums/                 #   TransactionType
│   ├── repository/            #   TransactionRepository, TransactionCategoryRepository
│   └── service/               #   TransactionService, TransactionCategoryService + impls
│
├── budget/                    # Budget management
│   ├── controller/            #   BudgetController
│   ├── dto/                   #   BudgetRequest, BudgetResponse
│   ├── entity/                #   Budget
│   ├── repository/            #   BudgetRepository
│   └── service/               #   BudgetService + impl
│
├── dashboard/                 # Dashboard cards & charts
│   ├── controller/            #   DashboardCardController
│   ├── dto/                   #   DashboardCardRequest/Response, ChartDataResponse
│   ├── entity/                #   DashboardCard
│   ├── enums/                 #   ChartType, CardWidth
│   ├── repository/            #   DashboardCardRepository
│   └── service/               #   DashboardCardService + impl
│
├── analytics/                 # Analytics & reporting
│   ├── controller/            #   AnalyticsController
│   ├── dto/                   #   AnalyticsSummaryResponse, TrendResponse, etc.
│   └── service/               #   AnalyticsService + impl
│
├── recurring/                 # Recurring transactions
│   ├── controller/            #   RecurringTransactionController
│   ├── dto/                   #   RecurringTransactionRequest/Response
│   ├── entity/                #   RecurringTransaction
│   ├── enums/                 #   RecurringFrequency, RecurringStatus
│   ├── job/                   #   RecurringTransactionJob
│   ├── repository/            #   RecurringTransactionRepository
│   └── service/               #   RecurringTransactionService + impl
│
├── notification/              # Notification system
│   ├── controller/            #   NotificationController
│   ├── dto/                   #   NotificationResponse
│   ├── entity/                #   Notification
│   ├── enums/                 #   NotificationType
│   ├── repository/            #   NotificationRepository
│   └── service/               #   NotificationService, NotificationHelper + impl
│
├── job/                       # Job execution framework (shared)
│   ├── dto/                   #   JobExecutionResponse, JobScheduleConfig*
│   ├── entity/                #   JobExecution, JobScheduleConfig, JobStepExecution
│   ├── enums/                 #   JobStatus, JobStepStatus, JobTriggerType
│   ├── repository/            #   JobExecutionRepository, JobScheduleConfig*, JobStepExecution*
│   └── service/               #   JobExecutionService, JobSchedulerService + impls
│
├── bulkimport/                # CSV bulk import
│   ├── controller/            #   BulkImportController
│   ├── dto/                   #   BulkImportConfirmRequest, CsvRowPreview, etc.
│   ├── entity/                #   BulkImport
│   ├── enums/                 #   ImportStatus, ImportType
│   ├── job/                   #   BulkImportJob
│   ├── repository/            #   BulkImportRepository
│   └── service/               #   BulkImportService + impl
│
├── statement/                 # Credit card statement parser
│   ├── controller/            #   CreditCardStatementController
│   ├── dto/                   #   ConfirmStatementRequest, ParsedTransactionRow, etc.
│   ├── entity/                #   CreditCardStatement
│   ├── enums/                 #   StatementStatus
│   ├── repository/            #   CreditCardStatementRepository
│   └── service/               #   CreditCardStatementService, StatementParserService + impls
│
├── llm/                       # LLM config, models, merchant mappings
│   ├── controller/            #   LlmConfigController, AdminLlmModelController
│   ├── dto/                   #   LlmConfig*, LlmModel*, MerchantMapping*
│   ├── entity/                #   LlmModel, LlmConfiguration, MerchantCategoryMapping
│   ├── repository/            #   LlmModelRepository, LlmConfiguration*, MerchantCategoryMapping*
│   └── service/               #   LlmService, GeminiLlmService, LlmConfigService, MerchantMappingService + impls
│
└── admin/                     # Admin management
    ├── controller/            #   AdminController
    ├── dto/                   #   AdminUserResponse, CreateUserRequest, Default*Request/Response, etc.
    ├── entity/                #   DefaultCategory, DefaultAccountType, SystemSetting
    ├── repository/            #   DefaultCategoryRepository, DefaultAccountType*, SystemSetting*
    └── service/               #   DefaultListsService, SystemSettingService, UserManagementService + impls

src/main/resources/
├── application.properties     # Externalized config via env vars
└── db/migration/              # Flyway versioned SQL migrations (V0_0_1 through V0_0_27)
```

### Design Principles

- **Feature-based modules** — Each domain's files live together (controller, dto, entity, repo, service)
- **DTOs only** — JPA entities are never exposed in API responses
- **Flyway migrations** — All schema changes go through versioned SQL files (`validate` mode)
- **Stateless auth** — JWT access tokens with configurable expiry
- **Environment variables** — No hardcoded secrets; all config externalized
- **Shared infrastructure** — Cross-cutting concerns (security, exceptions, JWT) in `common/`

### Key Cross-Module Dependencies

- `user.entity.User` is referenced by nearly every module
- `notification.service.NotificationHelper` is used by auth, bulkimport, statement, admin
- `admin.service.SystemSettingService` is used by auth, statement, llm
- `job.*` entities/services are used by bulkimport and statement for execution tracking

---

## Database Migrations

Flyway manages all schema changes. Migration files are in `src/main/resources/db/migration/`:

```bash
# Apply pending migrations
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo
```

---

## Docker

The API is containerized as a multi-stage build:

1. **Stage 1 (Gradle 8.5 + JDK 17):** Downloads dependencies, builds the JAR
2. **Stage 2 (Eclipse Temurin JRE 17):** Runs as non-root `spring` user

```bash
# Build image standalone
docker build -t minted-api .

# Or use the full stack via root docker-compose.yml
docker compose up --build
```

See the root [docker-compose.yml](../docker-compose.yml) for full orchestration with MySQL and frontend.

---

## More Info

- [Root README](../README.md) — Full project overview and setup
- [Frontend README](../minted-web/README.md) — Angular app documentation
- [API Spec](../docs/API_SPEC.md) — Detailed endpoint specification
- [Backend Spec](../docs/BACKEND_SPEC.md) — Architecture & design decisions
