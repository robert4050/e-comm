# Industrial-Standard Frontend & Backend Architecture Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor both the frontend (Angular `customer-app` & `admin-app`) and backend (Spring Boot `product-service`, `api-gateway`, and `common-library`) into production-grade, enterprise-standard architectures featuring modularity, Clean Layered / DDD design, Correlation ID tracing, reactive state with Signals, and lazy-loaded routes.

**Architecture:**
- **Angular Industrial Architecture**: Strict separation into `core/` (singletons, auth guards, HTTP interceptors for JWT & Correlation ID, global error handling), `shared/` (pure presentational UI components, custom pipes, directives), and `features/` (domain-sliced features with lazy-loaded routes, container/presentational components, and signal-based state).
- **Spring Boot Industrial Architecture**: Clean Layered Architecture with `config/`, `controller/` (OpenAPI/Swagger ready, DTO contracts, validation), `service/` (interface-driven business logic with transactional outbox), `repository/` (Spring Data JPA), `entity/` (auditing, optimistic locking), `kafka/` (resilient outbox publisher), `filter/` (MDC correlation ID filter), and `exception/` (RFC 7807 Problem Details tied to correlation ID).

**Tech Stack:**
- Frontend: Angular 21, TypeScript, RxJS, Angular Signals, Standalone Components, Nginx.
- Backend: Java 21 LTS, Spring Boot 3.3.4, Spring Cloud Gateway, Spring Security 6 (Keycloak OIDC), Spring Data JPA, Flyway, Apache Kafka, SLF4J/MDC.

---

## Global Constraints

- Project Name: strictly `e-comm`.
- Code standard: zero compilation warnings, pure Java without fragile annotation processor hacks, strict TypeScript typing without `any`.
- Tracing standard: `X-Correlation-ID` header generated at Gateway and propagated across HTTP interceptors, REST controllers, and Kafka event envelopes.
- Routing standard: Lazy-loaded standalone feature routes with Angular Router.
- Containerization: All builds must compile cleanly and run within the existing `docker-compose.yml`.

---

### Task 1: Backend Common Library & Enterprise Contracts

**Files:**
- Create: `backend/common-library/src/main/java/com/ecomm/common/constant/HeaderConstants.java`
- Create: `backend/common-library/src/main/java/com/ecomm/common/dto/ApiResponse.java`
- Create: `backend/common-library/src/main/java/com/ecomm/common/dto/PageResponse.java`
- Create: `backend/common-library/src/main/java/com/ecomm/common/exception/BaseDomainException.java`
- Create: `backend/common-library/src/main/java/com/ecomm/common/exception/ResourceNotFoundException.java`
- Create: `backend/common-library/src/main/java/com/ecomm/common/exception/ConflictException.java`
- Modify: `backend/common-library/src/main/java/com/ecomm/common/dto/ProblemDetailsDto.java`

- [ ] **Step 1: Implement enterprise constants and common DTOs**
- [ ] **Step 2: Implement domain exception hierarchy**
- [ ] **Step 3: Compile and test common-library module**

---

### Task 2: API Gateway Industrial Enhancements (Tracing & Logging)

**Files:**
- Create: `backend/api-gateway/src/main/java/com/ecomm/gateway/filter/CorrelationIdGlobalFilter.java`
- Create: `backend/api-gateway/src/main/java/com/ecomm/gateway/filter/LoggingGlobalFilter.java`
- Modify: `backend/api-gateway/src/main/resources/application.yml`

- [ ] **Step 1: Implement reactive CorrelationIdGlobalFilter ensuring `X-Correlation-ID` on all inbound & outbound traffic**
- [ ] **Step 2: Implement structured LoggingGlobalFilter recording latency, method, URI, and correlation ID**
- [ ] **Step 3: Test and verify Gateway build**

---

### Task 3: Product Service Clean Layered Architecture Refactor

