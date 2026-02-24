# Minted - Docker Deployment Guide

This guide explains how to run the Minted application using Docker and Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- At least 2GB of available RAM
- At least 5GB of available disk space

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/kartikeychoudhary/minted.git
cd minted
```

### 2. Configure Environment Variables

Copy the example environment file and customize it:

```bash
cp .env.example .env
```

**Important:** For production deployments, you MUST change the following values in `.env`:

```bash
# Generate a secure JWT secret
MINTED_JWT_SECRET=$(openssl rand -base64 64)

# Generate a secure Jasypt password
MINTED_JASYPT_PASSWORD=$(openssl rand -base64 32)

# Set strong database passwords
MINTED_DB_ROOT_PASSWORD=your_secure_root_password
MINTED_DB_PASSWORD=your_secure_db_password
```

### 3. Start the Application

```bash
docker-compose up -d
```

This will start three services:
- **MySQL** (port 3306) - Database server
- **Backend** (port 5500) - Spring Boot API
- **Frontend** (port 80) - Angular web application

### 4. Wait for Services to Start

The services will take 30-60 seconds to become healthy. Monitor the startup:

```bash
docker-compose logs -f
```

Press `Ctrl+C` to stop following logs.

### 5. Access the Application

Once all services are healthy, access the application:

- **Web UI:** http://localhost
- **API:** http://localhost:5500/api/v1
- **Swagger UI:** http://localhost:5500/swagger-ui.html

**Default Credentials:**
- Username: `admin`
- Password: `admin`

⚠️ **Security:** You will be forced to change the password on first login!

## Docker Commands

### View Service Status

```bash
docker-compose ps
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### Stop Services

```bash
# Stop containers (data is preserved)
docker-compose down

# Stop and remove volumes (DELETES DATABASE)
docker-compose down -v
```

### Restart Services

```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart backend
```

### Rebuild Containers

```bash
# Rebuild all containers
docker-compose build

# Rebuild and restart
docker-compose up -d --build

# Rebuild specific service
docker-compose build backend
docker-compose up -d backend
```

### Access Container Shell

```bash
# Backend container
docker-compose exec backend sh

# Frontend container
docker-compose exec frontend sh

# MySQL container
docker-compose exec mysql bash
```

### Access MySQL Database

```bash
# Using docker-compose
docker-compose exec mysql mysql -u root -p

# Enter password when prompted (from .env: MINTED_DB_ROOT_PASSWORD)

# Connect to application database
USE minted_db;
SHOW TABLES;
```

## Environment Variables

### Database Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `MINTED_DB_ROOT_PASSWORD` | `rootroot` | MySQL root password |
| `MINTED_DB_NAME` | `minted_db` | Database name |
| `MINTED_DB_USER` | `minted_user` | Database user |
| `MINTED_DB_PASSWORD` | `minted_pass` | Database password |
| `MINTED_DB_PORT` | `3306` | Exposed MySQL port on host |

### Backend Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `BACKEND_PORT` | `5500` | Exposed backend port on host |
| `MINTED_JWT_SECRET` | ⚠️ Required | JWT signing secret (base64, 256+ bits) |
| `MINTED_JWT_EXPIRATION` | `86400000` | JWT expiration (milliseconds, 24h) |
| `MINTED_JASYPT_PASSWORD` | ⚠️ Change in prod | Jasypt encryption password |
| `MINTED_CORS_ORIGINS` | `http://localhost:80,...` | Allowed CORS origins (comma-separated) |
| `LOG_LEVEL` | `INFO` | Application log level |
| `SECURITY_LOG_LEVEL` | `INFO` | Security log level |
| `JPA_SHOW_SQL` | `false` | Show SQL queries in logs |
| `JPA_FORMAT_SQL` | `false` | Format SQL queries in logs |

