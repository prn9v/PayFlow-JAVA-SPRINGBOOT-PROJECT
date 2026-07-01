# 💳 PayFlow Microservices Backend

Welcome to **PayFlow**, a robust, secure, and production-grade distributed payment gateway and merchant ledger orchestration system. The backend is architected using **Java Spring Boot**, **Spring Cloud (Netflix Eureka, Spring Cloud Gateway)**, and a highly resilient event-driven model powered by **RabbitMQ**, **Redis**, **MySQL**, and the **Razorpay API**.

This system is engineered for maximum availability, fault tolerance, and secure inter-service communication.

---

## 🗺️ System Architecture

The following diagram illustrates how requests flow through the system, how inter-service calls are made, and how events are published asynchronously across the microservices grid:

```mermaid
graph TD
    Client[Client / Frontend] -->|HTTP Requests / Cookies| Gateway[API Gateway - Port 8080]
    Gateway -->|Eureka Service Discovery| Eureka[Eureka Service Registry - Port 8761]
    
    %% Gateway Filter Authentication
    Gateway -->|JWT Validation & Headers| AuthSvc[Auth Service - Port 8081]
    Gateway -->|Propagates: X-User-Id, X-User-Role| MerchantSvc[Merchant Service - Port 8082]
    Gateway -->|Propagates: X-User-Id, X-User-Role| PaymentSvc[Payment Service - Port 8083]
    Gateway -->|Propagates: X-User-Id, X-User-Role| WalletSvc[Wallet Service - Port 8085]
    Gateway -->|Propagates: X-User-Id, X-User-Role| NotificationSvc[Notification Service - Port 8084]

    %% Internal Feign Communication
    PaymentSvc -->|Feign Client + Circuit Breaker| MerchantSvc
    WalletSvc -->|Feign Client| MerchantSvc
    
    %% Third-party Integrations
    AuthSvc -->|Sends OTP / Verification| Brevo[Brevo SMTP Server]
    PaymentSvc -->|Initiate Payouts / Orders| Razorpay[Razorpay Payment Gateway]
    Razorpay -->|Webhooks / Callbacks| PaymentSvc
    NotificationSvc -->|HTML Templates| Brevo
    
    %% Databases & Cache Layer
    AuthSvc --> MySQL[(MySQL DB - Aiven)]
    MerchantSvc --> MySQL
    MerchantSvc -->|Cache Credentials & Profiles| RedisCache[(Redis Cache - Upstash)]
    PaymentSvc --> MySQL
    WalletSvc --> MySQL
    NotificationSvc --> MySQL
    
    %% Event Bus Orchestration
    MerchantSvc -->|Publish: merchant.activated| RabbitMQ{RabbitMQ Exchange: payflow.events}
    PaymentSvc -->|Publish: payment.success / failed / refund.created| RabbitMQ
    RabbitMQ -->|Consume| WalletSvc
    RabbitMQ -->|Consume| NotificationSvc
```

---

## 📁 Repository Structure & Services Directory

Each directory contains a self-contained Spring Boot project acting as a microservice:

| Service Name | Port | Description | Primary Technologies |
| :--- | :--- | :--- | :--- |
| [`api_gateway`](./api_gateway) | `8080` | Centralized gateway router, CORS management, and JWT validation filter. | Spring Cloud Gateway, JWT (jjwt) |
| [`eureka_server`](./eureka_server) | `8761` | Service discovery registry for dynamic location mapping. | Spring Cloud Netflix Eureka Server |
| [`auth-service`](./auth-service) | `8081` | Core identity management, local/OAuth2 credentials, and registration verification. | Spring Security, OAuth2 (Google/GitHub), Brevo SMTP |
| [`merchant-service`](./merchant-service) | `8082` | Merchant directory, bank accounts, and secure API credentials (Key/Secret). | Spring Data JPA, Redis (Upstash), RabbitMQ |
| [`payment-service`](./payment-service) | `8083` | Core transaction controller, Razorpay Order SDK, signature verification, and Webhooks. | Razorpay SDK, Resilience4j, RabbitMQ, Spring Data JPA |
| [`wallet-service`](./wallet-service) | `8085` | Merchant balance ledger, debit/credit logic, settlements (payouts), and transaction logs. | Spring Data JPA, RabbitMQ Consumer |
| [`notification-service`](./notification-service) | `8084` | Email dispatch service using HTML templates and RabbitMQ queues. | JavaMailSender, Brevo SMTP, RabbitMQ Consumer |

