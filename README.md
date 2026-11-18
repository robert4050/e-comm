# e-comm :: Enterprise Microservices Platform

An advanced, cloud-native microservices e-commerce platform built on **Java 21 LTS**, **Spring Boot 3**, **Angular 21**, **Keycloak**, **MySQL 8**, **Apache Kafka (KRaft)**, **Redis**, and **Elasticsearch**, fully containerized and runnable locally with **Docker Compose**.

---

## 🏗️ Architecture & Blueprint Highlights

```
                            [ Web Browsers & Mobile Clients ]
                                     |             |
                                     v             v
             [ Customer Portal :4200 ]             [ Admin Portal :4201 ]
                     (Angular)                             (Angular)
                                     \             /
                                      \           /
                                       v         v
                               [ API Gateway :8080 ] <---> [ Keycloak OIDC :8180 ]
                            (Spring Cloud Gateway)         (Realm: e-comm)
                                        |
                 +----------------------+----------------------+
                 |                      |                      |
                 v                      v                      v
        [ Product Service ]    [ Order Service ]     [ Checkout Service (Saga) ]
           (:8081 REST)           (:8082 REST)              (:8083 REST)
                 |                      |                      |
                 +----------------------+----------------------+
                 |                      |                      |
                 v                      v                      v
        [ MySQL (Database-Per-Service) ]     [ Apache Kafka (KRaft) ]
        - product_db                         - product.events.v1
        - order_db                           - order.events.v1
        - checkout_db                        - payment.events.v1
        - keycloak_db                        (Transactional Outbox Pattern)
```

### Key Engineering Principles
- **Containerize Everything**: Single-command startup with Docker Compose for all applications and infrastructure.
- **Microservice Ownership & Isolation**: Each microservice owns its business logic, schema/database (`product_db`, `order_db`, `customer_db`, etc.), migrations (Flyway), and deployment unit.
- **Security in Depth**: Keycloak issues tokens using OpenID Connect (OIDC) & PKCE; the API Gateway and protected microservices validate JWT signatures and enforce role-based access control (`ADMIN`, `PRODUCT_MANAGER`, `CUSTOMER`).
- **Event-Driven Resilience**: Transactional Outbox Pattern for publishing domain events via Apache Kafka with idempotency and at-least-once delivery.
- **Distributed Saga**: Orchestrated checkout saga across Pricing, Inventory, Order, and Payment services with compensating actions.

---

## 📦 19 Microservices Domain Map

| Service | Responsibility | Port | Data Store | Events Published |
|---|---|---|---|---|
| **api-gateway** | Routing, JWT verification, rate limiting, CORS | `8080` | Stateless | - |
| **product-service** | Products, SKUs, variants, publishing | `8081` | MySQL (`product_db`) | `product.events.v1` |
| **category-service**| Category tree, brands, dynamic attributes | `8082` | MySQL (`category_db`) | `category.events.v1` |
| **media-service** | Image upload pipeline, metadata, S3 | `8083` | MySQL (`media_db`) | `media.events.v1` |
| **pricing-service** | Quotes, discounts, coupons, promotions | `8084` | MySQL (`pricing_db`) | `pricing.events.v1` |
| **inventory-service**| Stock reservations (TTL), warehouses | `8085` | MySQL (`inventory_db`)| `inventory.events.v1` |
| **cart-service** | Shopping cart lifecycle, guest cart merge | `8086` | MySQL (`cart_db`) + Redis | `cart.events.v1` |
| **wishlist-service**| Saved items, customer favorites | `8087` | MySQL (`wishlist_db`) | - |
| **checkout-service**| Saga orchestrator, checkout state | `8088` | MySQL (`checkout_db`)| - |
| **order-service** | Immutable order snapshots, status history | `8089` | MySQL (`order_db`) | `order.events.v1` |
| **payment-service** | Payment authorization, webhooks, refunds | `8090` | MySQL (`payment_db`) | `payment.events.v1` |
| **shipping-service**| Shipment creation, tracking | `8091` | MySQL (`shipping_db`) | `shipment.events.v1` |
| **returns-service** | Return eligibility & approvals | `8092` | MySQL (`returns_db`) | `return.events.v1` |
| **customer-service**| Customer profile, preferences | `8093` | MySQL (`customer_db`) | `customer.events.v1` |
| **address-service** | Delivery & billing addresses | `8094` | MySQL (`address_db`) | - |
| **search-service** | Full-text, fuzzy, faceted search | `8095` | Elasticsearch | - |
| **review-service** | Ratings, reviews, moderation | `8096` | MySQL (`review_db`) | `review.events.v1` |
| **recommendation-service**| Behavior signals, collaborative filtering | `8097`| MySQL + Vector Index | - |
| **notification-service**| Email, SMS, transactional notifications | `8098` | MySQL (`notification_db`)| - |
| **ai-assistant-service**| RAG, product Q&A, LLM tool calling | `8099` | MySQL + Vector Index | - |

