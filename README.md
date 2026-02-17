# Minted - Personal Budget & Expense Management

> **Status:** Phase 1 Complete - Projects Scaffolded
> **Current Phase:** Phase 2 - Authentication Backend

## Overview

Minted is a full-stack personal finance management application that helps users:
- Track income, expenses, and transfers across multiple accounts
- Categorize transactions with custom categories and icons
- Set and monitor budgets by category and time period
- Visualize spending patterns with configurable dashboard charts
- Manage accounts, account types, categories, and budgets

## Tech Stack

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot 3.2.x
- **Build Tool:** Gradle 8.5
- **Database:** MySQL 8.x
- **Migrations:** Flyway (versioned SQL)
- **Authentication:** JWT (jjwt 0.12.x)
- **Encryption:** Jasypt Spring Boot 3.x
- **API Docs:** SpringDoc OpenAPI (Swagger UI)

### Frontend
- **Framework:** Angular 21 (non-standalone components)
- **UI Library:** PrimeNG
- **Data Grid:** AG Grid Community
- **CSS Framework:** Tailwind CSS 3.x
- **Icons:** Font Awesome
- **Charts:** Chart.js (via PrimeNG Charts)

## Project Structure

```
minted/
├── minted-api/              # Spring Boot backend (port 5500)
│   ├── src/main/java/com/minted/api/
│   └── src/main/resources/
│       └── db/migration/    # Flyway SQL migrations
│
├── minted-web/              # Angular frontend (port 4200)
│   └── src/app/
│       ├── core/            # Services, guards, interceptors
│       ├── shared/          # Shared components
│       ├── modules/         # Feature modules
│       └── layout/          # App layout
│
└── Documentation/
    ├── CLAUDE.md            # Master instructions for Claude
    ├── DEVELOPMENT_PROCESS.md
    ├── MISTAKES.md          # Error log & prevention
    ├── QUICKSTART.md        # Implementation phases
    ├── BACKEND_SPEC.md
    ├── FRONTEND_SPEC.md
    ├── UI_UX_SPEC.md
    ├── API_SPEC.md
    └── STITCH_UI_REFERENCE.md
```

## Quick Start

### Backend
```bash
cd minted-api

# Set environment variables (see minted-api/README.md)
export MINTED_DB_HOST=localhost
export MINTED_DB_PORT=3306
export MINTED_DB_NAME=minted_db
export MINTED_DB_USER=root
export MINTED_DB_PASSWORD=your_password
export MINTED_JWT_SECRET=your-256-bit-secret
export MINTED_JASYPT_PASSWORD=your-jasypt-password

# Create database
mysql -u root -p -e "CREATE DATABASE minted_db;"

# Run migrations (Phase 2+)
./gradlew flywayMigrate

# Start server
./gradlew bootRun
```

Access Swagger UI at: `http://localhost:5500/swagger-ui`

### Frontend
```bash
cd minted-web

# Install dependencies
npm install

# Start dev server
npm start
# OR
ng serve
```

Access app at: `http://localhost:4200`

## Implementation Status

### ✅ Phase 1: Project Scaffolding (COMPLETE)
- [x] Backend: Spring Boot project with Gradle
- [x] Frontend: Angular project with all dependencies
- [x] Both projects compile and run

### ✅ Phase 2: Authentication Backend (COMPLETE ✅)
- [x] Flyway migration for users table (V0_0_1, V0_0_2)
- [x] User entity, repository, service
- [x] JWT utilities (jjwt 0.12.6) and filters
- [x] Security configuration (Spring Security + BCrypt)
- [x] Auth controller (/login, /refresh, /change-password)
- [x] DTOs and custom exceptions
- [x] Global exception handler
- [x] Build successful and tested ✅
- [x] Login endpoint verified with admin/admin credentials

### ✅ Phase 3: Authentication (Frontend) COMPLETE ✅
- [x] Core module (services, guards, interceptors)
  - [x] AuthService with login, logout, token management
  - [x] AuthGuard for route protection
  - [x] JwtInterceptor for attaching tokens
  - [x] ErrorInterceptor for handling 401/403
- [x] Auth module (login component with PrimeNG)
- [x] Routing with lazy loading
- [x] Build successful and dev server running
- [ ] Layout module (sidebar, header) - Deferred to Phase 5

### ⏳ Phase 4-8: Core Features (Pending)
See `QUICKSTART.md` for detailed phase breakdown.

## Reference Repositories

- **Backend Reference:** [wheremybuckgoes](https://github.com/kartikeychoudhary/wheremybuckgoes)
- **Frontend Reference:** [expense_track](https://github.com/kartikeychoudhary/expense_track)

## UI/UX Design Reference

- **Stitch Project:** https://stitch.withgoogle.com/projects/13720741124727703321
- **Theme:** Light mode, Inter font, #c48821 accent, 8px roundness
- **Screens:** 6 desktop screens (1280x1024)

## Development Guidelines

### Before Starting ANY Task
1. Read `MISTAKES.md` in full
2. Read relevant spec file (`BACKEND_SPEC.md`, `FRONTEND_SPEC.md`, etc.)
3. Ensure project builds cleanly

### Critical Rules
1. **NEVER** generate standalone Angular components (use `--standalone=false`)
2. **NEVER** add libraries outside approved tech stack
3. **NEVER** use Hibernate auto-DDL (always use Flyway with `validate`)
4. **NEVER** expose JPA entities in REST responses (always use DTOs)
5. **NEVER** hardcode secrets (always use environment variables)

### Development Process
1. **Backend First:** Database → Entity → Repository → DTO → Service → Controller
2. **Frontend:** Model → API Service → Module → Component → Template
3. **Integration:** Test full flow, check responsive design, verify error handling

## Contributing

This project follows a strict specification-driven development process. All code must:
- Follow the tech stack exactly (no substitutions)
- Use non-standalone Angular components
- Pass both backend and frontend builds
- Be responsive across all breakpoints
- Handle errors gracefully

## License

Proprietary - Kartikey Choudhary © 2026
