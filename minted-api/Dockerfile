# Stage 1: Build
FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

# Copy gradle files first for dependency caching
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 spring && \
    adduser -u 1001 -G spring -s /bin/sh -D spring

# Copy JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Change ownership
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port (configurable via environment)
EXPOSE ${SERVER_PORT:-5500}

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-5500}/api/v1/auth/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