---

## 🛠️ Microservice Details & Key Features

### 1. Centralized Routing & Security Gateway (`api_gateway`)
* **Dynamic Routing:** Acts as the reverse proxy. All traffic entering the cluster is dynamically routed to downstream services registered with the Eureka Registry.
* **Global Authentication Filter ([JwtAuthenticationFilter](./api_gateway/src/main/java/com/pranav/api_gateway/filter/JwtAuthenticationFilter.java)):** Intercepts requests for secure routes. It validates the `payflow_token` HTTP-Only cookie (or Bearer token) using the centralized `jwt.secret`.
* **Identity Header Propagation:** Once a JWT is validated, the Gateway extracts the user claims (`userId`, `email`, `role`) and injects them as custom headers (`X-User-Id`, `X-User-Email`, `X-User-Role`) into the request forwarded to downstream microservices.

### 2. High-Performance Caching (`merchant-service`)
* **Profile & Bank Accounts:** Handles registration of merchant businesses, including tax details (PAN numbers) and bank accounts (with primary designation).
* **API Credentials Management:** Allows merchants to generate, rotate, and disable a pair of API keys (`publicKey` and `secretKey`). Downstream checkout widgets use these keys to authenticate orders.
* **Redis Caching ([RedisConfig](./merchant-service/src/main/java/com/pranav/merchant_service/config/RedisConfig.java)):** Integrated with Upstash Redis to cache merchant credentials (TTL: 30 minutes) and profiles (TTL: 60 minutes). This ensures that payment authorization requests bypass database roundtrips, maintaining sub-second latency.

### 3. Payment Processing & Circuit Breakers (`payment-service`)
* **Razorpay Order Creation:** Generates a secure order ID from the Razorpay API. Amounts are auto-converted to paise (smallest currency unit for INR).
* **Fault Tolerance & Resilience (Resilience4j):**
  * **Circuit Breaker ([CircuitBreakerService](./payment-service/src/main/java/com/pranav/payment_service/service/CircuitBreakerService.java)):** Protects payment flows. If the `merchant-service` or the third-party Razorpay API is experiencing elevated failure rates (>50% failures over 10 calls), the circuit opens. Fallbacks gracefully fail the transaction without hanging threads.
  * **Retry Engine:** Automatically retries transient errors (e.g. network timeouts, Feign retry exceptions) up to 3 times for the merchant client and 2 times for Razorpay.
* **Secure Webhook Processor ([PaymentService](./payment-service/src/main/java/com/pranav/payment_service/service/PaymentService.java)):** Validates HMAC SHA256 webhook signatures from Razorpay via a public endpoint. Upon confirmation:
  * `payment.captured` -> Updates state to `SUCCESS`, logs state audit in `payment_events`, and publishes `PaymentSuccessEvent`.
  * `payment.failed` -> Updates state to `FAILED` and publishes `PaymentFailedEvent`.
* **Refund Manager:** Handles partial and full refunds. Verifies that the refund amount does not exceed the successful payment amount, registers a `Refund` record in `PENDING` state, and pushes a `RefundCreatedEvent` to RabbitMQ.

### 4. Merchant Ledger & Settlements (`wallet-service`)
* **Dynamic Onboarding:** Consumes the `merchant.activated` event to automatically create a wallet for newly approved merchants.
* **Double-Entry Balance Updates:** Consumes `payment.success` events to credit wallets and `refund.created` events to debit wallets.
* **Idempotency Guard:** Every wallet credit/debit checks the transaction ledger for the presence of the `referenceId` (e.g., `paymentId` or `refundId`). Duplicate events are immediately dropped, guaranteeing exactly-once ledger updates.
* **Settlement Engine:** Merchants can request a payout (`Settlement`) of their funds to their primary bank account. The system validates the balance, registers the settlement as `PENDING`, and immediately debits the available balance to prevent double-spending.