---

## 🚀 Quickstart Guide

### 1. Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (v24+ or higher with Compose v2)
- Node.js 20+ & Java 21 (optional for local non-container development)

### 2. Launch Stack via Docker Compose
From the repository root, execute:

```bash
# Start all containers in the background
docker compose up -d
```

To build and start with fresh images:
```bash
docker compose up --build -d
```

### 3. Service URLs & Ports

| Application / Service | URL | Default Credentials |
|---|---|---|
| **Customer Portal** | `http://localhost:4200` | Retail Customer (`customer` / `customer123`) |
| **Admin Portal** | `http://localhost:4201` | Product Manager (`manager` / `manager123`) or Admin (`admin` / `admin123`) |
| **API Gateway** | `http://localhost:8080` | Protected by Keycloak JWT |
| **Keycloak IAM** | `http://localhost:8180` | Console Admin: `admin` / `admin` |
| **Database Web GUI (phpMyAdmin)** | `http://localhost:8085` | User: `ecomm_user` / `ecomm_password` |
| **MySQL 8** | `localhost:3306` | User: `ecomm_user` / Password: `ecomm_password` |
| **Apache Kafka** | `localhost:9092` | Single-node KRaft broker |
| **Redis** | `localhost:6379` | Cache & Ephemeral Session Store |
| **Elasticsearch** | `http://localhost:9200` | Search & Vector Index |

---

## 🔑 Pre-Configured Test Accounts (Keycloak)

Realm: `e-comm`

| Username | Password | Realm Roles | Description |
|---|---|---|---|
| `admin` | `admin123` | `ADMIN`, `PRODUCT_MANAGER`, `CUSTOMER` | Super Administrator with full privileges |
| `manager` | `manager123` | `PRODUCT_MANAGER`, `CUSTOMER` | Catalog and Product Manager |
| `customer`| `customer123` | `CUSTOMER` | Retail customer shopping account |

---

## 🧪 Testing the First Vertical Slice

### 1. Obtain an Access Token from Keycloak
```bash
curl -X POST "http://localhost:8180/realms/e-comm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=admin-app" \
  -d "username=manager" \
  -d "password=manager123"
```

### 2. Fetch Catalog Products via API Gateway (Public Endpoint)
```bash
curl -X GET "http://localhost:8080/api/v1/products"
```

### 3. Create a New Product via API Gateway (Protected Endpoint)
```bash
curl -X POST "http://localhost:8080/api/v1/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -d '{
    "sku": "SKU-MON-007",
    "name": "Ultra-Wide Gaming Monitor 34\"",
    "description": "Curved OLED 240Hz 0.03ms HDR1000 gaming display.",
    "price": 899.99,
    "category": "Electronics",
    "stockQuantity": 30,
    "imageUrl": "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=600"
  }'
```

### 4. Verify Kafka Domain Event Dispatch
The Product Service immediately writes the `ProductCreated` domain event to the `outbox_events` table in `product_db`, and the scheduled publisher dispatches it to the Kafka topic `product.events.v1`!

---

## 🛑 Stopping the Platform

```bash
# Stop containers and keep data volumes intact
docker compose down

# Stop and remove persistent data volumes
docker compose down -v
```
