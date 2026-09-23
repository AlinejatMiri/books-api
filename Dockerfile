# syntax=docker/dockerfile:1

# =============================================================================
# Multi-stage build for the LAB-03 Book Management API.
#
# Build:  docker build -t book-api .
# Run:    docker run -p 8080:8080 book-api
#
# The catalogue is kept in memory, so the container is stateless.
# =============================================================================

# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace/app

# Cache dependencies first: re-copying only src/ then rebuilds fast
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./
RUN ./mvnw -q -B dependency:go-offline

COPY src ./src
RUN ./mvnw -q -B package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /workspace/app/target/book-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Simple health check against the catalogue endpoint (no actuator in this app)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
    CMD wget -qO- http://localhost:8080/api/v3/books || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