### Frontend Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `FRONTEND_PORT` | `80` | Exposed frontend port on host |

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     Host Machine                        │
│                                                         │
│  ┌──────────┐      ┌──────────┐      ┌──────────┐    │
│  │ Frontend │──────│ Backend  │──────│  MySQL   │    │
│  │  (Nginx) │      │  (Java)  │      │  (DB)    │    │
│  │ Port 80  │      │ Port 5500│      │ Port 3306│    │
│  └──────────┘      └──────────┘      └──────────┘    │
│       │                  │                  │          │
│       └──────────────────┴──────────────────┘          │
│              minted-network (Bridge)                   │
└─────────────────────────────────────────────────────────┘
```

### Service Communication

- Frontend proxies `/api/*` requests to backend via Nginx
- Backend connects to MySQL using internal hostname `mysql:3306`
- All services communicate over the `minted-network` bridge network
- Only specified ports are exposed to the host machine

## Data Persistence

### MySQL Data Volume

Database data is stored in a named Docker volume: `minted_mysql_data`

```bash
# List volumes
docker volume ls | grep minted

# Inspect volume
docker volume inspect minted_mysql_data

# Backup database
docker-compose exec mysql mysqldump -u root -p minted_db > backup.sql

# Restore database
docker-compose exec -T mysql mysql -u root -p minted_db < backup.sql
```

⚠️ **Warning:** Running `docker-compose down -v` will DELETE the database volume!

## Troubleshooting

### Services Not Starting

1. Check if ports are already in use:
```bash
# Check port 80 (frontend)
netstat -ano | grep :80

# Check port 5500 (backend)
netstat -ano | grep :5500

# Check port 3306 (mysql)
netstat -ano | grep :3306
```

2. Check Docker logs:
```bash
docker-compose logs backend
```

### Health Check Failing

Wait for services to become healthy (can take 60-90 seconds on first startup):

```bash
docker-compose ps
```

All services should show `healthy` status.

### Database Connection Issues

1. Ensure MySQL is healthy:
```bash
docker-compose ps mysql
```

2. Check MySQL logs:
```bash
docker-compose logs mysql
```

3. Verify database credentials in `.env` file

### Backend Startup Errors

1. Check if Flyway migrations ran successfully:
```bash
docker-compose logs backend | grep Flyway
```

2. Verify environment variables:
```bash
docker-compose exec backend env | grep MINTED
```

### Frontend Not Loading

1. Check Nginx logs:
```bash
docker-compose logs frontend
```

2. Verify backend URL configuration:
```bash
docker-compose exec frontend cat /etc/nginx/conf.d/default.conf
```

## Production Deployment

For production deployments:

1. **Security:**
   - Generate secure secrets (JWT, Jasypt)
   - Use strong database passwords
   - Configure proper CORS origins
   - Set `LOG_LEVEL=WARN` or `LOG_LEVEL=ERROR`

2. **Performance:**
   - Allocate sufficient resources (4GB+ RAM recommended)
   - Use production-grade MySQL configuration
   - Enable connection pooling
   - Configure proper caching headers

3. **Monitoring:**
   - Set up container health monitoring
   - Configure log aggregation
   - Monitor resource usage
   - Set up database backups

4. **SSL/TLS:**
   - Use a reverse proxy (Nginx, Traefik, or Caddy)
   - Configure SSL certificates
   - Redirect HTTP to HTTPS
   - Use HSTS headers

## Updating the Application

1. Pull latest changes:
```bash
git pull origin main
```

2. Rebuild and restart:
```bash
docker-compose down
docker-compose build
docker-compose up -d
```

3. Verify services are healthy:
```bash
docker-compose ps
```

## Uninstalling

To completely remove the application:

```bash
# Stop and remove containers
docker-compose down

# Remove volumes (deletes database)
docker-compose down -v

# Remove images
docker rmi minted-frontend minted-backend

# Remove network
docker network rm minted-network
```

## Optional: SigNoz Log Management (Production)

The `docker-compose.prod.yml` overlay adds [SigNoz](https://signoz.io/) for centralized log management, distributed tracing, and metrics collection. It uses the **OpenTelemetry Java Agent** to auto-instrument the backend — no application code changes required.

### SigNoz Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Host Machine                                │
│                                                                     │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    │
│  │ Frontend │    │ Backend  │    │  MySQL   │    │  SigNoz  │    │
│  │  (Nginx) │    │  (Java)  │    │  (DB)    │    │   (UI)   │    │
│  │ Port 80  │    │ Port 5500│    │ Port 3306│    │ Port 3301│    │
│  └──────────┘    └────┬─────┘    └──────────┘    └────┬─────┘    │
│                       │ OTLP/gRPC                      │          │
│                  ┌────▼─────┐                          │          │
│                  │   OTel   │                          │          │
│                  │Collector │                          │          │
│                  │4317/4318 │                          │          │
│                  └────┬─────┘                          │          │
│                       │                                │          │
│                  ┌────▼──────────────────────────────────┘          │
│                  │  ClickHouse  ◄── ZooKeeper                     │
│                  │  (Storage)       (Coordination)                 │
│                  └────────────────────────────────────────          │
│                                                                     │
│              minted-network (Bridge)                                │
└─────────────────────────────────────────────────────────────────────┘
```

- **OTel Java Agent** — attached to the JVM via `JAVA_TOOL_OPTIONS`, auto-captures all SLF4J/Logback logs, HTTP traces, and JVM metrics
- **SigNoz OTel Collector** — receives OTLP data and forwards it to ClickHouse
- **ClickHouse** — time-series database for logs, traces, and metrics storage
- **ZooKeeper** — coordination service for ClickHouse
- **SigNoz UI** — query and visualization dashboard

### Starting with SigNoz

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

This starts all standard services plus the SigNoz stack. The backend automatically switches to:
- `prod` Spring profile (JSON structured logging)
- OTel agent instrumentation (logs, traces, metrics exported via OTLP)

### Additional Ports

| Port | Service              | Purpose                  |
|------|----------------------|--------------------------|
| 3301 | SigNoz UI            | Log viewer dashboard     |
| 4317 | OTel Collector gRPC  | OTLP gRPC endpoint       |
| 4318 | OTel Collector HTTP  | OTLP HTTP endpoint       |

### Additional Containers

| Container              | Image                                    | Purpose                       |
|------------------------|------------------------------------------|-------------------------------|
| minted-otel-collector  | signoz/signoz-otel-collector:latest      | Receives and routes telemetry |
| minted-clickhouse      | clickhouse/clickhouse-server:24.1-alpine | Time-series storage           |
| minted-zookeeper       | bitnami/zookeeper:3.9                    | ClickHouse coordination       |
| minted-signoz          | signoz/signoz:latest                     | Query service and UI          |

### Additional Volumes

| Volume                  | Purpose                        |
|-------------------------|--------------------------------|
| minted_clickhouse_data  | ClickHouse data persistence    |
| minted_zookeeper_data   | ZooKeeper data persistence     |

### SigNoz Files

| File                                | Purpose                                      |
|-------------------------------------|----------------------------------------------|
| `docker-compose.prod.yml`          | Production compose overlay with SigNoz stack |
| `minted-api/Dockerfile.prod`       | Backend Dockerfile with OTel agent included  |
| `signoz/otel-collector-config.yaml` | OTel Collector pipeline configuration        |

### Verification

1. Start the production stack and wait ~60s for ClickHouse and SigNoz to initialize
2. Open http://localhost:3301 — SigNoz UI should load
3. Make API requests to the backend (login, create transactions, etc.)
4. In SigNoz UI, go to **Logs** tab — verify `minted-api` logs appear with MDC fields (`requestId`, `userId`, `method`, `uri`)
5. In SigNoz UI, go to **Traces** tab — verify HTTP request traces appear

### Stopping with SigNoz

```bash
# Stop all containers (data preserved in volumes)
docker compose -f docker-compose.yml -f docker-compose.prod.yml down

# Stop AND delete all volumes (database, logs, metrics, traces all lost)
docker compose -f docker-compose.yml -f docker-compose.prod.yml down -v
```

---

## Support

For issues or questions:
- GitHub Issues: https://github.com/kartikeychoudhary/minted/issues
- Documentation: Check the main README.md file

## License

See LICENSE file in the repository root.
