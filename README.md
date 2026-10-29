# Secure Banking System

A microservices-based digital banking platform built with **Spring Boot**, **React**, **MariaDB**, **Apache Kafka**, and **Eureka**.

It supports secure customer onboarding, KYC verification, accounts, money transfers, fixed and recurring deposits, statements, analytics, and event-driven email and SMS notifications.

The project focuses on the problems that matter when handling money: **duplicate requests, concurrent balance updates, partial failures, and credential security**.

---

## Features

### Security
- **SSL/TLS** on all services, including service-to-service calls
- **BCrypt** password hashing
- **JWT** stateless authentication with role-based access (`CUSTOMER`, `EMPLOYEE`, `ADMIN`)
- **Email OTP** verification during registration (SecureRandom, 5-minute expiry, single use)
- **Passkey authentication** (WebAuthn / FIDO2 via WebAuthn4J)
- **Password re-verification** before every transfer

### Banking
- Customer onboarding with **KYC verification by bank employees**
- Savings accounts, deposits, and withdrawals
- **Secure fund transfers**
- **Fixed Deposits** and **Recurring Deposits** with installment tracking
- **Account statements** and **dashboard analytics**

### Reliability
- **Idempotent operations** for transfers, FD creation, RD creation, and RD installments
- **Pessimistic row locking** (`SELECT ... FOR UPDATE`)
- **Deterministic lock ordering** to prevent deadlocks
- **Atomic transactions** for debit, credit, and records

### Event-Driven Notifications
- **Kafka** `TRANSFER_COMPLETED` events published after commit
- **In-app**, **email** (Gmail SMTP), and **SMS** (Twilio) notifications for sender and recipient
- Duplicate-safe consumer with isolated channels

---

## Architecture

```text
                React Frontend (Vite)  :5173
                          │ HTTPS
                          ▼
                API Gateway  :9192
             (JWT validation, routing, CORS)
                          │ lb://SERVICE via Eureka
                          ▼
   ┌────────────────────────────────────────────────┐
   │ Auth Service          :8903  login, OTP, passkey│
   │ Banking System        :8900  customers, KYC     │
   │ Account Service       :8901  accounts, FD, RD   │
   │ Transaction Service   :8902  deposits, transfers│
   │ Notification Service  :8906  Kafka consumer     │
   └────────────────────────────────────────────────┘
          │                              ▲
          ▼                              │
     MariaDB :3307        Kafka :9092 ───┘
                          topic: banking.transfer.completed.v1

                Eureka Server  :8761
```

### Transfer Flow

```text
POST /api/transactions/transfer
  → Gateway validates JWT, routes via Eureka
  → Transaction Service (@Transactional)
      1. Verify password (Feign → Auth Service)
      2. Check idempotency key → return original result if repeated
      3. Resolve account numbers to IDs
      4. Lock both accounts in ascending ID order (FOR UPDATE)
      5. Validate balance, debit, credit
      6. Save transfer and idempotency record
  → Commit
  → Kafka event published after commit
  → Notification Service: in-app + email + SMS
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.0.5, Spring Cloud 2025.1.0 |
| Security | Spring Security, JWT, BCrypt, WebAuthn4J, SSL/TLS |
| Data | MariaDB 10.6, Spring Data JPA, Hibernate |
| Messaging | Apache Kafka (Docker) |
| Discovery | Eureka, Spring Cloud Gateway, OpenFeign |
| Notifications | Spring Mail (Gmail SMTP), Twilio SDK |
| Frontend | React, Vite, Recharts |

---

## Project Structure

```text
banking-system/
├── Backend/
│   ├── eureka-server/          # Service discovery
│   ├── api-gateway/            # Routing and JWT validation
│   ├── authservice/            # Login, JWT, OTP, passkeys
│   ├── Banking-System-2/       # Customers, employees, KYC
│   ├── accountservice/         # Accounts, FD, RD
│   ├── transactionservice/     # Deposits, withdrawals, transfers
│   └── notification-service/   # Kafka consumer, email, SMS
├── frontend/                   # React + Vite app
├── docker-compose.kafka.yml    # Local Kafka broker
└── README.md
```

---

## Prerequisites

| Tool | Version |
|---|---|
| JDK | 21+ |
| Maven | 3.9+ (or the `mvnw` wrapper) |
| Node.js | 18+ |
| MariaDB | 10.6+ |
| Docker Desktop | Latest (for Kafka) |

---

## Setup

### 1. Database

Start MariaDB on port `3307` and create the databases:

```sql
CREATE DATABASE bankingsystem;
CREATE DATABASE bankingsystem_test;
```

### 2. SSL Certificates (first time only)

Keystores are **not committed** for security reasons. Generate a self-signed certificate for `localhost` and copy it into every service:

```bash
keytool -genkeypair -alias bankingapp -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -storepass password -validity 3650 \
  -dname "CN=localhost, OU=Banking, O=BankingSystem, C=IN" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"

keytool -exportcert -alias bankingapp -keystore keystore.p12 \
  -storepass password -file bankingapp.crt

keytool -importcert -noprompt -alias bankingapp -file bankingapp.crt \
  -keystore truststore.p12 -storetype PKCS12 -storepass password

for s in api-gateway authservice accountservice transactionservice Banking-System-2; do
  cp keystore.p12 truststore.p12 "Backend/$s/src/main/resources/"
