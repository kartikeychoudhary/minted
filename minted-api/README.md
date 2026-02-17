# Minted API - Spring Boot Backend

## Prerequisites
- Java 17
- MySQL 8.x
- Gradle 8.x (or use wrapper)

## Environment Variables
Set these before running:
```bash
export MINTED_DB_HOST=localhost
export MINTED_DB_PORT=3306
export MINTED_DB_NAME=minted_db
export MINTED_DB_USER=root
export MINTED_DB_PASSWORD=your_password
export MINTED_JWT_SECRET=your-256-bit-secret-key
export MINTED_JASYPT_PASSWORD=your-jasypt-password
```

## Setup
1. Create MySQL database:
   ```sql
   CREATE DATABASE minted_db;
   ```

2. Run Flyway migrations:
   ```bash
   ./gradlew flywayMigrate
   ```

3. Start the application:
   ```bash
   ./gradlew bootRun
   ```

## API Documentation
Once running, access Swagger UI at: http://localhost:5500/swagger-ui

## Project Structure
```
src/main/java/com/minted/api/
├── config/         # Security, JWT, CORS configuration
├── controller/     # REST Controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA Entities
├── enums/          # Enumerations
├── exception/      # Custom Exceptions
├── filter/         # JWT Auth Filter
├── repository/     # Spring Data JPA Repositories
├── service/        # Business Logic (interface + impl/)
└── util/           # Utility Classes
```