### 5. Notification Templates & Dispatch (`notification-service`)
* **HTML Templating Engine:** Stores template bodies in the database (e.g., `PAYMENT_SUCCESS`, `REFUND_CREATED`) containing double curly brace placeholders (e.g., `{{amount}}`, `{{paymentReference}}`).
* **Asynchronous Dispatcher:** Listens to RabbitMQ queues, dynamically resolves templates, creates audit records in the database, and sends HTML emails asynchronously using JavaMailSender over SMTP (Brevo).
* **Retry Dashboard Console:** Saves the failure reason for undelivered notifications. Allows administrators to retry failures via `POST /api/notifications/{id}/resend`.

---

## ✉️ Event-Driven Orchestration (RabbitMQ Topology)

PayFlow leverages an asynchronous, event-driven pattern to decouple heavy operations (like ledger updates and email sending) from critical customer payment flows.

### 🔀 Exchanges, Routing Keys, and Queues

All services share a unified topology defined in their respective `RabbitMQConfig` classes:
* **Exchange:** `payflow.events` (Topic Exchange)

| Routing Key | Target Queue | Producer | Consumers | Actions Triggered |
| :--- | :--- | :--- | :--- | :--- |
| `merchant.activated` | `queue.merchant.activated` | `merchant-service` | `wallet-service` | Allocates new wallet structure for the merchant. |
| `payment.created` | `queue.payment.created` | `payment-service` | `notification-service` | Sends a "Payment Order Initiated" email to the customer. |
| `payment.success` | `queue.payment.success` | `payment-service` | `wallet-service`, `notification-service` | Credits merchant wallet, updates ledger, sends customer receipt email. |
| `payment.failed` | `queue.payment.failed` | `payment-service` | `notification-service` | Sends a "Payment Failed" alert email to the customer. |
| `refund.created` | `queue.refund.created` | `payment-service` | `wallet-service`, `notification-service` | Debits merchant wallet, logs refund transaction, sends refund confirmation email. |

---

## 🔒 Security & Context Propagation

1. **Centralized Authentication:** Clients log in through `/api/auth/login` (or social OAuth2). The `auth-service` issues a secure HTTP-Only cookie named `payflow_token`.
2. **Gateway Filter intercept:** The `api_gateway` intercepts every incoming REST API call, decodes the JWT signature, and injects user identity into HTTP request headers.
3. **Identity Propagation Filter ([GatewayAuthenticationFilter](./merchant-service/src/main/java/com/pranav/merchant_service/security/GatewayAuthenticationFilter.java)):**
   * Downstream microservices register a filter that intercept incoming HTTP requests (excluding internal `/internal/` service routes).
   * It reads `X-User-Id`, `X-User-Email`, and `X-User-Role` headers.
   * It populates the Spring Security `SecurityContextHolder` with a pre-authenticated `UsernamePasswordAuthenticationToken` using the gateway-provided role.
   * This enables downstream controllers to enforce authorization logic cleanly via Spring annotation standard: `@PreAuthorize("hasRole('MERCHANT')")`.
4. **Secure Inter-Service Communication:** Feign clients use a configuration (`FeignConfig`) that automatically adds a security header `X-Internal-Api-Key` matching the downstream's secret key (`internal.api-key`), protecting internal loop calls from spoofing.

---

## ⚙️ Configuration & Environment Variables

All critical configurations are loaded from a root-level `.env` file. These values are mapped into service `application.yaml` configurations.

### 📝 Key Environment Fields

