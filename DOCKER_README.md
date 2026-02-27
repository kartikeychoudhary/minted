# Minted — Docker Deployment Guide

This guide explains all the ways to run Minted using Docker and Docker Compose.

---

## 📋 Compose Files at a Glance

| File | Method | Use Case |
|------|--------|----------|
| `docker-compose.prod.yml` | Pull pre-built images from Docker Hub | **Recommended** — fastest, no build tools needed |
| `docker-compose.yml` | Build images from source | Development or customising the code |
| `docker-compose.signoz.yml` | Build with OpenTelemetry agent | Observability / tracing with SigNoz |

---

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+ (`docker compose` — note: no hyphen)
- At least 2 GB of available RAM
- At least 5 GB of available disk space

---

## 🚀 Option 1: Docker Hub Images (Recommended)

Pull and run the pre-built images from Docker Hub — **no source code, no build tools required**.

### 1. Get the files

```bash
curl -O https://raw.githubusercontent.com/kartikeychoudhary/minted/main/docker-compose.prod.yml
curl -O https://raw.githubusercontent.com/kartikeychoudhary/minted/main/.env.example
cp .env.example .env
```

### 2. Set your secrets in `.env`

At minimum, change these three values before starting:

```bash
MINTED_JWT_SECRET=$(openssl rand -base64 64)
MINTED_JASYPT_PASSWORD=$(openssl rand -base64 32)
MINTED_ENCRYPTION_KEY=$(openssl rand -base64 32)
```

### 3. Start the stack

```bash
# Always pull the latest images and start
docker compose -f docker-compose.prod.yml up -d

# Or pin to a specific release (recommended for production)
MINTED_VERSION=v1.0.0 docker compose -f docker-compose.prod.yml up -d
```

> You can also set `MINTED_VERSION=v1.0.0` in your `.env` file to pin permanently.

### 4. Access the application

Once healthy (~60 seconds), open your browser:

| Service    | URL |
| ---------- | --- |
| Frontend   | http://localhost |
| API        | http://localhost:5500/api/v1 |
| Swagger UI | http://localhost:5500/swagger-ui.html |

**Default credentials:** `admin` / `admin` — you will be **forced to change the password on first login**.

### 5. Stop the stack

```bash
docker compose -f docker-compose.prod.yml down        # stops containers, preserves database
docker compose -f docker-compose.prod.yml down -v     # ⚠️ stops AND deletes the database volume
```

### Updating to a New Release (Hub Images)

```bash
# Pull the new images
docker compose -f docker-compose.prod.yml pull

# Restart with the new images
docker compose -f docker-compose.prod.yml up -d

# Or update to a specific version
MINTED_VERSION=v1.1.0 docker compose -f docker-compose.prod.yml up -d
```

---

## 🔨 Option 2: Build from Source

Use this if you have cloned the repository and want to build images locally (development or custom changes).

### 1. Clone and configure

```bash
git clone https://github.com/kartikeychoudhary/minted.git
cd minted
cp .env.example .env
# Edit .env and set MINTED_JWT_SECRET, MINTED_JASYPT_PASSWORD, etc.
```

### 2. Build and start

```bash
docker compose up -d --build
```

This will:
- Start **MySQL 8** with a health check
- Build and start the **Spring Boot API** (waits for MySQL)
- Build and start the **Angular app** served via **Nginx** (waits for API)

### 3. Access the application

| Service    | URL |
| ---------- | --- |
| Frontend   | http://localhost |
| API        | http://localhost:5500/api/v1 |
| Swagger UI | http://localhost:5500/swagger-ui.html |

### 4. Stop the stack

```bash
docker compose down        # preserves database
docker compose down -v     # ⚠️ deletes the database volume
```

### Updating (Built from Source)

```bash
git pull origin main
docker compose down
docker compose up -d --build
docker compose ps
```

### Troubleshooting: `Permission denied on ./gradlew` (Linux/macOS)

```bash
chmod +x minted-api/gradlew
docker compose up -d --build
```

---

## ⚙️ Option 3: SigNoz Observability

