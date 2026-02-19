# Minted API — Spring Boot Backend

The backend of Minted, built with **Java 17** and **Spring Boot 3.2**. Provides RESTful APIs for authentication, transactions, accounts, categories, and dashboard analytics.

---

## 🚀 Quick Start

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

## 📘 API Documentation

Swagger UI is available at: [http://localhost:5500/swagger-ui](http://localhost:5500/swagger-ui)

### API Endpoints

| Group           | Endpoint Prefix           | Description                        |
| --------------- | ------------------------- | ---------------------------------- |
| **Auth**        | `/api/v1/auth/*`          | Login, refresh token, change password |
| **Accounts**    | `/api/v1/accounts/*`      | CRUD for bank accounts / wallets   |
| **Categories**  | `/api/v1/categories/*`    | CRUD for transaction categories    |
| **Transactions**| `/api/v1/transactions/*`  | CRUD with filtering and pagination |
| **Dashboard**   | `/api/v1/dashboard/*`     | Analytics aggregation endpoints    |

---

## 🏗 Architecture

```
src/main/java/com/minted/api/
├── config/             # Security, JWT, CORS, Jasypt configuration
├── controller/         # REST controllers (one per domain)
├── dto/                # Request/Response DTOs (no JPA entities exposed)
├── entity/             # JPA entities mapped to database tables
├── enums/              # TransactionType, AccountType, etc.
├── exception/          # Custom exceptions + GlobalExceptionHandler
├── filter/             # JWT authentication filter
├── repository/         # Spring Data JPA repositories
├── service/            # Business logic (interface + impl/)
└── util/               # JwtUtil, date helpers

src/main/resources/
├── application.properties  # Externalized config via env vars
└── db/migration/           # Flyway versioned SQL migrations (V0_0_1, etc.)
```

### Design Principles

- **DTOs only** — JPA entities are never exposed in API responses
- **Flyway migrations** — All schema changes go through versioned SQL files (`validate` mode)
- **Stateless auth** — JWT access tokens with configurable expiry
- **Environment variables** — No hardcoded secrets; all config externalized

---

## 🗄 Database Migrations

Flyway manages all schema changes. Migration files are in `src/main/resources/db/migration/`:

```bash
# Apply pending migrations
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo
```

---

## 🐳 Docker

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

## 📖 More Info

- [Root README](../README.md) — Full project overview and setup
- [Frontend README](../minted-web/README.md) — Angular app documentation
- [API Spec](../Documentation/API_SPEC.md) — Detailed endpoint specification
- [Backend Spec](../Documentation/BACKEND_SPEC.md) — Architecture & design decisions