done
```

Using one shared certificate lets every service trust the others.

### 3. Environment Variables

Secrets are read from environment variables. **Never commit them.**

```dotenv
DB_USERNAME=root
DB_PASSWORD=your-db-password
JWT_SECRET=a-random-secret-at-least-32-characters-long
KEYSTORE_PASSWORD=password
```

**Notification Service** — copy `Backend/notification-service/.env.example` to `.env`:

```dotenv
SMTP_USERNAME=your-sender@gmail.com
SMTP_PASSWORD=your-gmail-app-password
MAIL_FROM=your-sender@gmail.com
MAIL_ENABLED=true

TWILIO_ACCOUNT_SID=
TWILIO_AUTH_TOKEN=
TWILIO_PHONE_NUMBER=
SMS_ENABLED=false
SMS_DEFAULT_COUNTRY_CODE=+91
```

> Gmail requires an **App Password** (2-Step Verification enabled).
> Twilio trial accounts only allow verified recipients and predefined templates, so keep `SMS_ENABLED=false` unless upgraded.

### 4. Passkey Settings

In `authservice` `application.yaml`, `origin` must match the frontend URL:

```yaml
webauthn:
  rpId: "localhost"
  rpName: "Banking App"
  origin: "http://localhost:5173"
```

---

## Running the Project

Start in this order.

**1. MariaDB**

```bash
mysqld --port=3307 --console
```

**2. Kafka** (Docker Desktop running)

```bash
docker compose -f docker-compose.kafka.yml up -d
```

**3. Backend services** (one terminal each; on Windows use `.\mvnw.cmd`)

```bash
cd Backend/eureka-server        && ./mvnw spring-boot:run
cd Backend/api-gateway          && ./mvnw spring-boot:run
cd Backend/authservice          && ./mvnw spring-boot:run
cd Backend/Banking-System-2     && ./mvnw spring-boot:run
cd Backend/accountservice       && ./mvnw spring-boot:run
cd Backend/notification-service && ./mvnw spring-boot:run
cd Backend/transactionservice   && ./mvnw spring-boot:run
```

Check **http://localhost:8761** to confirm all services are registered.

**4. Frontend**

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. Visit `https://localhost:9192` once and accept the self-signed certificate so API calls work.

### Ports

| Service | Port |
|---|---|
| Frontend | 5173 |
| API Gateway | 9192 |
| Eureka | 8761 |
| Banking System | 8900 |
| Account Service | 8901 |
| Transaction Service | 8902 |
| Auth Service | 8903 |
| Notification Service | 8906 |
| Kafka | 9092 |
| MariaDB | 3307 |

---

## Key Engineering Decisions

**Idempotency** — Every money-moving request carries a client-generated UUID. A retried request returns the original result without moving money or publishing another event. A unique constraint guards concurrent duplicates.

**Pessimistic locking** — Account rows are locked with native `SELECT ... FOR UPDATE` inside transactions. Native SQL is used because Hibernate's `@Lock` generated `FOR UPDATE OF <alias>`, which MariaDB rejects.

**Deadlock prevention** — Transfers lock the lower account ID before the higher one, so opposite-direction transfers never deadlock.

**After-commit events** — Kafka events are sent from `@TransactionalEventListener(AFTER_COMMIT)`, so rolled-back transfers never trigger notifications.

**Notification reliability** — A unique `(event_id, user_id, notification_type)` constraint prevents duplicates on redelivery, and each email/SMS call is isolated so one failing provider never affects the others. Accounts, emails, and phone numbers are masked.

**Money** — All amounts use `BigDecimal`.

---

## API Overview

All requests go through `https://localhost:9192`.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register |
| POST | `/auth/login` | Login, returns JWT |
| POST | `/auth/send-otp?email=` | Send OTP |
| POST | `/auth/verify-otp` | Verify OTP |
| POST | `/auth/passkeys/registration/options` | Begin passkey registration |
| POST | `/auth/passkeys/registration/verify` | Finish passkey registration |
| POST | `/auth/passkeys/authentication/options` | Begin passkey login |
| POST | `/auth/passkeys/authentication/verify` | Finish passkey login |
| POST | `/api/transactions/transfer` | Idempotent transfer |
| GET | `/api/transactions/statement` | Statement by date range |
| GET | `/accounts/customer/{customerId}` | Customer accounts |
| POST | `/deposits/fixed/{accountId}` | Create FD |
| GET | `/deposits/fixed/account/{accountId}` | List FDs |
| POST | `/deposits/recurring/{accountId}` | Create RD |
| POST | `/deposits/recurring/{rdId}/pay` | Pay RD installment |
| GET | `/notifications/user/{userId}` | Notifications |

---

## Testing

```bash
cd Backend/transactionservice
./mvnw test
./mvnw -Dtest=TransferConcurrencyIntegrationTest test
```

The concurrency test fires two simultaneous ₹700 transfers against a ₹1000 balance and verifies that exactly one succeeds, the other fails with insufficient balance, and the final balance is ₹300.

Inspect Kafka events:

```bash
docker exec -it banking-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic banking.transfer.completed.v1 --from-beginning
```

---

## Known Limitations and Roadmap

| Area | Current | Planned |
|---|---|---|
| DB + Kafka consistency | After-commit publish can lose an event on crash | Transactional outbox |
| OTP / passkey challenges | In-memory (single instance) | Redis with TTL |
| Balance ownership | Two services write `accounts` | Single owner; locking for FD/RD debits |
| Email/SMS retries | Logged only | Delivery table with retry worker |
| Resilience | No circuit breakers | Resilience4j |
| SMS | Twilio trial limits | Upgraded account or Indian provider |

---

## Author

**Dikshant Singh** — B.Tech Information Technology, Chaitanya Bharathi Institute of Technology