Run the backend with OpenTelemetry instrumentation, sending traces, metrics, and logs to an existing [SigNoz](https://signoz.io/) instance.

### Prerequisites

- A running SigNoz instance reachable from the Docker host
- The SigNoz OTLP collector endpoint (default: `http://localhost:4317`)

### Start

```bash
docker compose -f docker-compose.signoz.yml up -d --build
```

This uses `minted-api/Dockerfile.signoz`, which downloads the OpenTelemetry Java agent at build time.

### Configuration

Add these to your `.env` file (all optional):

| Variable | Default | Description |
|----------|---------|-------------|
| `OTEL_SERVICE_NAME` | `minted-backend` | Service name in SigNoz |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://host.docker.internal:4317` | OTLP collector endpoint |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | `grpc` | `grpc` or `http/protobuf` |
| `OTEL_RESOURCE_ATTRIBUTES` | `deployment.environment=docker,...` | Additional resource labels |
| `OTEL_METRICS_EXPORTER` | `otlp` | `otlp` or `none` |
| `OTEL_LOGS_EXPORTER` | `otlp` | `otlp` or `none` |
| `OTEL_TRACES_EXPORTER` | `otlp` | `otlp` or `none` |

```bash
# Example: send only traces, no metrics/logs
OTEL_METRICS_EXPORTER=none
OTEL_LOGS_EXPORTER=none
```

---

## Environment Variables

### Database

| Variable | Default | Description |
|----------|---------|-------------|
| `MINTED_DB_ROOT_PASSWORD` | `rootroot` | MySQL root password |
| `MINTED_DB_NAME` | `minted_db` | Database name |
| `MINTED_DB_USER` | `minted_user` | Database user |
| `MINTED_DB_PASSWORD` | `minted_pass` | Database password |
| `MINTED_DB_PORT` | `3306` | Host port for MySQL |

### Backend

| Variable | Default | Description |
|----------|---------|-------------|
| `BACKEND_PORT` | `5500` | Host port for the API |
| `MINTED_JWT_SECRET` | ⚠️ Required | JWT signing secret (min 256-bit base64) |
| `MINTED_JWT_EXPIRATION` | `86400000` | JWT expiry in ms (24 h) |
| `MINTED_JASYPT_PASSWORD` | ⚠️ Change in prod | Jasypt encryption password |
| `MINTED_ENCRYPTION_KEY` | Falls back to Jasypt | AES-256 key for DB-stored API keys |
| `MINTED_CORS_ORIGINS` | `http://localhost:80,...` | Comma-separated allowed CORS origins |
| `LOG_LEVEL` | `INFO` | App log level |
| `SECURITY_LOG_LEVEL` | `INFO` | Security log level |
| `JPA_SHOW_SQL` | `false` | Log SQL queries |

### Frontend

| Variable | Default | Description |
|----------|---------|-------------|
| `FRONTEND_PORT` | `80` | Host port for the web UI |

### Image Version (prod compose only)

| Variable | Default | Description |
|----------|---------|-------------|
| `MINTED_VERSION` | `latest` | Docker Hub image tag — e.g. `v1.0.0` |

### Generate secure values

```bash
openssl rand -base64 64   # MINTED_JWT_SECRET
openssl rand -base64 32   # MINTED_JASYPT_PASSWORD
openssl rand -base64 32   # MINTED_ENCRYPTION_KEY
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Host Machine                        │
│                                                         │
│  ┌──────────┐      ┌──────────┐      ┌──────────┐      │
│  │ Frontend │──────│ Backend  │──────│  MySQL   │      │
│  │  (Nginx) │      │  (Java)  │      │  (DB)    │      │
│  │ Port 80  │      │ Port 5500│      │ Port 3306│      │
│  └──────────┘      └──────────┘      └──────────┘      │
│       │                  │                  │            │
│       └──────────────────┴──────────────────┘           │
│              minted-network (Bridge)                    │
└─────────────────────────────────────────────────────────┘
```

- Frontend Nginx proxies `/api/*` requests to the backend internally
- Backend connects to MySQL using the internal hostname `mysql:3306`
- All services communicate over the `minted-network` bridge network
- Only the ports defined in `.env` are exposed to the host

---

## Docker Commands Reference

### View service status

```bash
docker compose -f docker-compose.prod.yml ps
```

### View logs

```bash
docker compose -f docker-compose.prod.yml logs -f
docker compose -f docker-compose.prod.yml logs -f backend
docker compose -f docker-compose.prod.yml logs -f frontend
docker compose -f docker-compose.prod.yml logs -f mysql
```

### Restart a service

```bash
docker compose -f docker-compose.prod.yml restart backend
```

### Access a container shell

```bash
docker compose -f docker-compose.prod.yml exec backend sh
docker compose -f docker-compose.prod.yml exec frontend sh
docker compose -f docker-compose.prod.yml exec mysql bash
```

---

## Data Persistence

Database data is stored in a named Docker volume: **`minted_mysql_data`**

This volume persists across `docker compose down` and rebuilds. It is only deleted when you explicitly run `down -v`.

```bash
# List volumes
docker volume ls | grep minted

# Backup database
docker compose exec mysql mysqldump -u root -p minted_db > backup.sql

# Restore database
docker compose exec -T mysql mysql -u root -p minted_db < backup.sql
```

---

## Troubleshooting

### Services Not Starting — Check Port Conflicts

```bash
netstat -ano | findstr :80      # Windows
netstat -ano | findstr :5500
netstat -ano | grep :80         # Linux/macOS
```

### Health Check Failing

Services may take up to 90 seconds on first startup. Check:

```bash
docker compose -f docker-compose.prod.yml ps
```

All services should show `healthy`.

### Database Connection Issues

```bash
docker compose -f docker-compose.prod.yml logs mysql
docker compose -f docker-compose.prod.yml ps mysql
```

Verify that the DB credentials in `.env` match what the backend expects.

### Backend Startup Errors

```bash
# Check Flyway migrations
docker compose -f docker-compose.prod.yml logs backend | grep Flyway

# Check env vars were applied
docker compose -f docker-compose.prod.yml exec backend env | grep MINTED
```

### Frontend Not Loading

```bash
docker compose -f docker-compose.prod.yml logs frontend
docker compose -f docker-compose.prod.yml exec frontend cat /etc/nginx/conf.d/default.conf
```

---

## Uninstalling

```bash
# Stop and remove containers (keeps database)
docker compose -f docker-compose.prod.yml down

# Remove containers AND database volume
docker compose -f docker-compose.prod.yml down -v

# Remove pulled images
docker rmi kartikey31choudhary/minted-frontend:latest
docker rmi kartikey31choudhary/minted-backend:latest

# Remove network (if still present)
docker network rm minted-network
```

---

## Support

- **GitHub Issues:** https://github.com/kartikeychoudhary/minted/issues
- **Docker Hub:** https://hub.docker.com/u/kartikey31choudhary
- **Documentation:** See the main [README.md](./README.md)

## License

Proprietary — Kartikey Choudhary © 2026
