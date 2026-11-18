# e-comm Platform Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a containerized, enterprise-grade e-commerce microservices platform named `e-comm` (based on the architectural blueprint) featuring two Angular frontends (Customer & Admin), Spring Boot 3 / Java 21 microservices, Spring Cloud Gateway, Keycloak OIDC, MySQL 8 per-service databases, Kafka KRaft, Redis, and Elasticsearch, orchestrating everything via Docker Compose.

**Architecture:** Distributed microservices architecture adhering to database-per-service, transactional outbox & event-driven patterns with Kafka, OAuth2/OIDC security with Keycloak, unified routing through Spring Cloud Gateway, and two Angular frontends served via Nginx. Everything is fully containerized with healthchecks and compose profiles.

**Tech Stack:** 
- Frontend: Angular (v19/v21) + TypeScript + RxJS + Tailwind/Bootstrap + Nginx Docker
- Backend: Java 21 LTS + Spring Boot 3.3.x + Spring Cloud Gateway + Spring Data JPA + Flyway
- Security: Keycloak 24+ (OpenID Connect / OAuth 2.0 with PKCE & JWT)
- Data & Storage: MySQL 8 (independent schemas per service)
- Messaging: Apache Kafka (KRaft mode)
- Search & Cache: Elasticsearch & Redis
- Container Runtime: Docker & Docker Compose (v2)

---

## Global Constraints

- Project Name: strictly `e-comm` across all configurations, realms, client definitions, and documentation.
- Target Runtime: 100% containerized via `docker-compose.yml`. Must support `docker compose up` with single-command spin-up.
- Backend baseline: Java 21 LTS, Spring Boot 3.x, Maven multi-module structure with root parent `pom.xml`.
- Frontend baseline: Two distinct applications (`customer-app` and `admin-app`) under `frontend/`.
- Database ownership: Each microservice owns its schema/database (`product_db`, `customer_db`, `order_db`, etc.).
- Event format: Standard JSON event envelope with `eventId`, `eventType`, `occurredAt`, `aggregateId`, `payload`.
- Security: Keycloak realm named `e-comm` with preconfigured clients, roles, and test users.

---

### Task 1: Project Repository Structure & Root Infrastructure Setup

**Files:**
- Create: `docker-compose.yml`
- Create: `.env.example`
- Create: `.env`
- Create: `.gitignore`
- Create: `infrastructure/mysql/init/01-init-databases.sql`
- Create: `infrastructure/keycloak/realm-export.json`
- Create: `README.md`

**Interfaces:**
- Produces: MySQL container hosting service schemas (`keycloak_db`, `product_db`, `customer_db`, `order_db`, `inventory_db`, `cart_db`, `pricing_db`), Kafka KRaft broker, Redis, Elasticsearch, and Keycloak realm `e-comm`.

- [ ] **Step 1: Create repository .gitignore and root environment variables**
- [ ] **Step 2: Create MySQL multi-database initialization script for all microservices**
- [ ] **Step 3: Create Keycloak `e-comm` realm configuration with pre-seeded roles, clients, and test users**
- [ ] **Step 4: Create master `docker-compose.yml` with healthchecks, persistent volumes, and networking**
- [ ] **Step 5: Verify infrastructure startup with Docker Compose**

---

### Task 2: Backend Maven Multi-Module Foundation & Common Library

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/common-library/pom.xml`
- Create: `backend/common-library/src/main/java/com/ecomm/common/event/DomainEvent.java`
- Create: `backend/common-library/src/main/java/com/ecomm/common/dto/ProblemDetailsDto.java`
- Create: `backend/common-library/src/main/java/com/ecomm/common/security/SecurityUtils.java`

**Interfaces:**
- Produces: Parent POM managing dependency versions (Spring Boot 3.3.x, Spring Cloud 2023.x, Java 21) and shared DTOs/event envelopes.

- [ ] **Step 1: Write root backend `pom.xml` with dependency management**
- [ ] **Step 2: Implement `common-library` with domain event envelope, error contracts, and DTOs**
- [ ] **Step 3: Add Maven Wrapper (`mvnw` / `.mvn`) for self-contained builds**

---

### Task 3: API Gateway Service

**Files:**
- Create: `backend/api-gateway/pom.xml`
- Create: `backend/api-gateway/src/main/resources/application.yml`
- Create: `backend/api-gateway/src/main/java/com/ecomm/gateway/ApiGatewayApplication.java`
- Create: `backend/api-gateway/src/main/java/com/ecomm/gateway/config/SecurityConfig.java`
- Create: `backend/api-gateway/Dockerfile`

**Interfaces:**
- Consumes: Keycloak JWT tokens on port 8080.
- Produces: Reverse proxy on `http://localhost:8080/api/v1/*` routing to downstream services with CORS and token propagation.

