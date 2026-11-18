# Stage 1: Build
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY common-library ./common-library
COPY api-gateway ./api-gateway
COPY product-service ./product-service

RUN --mount=type=cache,target=/root/.m2 mvn clean package -pl product-service -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /workspace/product-service/target/product-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