```bash
# ── Databases ─────────────────────────────────────────────────────────────────
USER_DB_HOST=payflow-pranavdeshmukh5454-95ff.h.aivencloud.com
USER_DB_PORT=21782
USER_DB_USERNAME=********
USER_DB_PASSWORD=********
USER_DB_NAME=defaultdb

# ── JWT Security ──────────────────────────────────────────────────────────────
JWT_SECRET=********
JWT_EXPIRATION=86400000

# ── Google & GitHub OAuth2 ───────────────────────────────────────────────────
GOOGLE_CLIENT_ID=********
GOOGLE_CLIENT_SECRET=********
GITHUB_CLIENT_ID=********
GITHUB_CLIENT_SECRET=********

# ── Inter-Service Authentication ─────────────────────────────────────────────
INTERNAL_API_KEY=********

# ── Redis Cache (Upstash) ────────────────────────────────────────────────────
REDIS_HOST=rediss://default:********@key-treefrog-145301.upstash.io
REDIS_PORT=6379

# ── RabbitMQ Broker (CloudAMQP) ──────────────────────────────────────────────
RABBITMQ_URL=amqps://lqpexrwc:********@beaver.rmq.cloudamqp.com/lqpexrwc

# ── Razorpay Gateway Integrations ────────────────────────────────────────────
RAZORPAY_KEY_ID=********
RAZORPAY_KEY_SECRET=********
RAZORPAY_WEBHOOK_SECRET=********

# ── Email SMTP (Brevo) ────────────────────────────────────────────────────────
BREVO_SMTP_HOST=smtp-relay.brevo.com
BREVO_SMTP_PORT=587
BREVO_SMTP_USERNAME=********
BREVO_SMTP_PASSWORD=********
NOTIFICATION_FROM_EMAIL=pranavdeshmukh5454@gmail.com
```

---

## 🚀 Running the Project Locally

### 📋 Prerequisites
* **Java SDK 17** or higher
* **Maven 3.8+**
* **Docker & Docker Compose** (optional: for local Redis/Kafka setups)

### 🏗️ Step 1: Clone and Build
Build the Maven dependencies for all microservices:
```bash
# Build the entire Maven project
mvn clean install -DskipTests
```

### 📦 Step 2: Spin Up Infrastructure Containers (Optional)
If you want to run Redis or Kafka locally instead of using cloud-managed Upstash/Aiven endpoints:
```bash
docker-compose up -d
```
> **Note:** The local `docker-compose.yml` configures local Redis (port `6379`) and a Kafka environment.

### 🏃 Step 3: Run the Services
Run each service in the following order:
1. **`eureka_server`:** Wait for registration port `8761` to go active.
2. **`api_gateway`:** Start gateway listener on port `8080`.
3. **`auth-service`**, **`merchant-service`**, **`payment-service`**, **`wallet-service`**, **`notification-service`**.

You can start each service by navigating to its subfolder and executing:
```bash
mvn spring-boot:run
```

---

## 📖 Interactive Swagger API Documentation

PayFlow microservices expose rich OpenAPI documentation. Thanks to the API Gateway integration, all service docs are aggregated into a single Swagger UI console:

* **Centralized API documentation URL:** `http://localhost:8080/swagger-ui.html`

Use the dropdown selector in the top-right corner of the Swagger UI to switch documentation views between:
* **Auth Service API Specs**
* **Merchant Service API Specs**
* **Payment Service API Specs**
* **Wallet Service API Specs**
* **Notification Service API Specs**

---

## 📈 System Resilience & Fallback Metrics

Downstream service health endpoints expose circuit breaker status metrics:
* **Gateway Health Status:** `http://localhost:8080/actuator/health`
* **Payment Service Circuit Health:** `http://localhost:8083/actuator/health`

When testing fallbacks, if the merchant service is killed manually:
1. The Payment Service circuit transitions from `CLOSED` to `OPEN`.
2. New payment creation endpoints immediately throw `400 Bad Request` with the message `"Merchant Service is currently unavailable. Please try again in a few moments."` instead of waiting for a Feign connection timeout.
3. Once the merchant service is brought back online, the circuit moves to `HALF-OPEN` during trial calls, then resets back to `CLOSED`.