- [ ] **Step 1: Create API Gateway POM and application configuration**
- [ ] **Step 2: Configure Spring Cloud Gateway routing for `/api/v1/products/**`, `/api/v1/orders/**`, etc.**
- [ ] **Step 3: Configure Spring Security Reactive Resource Server with Keycloak JWKS**
- [ ] **Step 4: Create multi-stage Dockerfile for API Gateway**

---

### Task 4: Product Service (First Vertical Slice Backend)

**Files:**
- Create: `backend/product-service/pom.xml`
- Create: `backend/product-service/src/main/resources/application.yml`
- Create: `backend/product-service/src/main/resources/db/migration/V1__init_products.sql`
- Create: `backend/product-service/src/main/java/com/ecomm/product/ProductServiceApplication.java`
- Create: `backend/product-service/src/main/java/com/ecomm/product/entity/Product.java`
- Create: `backend/product-service/src/main/java/com/ecomm/product/repository/ProductRepository.java`
- Create: `backend/product-service/src/main/java/com/ecomm/product/dto/ProductRequest.java`
- Create: `backend/product-service/src/main/java/com/ecomm/product/dto/ProductResponse.java`
- Create: `backend/product-service/src/main/java/com/ecomm/product/service/ProductService.java`
- Create: `backend/product-service/src/main/java/com/ecomm/product/controller/ProductController.java`
- Create: `backend/product-service/src/main/java/com/ecomm/product/kafka/ProductEventPublisher.java`
- Create: `backend/product-service/Dockerfile`

**Interfaces:**
- Consumes: MySQL `product_db`, Kafka `product.events.v1`.
- Produces: CRUD endpoints `GET /api/v1/products`, `POST /api/v1/products`, `GET /api/v1/products/{id}`, `PUT /api/v1/products/{id}`.

- [ ] **Step 1: Setup Product Service Maven config and Flyway migrations**
- [ ] **Step 2: Implement Product entity, repository, and DTOs with Bean Validation**
- [ ] **Step 3: Implement Product service and Kafka event publishing**
- [ ] **Step 4: Implement REST controller with role-based security (`ADMIN` / `PRODUCT_MANAGER` to write, public to read)**
- [ ] **Step 5: Create Product Service multi-stage Dockerfile**

---

### Task 5: Customer Portal Angular App (`customer-app`)

**Files:**
- Create: `frontend/customer-app/package.json`
- Create: `frontend/customer-app/angular.json`
- Create: `frontend/customer-app/src/*` (Core, Shared, Features: Home, Catalog, Product Details, Cart, Auth)
- Create: `frontend/customer-app/nginx.conf`
- Create: `frontend/customer-app/Dockerfile`

**Interfaces:**
- Consumes: API Gateway at `http://localhost:8080/api/v1` and Keycloak at `http://localhost:8180`.
- Produces: Customer SPA running on port 4200 (or mapped container port 80/4200).

- [ ] **Step 1: Scaffold Angular Customer Portal structure**
- [ ] **Step 2: Setup Keycloak authentication service and HTTP interceptor**
- [ ] **Step 3: Implement Product catalog browsing, search/filter UI, and product detail view**
- [ ] **Step 4: Configure Nginx reverse proxy configuration and multi-stage Dockerfile**

---

### Task 6: Admin Portal Angular App (`admin-app`)

**Files:**
- Create: `frontend/admin-app/package.json`
- Create: `frontend/admin-app/angular.json`
- Create: `frontend/admin-app/src/*` (Core, Shared, Features: Dashboard, Product Management, Inventory, Orders)
- Create: `frontend/admin-app/nginx.conf`
- Create: `frontend/admin-app/Dockerfile`

**Interfaces:**
- Consumes: API Gateway at `http://localhost:8080/api/v1` with `PRODUCT_MANAGER`/`ADMIN` credentials.
- Produces: Admin SPA running on port 4201.

- [ ] **Step 1: Scaffold Angular Admin Portal structure**
- [ ] **Step 2: Setup Keycloak authentication service with role guard**
- [ ] **Step 3: Implement Product CRUD management UI (create, list, edit, publish)**
- [ ] **Step 4: Configure Nginx and multi-stage Dockerfile**

---

### Task 7: End-to-End Orchestration, Verification & Documentation

**Files:**
- Modify: `docker-compose.yml` (wire all services)
- Create: `README.md` (complete quickstart guide with curl examples, credentials, and architecture diagrams)
- Create: Verification test scripts (`verify-stack.ps1` / `verify-stack.sh`)

- [ ] **Step 1: Wire full stack in `docker-compose.yml` with profiles (`infra`, `backend`, `frontend`, `all`)**
- [ ] **Step 2: Build and verify container images**
- [ ] **Step 3: Execute integration test: Keycloak token issuance -> Gateway routing -> Product Service CRUD -> Kafka event**
- [ ] **Step 4: Validate Customer Portal and Admin Portal in browser / HTTP checks**
- [ ] **Step 5: Document complete architecture, startup guide, and verification steps in README.md**
