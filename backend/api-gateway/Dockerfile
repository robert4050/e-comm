# Stage 1: Build
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy parent pom and source files
COPY pom.xml ./pom.xml
COPY common-library ./common-library
COPY api-gateway ./api-gateway
COPY product-service ./product-service

# Build common-library and api-gateway
RUN --mount=type=cache,target=/root/.m2 mvn clean package -pl api-gateway -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /workspace/api-gateway/target/api-gateway-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
