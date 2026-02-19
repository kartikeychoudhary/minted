# 💰 Minted — Personal Budget & Expense Management

A modern, full-stack personal finance application built with **Angular 21** and **Spring Boot 3**. Track income, expenses, and transfers across multiple accounts with a beautiful dark/light themed UI.

---

## ✨ Features

- **Transaction Management** — Add, edit, and delete income, expenses, and transfers
- **Multi-Account Support** — Track balances across bank accounts, wallets, and credit cards
- **Custom Categories** — Organize transactions with custom categories and icons
- **Dashboard Analytics** — Visualize spending patterns with interactive charts
- **Dark / Light Theme** — System-aware dark mode with customizable accent colors
- **JWT Authentication** — Secure login with token-based auth and auto-refresh
- **AG Grid Data Tables** — Powerful, sortable, filterable transaction grids
- **Responsive Design** — Works on desktop and tablet viewports
- **Docker Ready** — One-command deployment with Docker Compose

---

## 🖥 Screenshots

> _Coming soon_

---

## 🏗 Tech Stack

| Layer       | Technology                                         |
| ----------- | -------------------------------------------------- |
| **Frontend** | Angular 21, PrimeNG, AG Grid, Tailwind CSS, Chart.js |
| **Backend**  | Java 17, Spring Boot 3.2, Spring Security, Flyway   |
| **Database** | MySQL 8.0                                           |
| **Auth**     | JWT (jjwt), Jasypt encryption                       |
| **DevOps**   | Docker, Docker Compose, Nginx                       |

---

## 📁 Project Structure

```
minted/
├── minted-api/          # Spring Boot backend → see minted-api/README.md
├── minted-web/          # Angular frontend   → see minted-web/README.md
├── docker-compose.yml   # Full-stack Docker orchestration
├── Dockerfile           # Frontend multi-stage build (Node → Nginx)
├── nginx.conf           # Nginx reverse proxy config
└── Documentation/       # Specs, phases, and design references
```

---

## 🚀 Getting Started

### Prerequisites

| Tool           | Version | Notes                          |
| -------------- | ------- | ------------------------------ |
| Node.js        | 20+     | For frontend                   |
| Java           | 17      | For backend                    |
| MySQL          | 8.x     | Or use Docker                  |
| Angular CLI    | 21      | `npm i -g @angular/cli`        |
| Docker         | 24+     | _Optional — for Docker setup_  |

---

### Option A: Run Locally

#### 1. Clone the repository

```bash
git clone https://github.com/kartikeychoudhary/minted.git
cd minted
```

#### 2. Start MySQL

Make sure MySQL 8 is running locally. Create the database:

```sql
CREATE DATABASE minted_db;
```

#### 3. Start the Backend

```bash
cd minted-api

# Set environment variables
export MINTED_DB_HOST=localhost
export MINTED_DB_PORT=3306
export MINTED_DB_NAME=minted_db
export MINTED_DB_USER=root
export MINTED_DB_PASSWORD=your_password
export MINTED_JWT_SECRET=your-256-bit-secret-key-here
export MINTED_JASYPT_PASSWORD=your-jasypt-password

# Run database migrations
./gradlew flywayMigrate

# Start the server (port 5500)
./gradlew bootRun
```

> **Windows PowerShell:** Use `$env:MINTED_DB_HOST = "localhost"` instead of `export`.

Swagger UI: [http://localhost:5500/swagger-ui](http://localhost:5500/swagger-ui)

#### 4. Start the Frontend

```bash
cd minted-web

# Install dependencies
npm install

# Start dev server (port 4200)
npm start
```

App: [http://localhost:4200](http://localhost:4200)

#### 5. Default Credentials

| Username | Password |
| -------- | -------- |
| `admin`  | `admin`  |

---

### Option B: Run with Docker

The entire stack (MySQL + Backend + Frontend) can be launched with a single command.

#### 1. Create a `.env` file in the project root

```env
# Required
MINTED_JWT_SECRET=your-256-bit-secret-key-here

# Optional (shown with defaults)
MINTED_DB_ROOT_PASSWORD=rootroot
MINTED_DB_NAME=minted_db
MINTED_DB_USER=minted_user
MINTED_DB_PASSWORD=minted_pass
MINTED_JASYPT_PASSWORD=default-jasypt-password
BACKEND_PORT=5500
FRONTEND_PORT=80
```

#### 2. Build and start all services

```bash
docker compose up --build -d
```

This will:
- Start **MySQL 8** with a health check
- Build and start the **Spring Boot API** (waits for MySQL)
- Build and start the **Angular app** served via **Nginx** (waits for API)

#### 3. Access the app

| Service    | URL                                            |
| ---------- | ---------------------------------------------- |
| Frontend   | [http://localhost](http://localhost)             |
| Backend    | [http://localhost:5500](http://localhost:5500)   |
| Swagger UI | [http://localhost:5500/swagger-ui](http://localhost:5500/swagger-ui) |

#### 4. Stop the stack

```bash
# Stop containers (data is preserved)
docker compose down

# Stop AND delete database volume
docker compose down -v
```

#### Troubleshooting

**Permission denied on `./gradlew` (Linux/macOS)**
If you are running Docker on a Linux/macOS machine after cloning or copying the project from Windows, you might encounter a `Permission denied` error for the Gradle wrapper during the backend build. To fix this, grant execute permissions to the wrapper file:

```bash
chmod +x minted-api/gradlew
docker compose up --build -d
```

---

## 📖 Documentation

| Document                                                | Description                              |
| ------------------------------------------------------- | ---------------------------------------- |
| [minted-web/README.md](./minted-web/README.md)         | Frontend setup, architecture, theming    |
| [minted-api/README.md](./minted-api/README.md)         | Backend setup, API structure, migrations |
| [IMPLEMENTATION_STATUS.md](./IMPLEMENTATION_STATUS.md)  | Phase-by-phase progress tracker          |
| [Documentation/QUICKSTART.md](./Documentation/QUICKSTART.md) | Detailed implementation phases      |
| [Documentation/API_SPEC.md](./Documentation/API_SPEC.md) | REST API specification                 |
| [Documentation/BACKEND_SPEC.md](./Documentation/BACKEND_SPEC.md) | Backend architecture spec        |
| [Documentation/FRONTEND_SPEC.md](./Documentation/FRONTEND_SPEC.md) | Frontend architecture spec      |
| [Documentation/UI_UX_SPEC.md](./Documentation/UI_UX_SPEC.md) | UI/UX design reference              |

---

## 🎨 Theming

Minted uses a **CSS custom property** design system (`--minted-*` tokens) with:

- **Light mode** — Clean white surfaces with subtle borders
- **Dark mode** — Elevated slate palette (not pure black)
- **Accent colors** — 6 switchable presets (Amber, Emerald, Blue, Violet, Rose, Teal)
- **AG Grid v35 Theming API** — Data grid inherits all theme tokens automatically

---

## 📄 License

Proprietary — Kartikey Choudhary © 2026
