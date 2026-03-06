# BACKEND_SPEC.md — Minted API (Spring Boot Backend)

> Detailed feature docs have been moved to [docs/features/api/](features/api/README.md).
> This file covers project setup and cross-cutting concerns only.

---

## 1. Project Setup

### Tech Stack
| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Build | Gradle 8.x (Groovy DSL) |
| Database | MySQL 8.x |
| Migrations | Flyway (versioned SQL) |
| Auth | JWT — jjwt 0.12.x (HMAC-SHA512) |
| Encryption | Jasypt Spring Boot 3.x |
| API Docs | SpringDoc OpenAPI 2.x |
| PDF Parsing | Apache PDFBox 3.0.2 |
| ORM | Spring Data JPA / Hibernate 6.x |

### Gradle Dependencies (`build.gradle`)
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    runtimeOnly 'com.mysql:mysql-connector-j'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'
    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
    implementation 'com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
    implementation 'org.apache.pdfbox:pdfbox:3.0.2'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

### Environment Variables
| Variable | Description |
|----------|-------------|
| `MINTED_DB_HOST` / `MINTED_DB_PORT` / `MINTED_DB_NAME` | MySQL connection |
| `MINTED_DB_USER` / `MINTED_DB_PASSWORD` | DB credentials (password Jasypt-encrypted) |
| `MINTED_JWT_SECRET` | JWT signing key (min 256-bit) |
| `MINTED_JWT_EXPIRATION` | Token expiry in ms (default: `86400000` = 24h) |
| `MINTED_JASYPT_PASSWORD` | Jasypt encryption salt |
| `MINTED_CORS_ORIGINS` | Allowed CORS origins |
| `MINTED_ADMIN_PASSWORD` | Initial admin password (`ENC(encrypted_value)`) |

### Key `application.properties` Settings
```properties
server.port=5500
spring.jpa.hibernate.ddl-auto=validate        # Flyway manages schema
spring.flyway.locations=classpath:db/migration
app.jwt.secret=${MINTED_JWT_SECRET}
app.jwt.expiration=${MINTED_JWT_EXPIRATION:86400000}
app.cors.allowed-origins=${MINTED_CORS_ORIGINS:http://localhost:4200}
jasypt.encryptor.algorithm=PBEWithMD5AndDES
jasypt.encryptor.iv-generator-classname=org.jasypt.iv.NoIvGenerator
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui
```

---

## 2. Package Structure

Feature-based modules — each domain owns its controller, DTO, entity, repository, and service:

```
com.minted.api/
├── MintedApiApplication.java
├── common/          # SecurityConfig, SchedulerConfig, RequestLoggingInterceptor,
│                    # GlobalExceptionHandler, JwtAuthFilter, MdcFilter, JwtUtil
├── auth/            # Authentication & signup
├── user/            # User entity & profile (avatar)
├── account/         # Accounts + Account Types
├── transaction/     # Transactions + Categories
├── budget/          # Budget management
├── dashboard/       # Dashboard cards & charts
├── analytics/       # Analytics & reporting
├── recurring/       # Recurring transactions + job
├── notification/    # Notification system + NotificationHelper
├── job/             # Job execution framework (shared tables/services)
├── bulkimport/      # CSV bulk import + job
├── statement/       # Credit card statement parser
├── llm/             # LLM config, models, merchant mappings
├── friend/          # Friends list (for splits)
├── split/           # Split transactions
└── admin/           # Admin users, system settings, default lists
```

Internal module structure: `controller/` · `dto/` · `entity/` · `enums/` · `job/` · `repository/` · `service/`

---

## 3. Flyway Migration Map

| Migration | Feature |
|-----------|---------|
| V0_0_1 | users |
| V0_0_2–3 | account_types, accounts |
| V0_0_4–5 | transaction_categories, transactions |
| V0_0_6 | budgets |
| V0_0_7 | dashboard_cards |
| V0_0_16 | job_schedule_configs, job_executions, job_step_executions |
| V0_0_17 | system_settings |
| V0_0_20 | bulk_imports |
| V0_0_22 | notifications |
| V0_0_23–26 | llm_models, llm_configurations, credit_card_statements, merchant_category_mappings |
| V0_0_27 | system settings seed (parser + LLM key flags) |
| V0_0_30 | EMI default category |
| V0_0_31 | friends, split_transactions, split_shares |
| V0_0_35 | exclude_from_analysis on transactions |
| V0_0_36 | dashboard_configurations |
| V0_0_38–39 | avatar columns for users and friends |

Full migration map with feature links: [docs/features/api/README.md](features/api/README.md)

---

## 4. Feature Documentation

| Feature | Doc |
|---------|-----|
| Authentication & Signup | [features/api/auth.md](features/api/auth.md) |
| Accounts & Account Types | [features/api/accounts.md](features/api/accounts.md) |
| Transactions & Categories | [features/api/transactions.md](features/api/transactions.md) |
| Budgets | [features/api/budgets.md](features/api/budgets.md) |
| Analytics & Dashboard | [features/api/analytics.md](features/api/analytics.md) |
| Recurring Transactions | [features/api/recurring.md](features/api/recurring.md) |
| Notifications | [features/api/notifications.md](features/api/notifications.md) |
| Bulk CSV Import | [features/api/import.md](features/api/import.md) |
| Credit Card Statement Parser | [features/api/statements.md](features/api/statements.md) |
| Splits & Friends | [features/api/splits.md](features/api/splits.md) |
| Admin (Users, Jobs, Settings) | [features/api/admin.md](features/api/admin.md) |
| Infrastructure & Cross-Cutting | [features/api/infrastructure.md](features/api/infrastructure.md) |

---

## 5. Key Patterns & Rules

- **Never expose JPA entities in API responses** — always use DTOs
- **Data ownership** — every query filters by `userId` from JWT SecurityContext
- **Hibernate 6.x + MySQL VARCHAR enums** — use `@Enumerated(EnumType.STRING)` + `@JdbcTypeCode(Types.VARCHAR)`
- **Async job pattern** — `TransactionSynchronizationManager.registerSynchronization(afterCommit)` + `CompletableFuture.runAsync()` + `TransactionTemplate` (bypasses self-invocation proxy issue)
- **Notifications** — inject `NotificationHelper`, use `REQUIRES_NEW` propagation; never rethrow exceptions
- **Soft deletes** — accounts, account types, categories, friends use `is_active` flag; restore on name-collision create
- **Balance updates** — all transaction create/update/delete operations adjust `account.balance` atomically

For logging patterns: [docs/LOGGING.md](LOGGING.md)