**Files:**
- Move & Refactor to:
  - `backend/product-service/src/main/java/com/ecomm/product/config/WebMvcConfig.java`
  - `backend/product-service/src/main/java/com/ecomm/product/filter/CorrelationIdFilter.java`
  - `backend/product-service/src/main/java/com/ecomm/product/dto/request/CreateProductRequest.java`
  - `backend/product-service/src/main/java/com/ecomm/product/dto/request/UpdateProductRequest.java`
  - `backend/product-service/src/main/java/com/ecomm/product/dto/response/ProductResponse.java`
  - `backend/product-service/src/main/java/com/ecomm/product/exception/ProductNotFoundException.java`
  - `backend/product-service/src/main/java/com/ecomm/product/exception/ProductAlreadyExistsException.java`
  - `backend/product-service/src/main/java/com/ecomm/product/exception/GlobalExceptionHandler.java`
  - `backend/product-service/src/main/java/com/ecomm/product/kafka/outbox/OutboxProcessor.java`
  - `backend/product-service/src/main/java/com/ecomm/product/controller/ProductController.java`
  - `backend/product-service/src/main/java/com/ecomm/product/service/ProductService.java`
  - `backend/product-service/src/main/java/com/ecomm/product/service/impl/ProductServiceImpl.java`

- [ ] **Step 1: Implement MDC CorrelationIdFilter for distributed log tracing**
- [ ] **Step 2: Restructure DTOs into request/response packages with fine-grained validation**
- [ ] **Step 3: Refactor exception handler with correlation ID injection and typed domain exceptions**
- [ ] **Step 4: Refactor OutboxProcessor with exponential retry metadata**
- [ ] **Step 5: Verify backend multi-module build via `backend/mvnw.cmd`**

---

### Task 4: Customer Portal (`customer-app`) Enterprise Angular Structure

**Files:**
- Restructure:
  - `frontend/customer-app/src/app/core/` (auth service, auth interceptor, correlation-id interceptor, error interceptor)
  - `frontend/customer-app/src/app/shared/components/` (navbar, footer, loading-spinner, status-badge)
  - `frontend/customer-app/src/app/shared/pipes/` (truncate pipe, currency pipe)
  - `frontend/customer-app/src/app/features/catalog/` (catalog routes, catalog-page container, product-card, product-filter, catalog service)
  - `frontend/customer-app/src/app/features/cart/` (cart drawer, cart service with signals)
  - `frontend/customer-app/src/app/features/checkout/` (saga checkout stepper modal, checkout service)
  - `frontend/customer-app/src/app/app.routes.ts` (lazy loading with `loadComponent` / `loadChildren`)

- [ ] **Step 1: Create Core HTTP interceptors (`correlation-id.interceptor.ts`, `error.interceptor.ts`)**
- [ ] **Step 2: Create Shared UI components (`navbar`, `footer`, `status-badge`, `truncate.pipe.ts`)**
- [ ] **Step 3: Build Catalog feature with lazy-loaded route & container-presenter pattern**
- [ ] **Step 4: Connect Cart and Checkout Saga features with reactive signals**
- [ ] **Step 5: Update `app.routes.ts` with lazy loading and verify build**

---

### Task 5: Admin Portal (`admin-app`) Enterprise Angular Structure

**Files:**
- Restructure:
  - `frontend/admin-app/src/app/core/` (auth service with role guard, correlation-id interceptor, error interceptor)
  - `frontend/admin-app/src/app/shared/components/` (sidebar, topbar, metric-card, data-table)
  - `frontend/admin-app/src/app/features/dashboard/` (dashboard overview metrics)
  - `frontend/admin-app/src/app/features/products/` (product list container, product form modal, admin product service)
  - `frontend/admin-app/src/app/app.routes.ts` (lazy loading with `loadComponent`)

- [ ] **Step 1: Create Core guards, interceptors, and models**
- [ ] **Step 2: Create Shared layout components (`sidebar`, `topbar`, `metric-card`)**
- [ ] **Step 3: Build Dashboard and Product management features with lazy-loaded routes**
- [ ] **Step 4: Update `app.routes.ts` and verify build**

---

### Task 6: Verification & Commit

- [ ] **Step 1: Run Maven backend compile and package across all modules**
- [ ] **Step 2: Run Angular production builds for both `customer-app` and `admin-app`**
- [ ] **Step 3: Update documentation and commit all architectural changes to Git**
