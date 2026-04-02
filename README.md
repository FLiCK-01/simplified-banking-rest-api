# MiniBank REST API: Core Banking Engine

**Author:** Mihai Cosmagiu

## Executive Summary
MiniBank is an enterprise-grade RESTful banking API engineered from scratch using Java 25 and Spring Boot 4. Designed to simulate a high-stakes financial ledger, the system strictly enforces real-world banking rules including SEPA structural validation, dynamic multi-currency conversions, complex daily aggregation limits, and database-level concurrency protection against double-spending.

This project was developed under strict offline-build constraints, relying exclusively on standard architectural patterns, JPA native locking, and approved utility libraries to ensure zero external network dependencies at runtime.

---

## 🏛️ Architecture & Design Patterns

The application enforces a strict **N-Tier Architecture** coupled with the **DTO (Data Transfer Object) Pattern**:
* **Presentation Layer (Controllers):** Handles HTTP routing, delegates JSON payload validation to Jakarta `@Valid`, and strips internal data via DTOs before responding.
* **Business Layer (Services):** The core rules engine. Orchestrates idempotency checks, limit calculations, UTC-normalized boundary checks, and cross-currency math.
* **Data Access Layer (Repositories):** Spring Data JPA interfaces utilizing JPQL and database-level locking mechanisms.
* **Mapping Layer:** MapStruct is utilized for compile-time, reflection-free mapping between internal Database Entities (`Account`, `Transfer`, `Transaction`) and external JSON DTOs, ensuring domain models are never accidentally exposed.

---

## ⚙️ Core Technical Features & Constraints

### 1. Concurrency Control & Race Condition Safety
To prevent catastrophic "dirty reads" or double-spending when multiple transfers hit the same account simultaneously, the engine utilizes **Pessimistic Locking**. 
The `AccountRepository` employs `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`), pushing the concurrency management down to the database level. This guarantees that thread execution is safely serialized during the balance mutation lifecycle.

### 2. Multi-Currency Engine & Precision Math
The system seamlessly routes funds between 4 supported currencies (`RON`, `EUR`, `USD`, `GBP`). 
* **Base Pivot:** All conversions pivot through `RON` as the mathematical base (1.00).
* **Formula:** `convertedAmount = amount * (sourceToRON / targetToRON)`
* **Precision Constraints:** All calculations enforce `BigDecimal` and `RoundingMode.HALF_EVEN` (Banker's Rounding). 
* **Decoupled Scaling:** The `exchangeRate` is calculated and stored to a precision of `scale 6`, while the final `convertedAmount` is explicitly rounded to `scale 2` before being applied to the ledger.

### 3. Dynamic Daily Limits (UTC Aggregation)
`SAVINGS` accounts are restricted to a **5,000 EUR-equivalent** daily outgoing limit. Because accounts may be denominated in different currencies, the engine:
1. Calculates the exact `startOfDay` and `endOfDay` boundary using `ZoneOffset.UTC`.
2. Aggregates all `TRANSFER_OUT` and `WITHDRAWAL` transactions for that specific account within the time boundary.
3. Dynamically converts historical outgoing amounts from their native currency back into EUR using live rates to ensure the cumulative limit is perfectly accurate across currency boundaries.

### 4. Immutable Double-Entry Ledger
Every successful transfer generates an immutable, double-entry audit trail (`Transaction` entities). 
* **System Bank Bypass:** Account ID `1` acts as the infinite funding source. The ledger logic dynamically intercepts transactions involving ID `1`, converting standard `TRANSFER_IN` / `TRANSFER_OUT` records into system `DEPOSIT` / `WITHDRAWAL` records, while enforcing a `null` counterparty IBAN for security.

### 5. Idempotency & Fault Tolerance
Transfer requests support an optional `idempotencyKey`. The business layer intercepts incoming keys against the `TransferRepository`. If a match is found, the system halts execution and safely returns the historical `TransferResponse`, allowing clients to retry network timeouts safely without duplicating charges.

### 6. SEPA & Structural IBAN Validation
Using `commons-validator`, incoming IBANs are subjected to strict Modulo-97 checksum validation. Furthermore, the engine cross-references the extracted country codes against a hardcoded `Set<String>` of official SEPA member states, instantly rejecting non-compliant routing attempts.

### 7. Centralized Exception Handling
The API guarantees a predictable client experience. A global `@RestControllerAdvice` acts as a safety net, intercepting all `BusinessValidationException`, `IllegalArgumentException`, and Jakarta `MethodArgumentNotValidException` errors. It normalizes every failure into a strict HTTP 4xx response:
```json
{
  "status": "REJECTED",
  "message": "<Specific constraint violation description>"
}
```

---

## 🛠️ Technology Stack
* **Language:** Java 25
* **Framework:** Spring Boot 4.0.3 (WebMVC, Data JPA)
* **Database:** H2 In-Memory Database (HikariCP connection pooling)
* **Persistence:** Hibernate ORM 7.x
* **Validation:** Jakarta Validation API, Hibernate Validator, Apache Commons Validator 1.9.0
* **Tooling:** MapStruct 1.6.3, Lombok 1.18.30

---

## 🚀 Getting Started

### Prerequisites
* Java 25+
* Maven 3.8+

### Build & Run
1. Clone the repository and navigate to the project root.
2. Compile the application (This triggers MapStruct and Lombok annotation processors):
   ```bash
   mvn clean install
   ```
3. Start the Spring Boot server:
   ```bash
   mvn spring-boot:run
   ```
4. The API is now listening on `http://localhost:8080`.

*(Note: Upon startup, a `CommandLineRunner` automatically provisions the System Bank account `RO49AAAA1B31007593840000` to allow immediate API testing).*

---

## 📖 API Reference

### Account Endpoints
* `POST /api/accounts` - Provision a new `CHECKING` or `SAVINGS` account.
* `GET /api/accounts/{id}` - Fetch account state and current balance.
* `GET /api/accounts` - Fetch paginated list of all accounts.
* `GET /api/accounts/{id}/transactions` - Fetch the paginated double-entry ledger history for a specific account.

### Transfer Endpoints
* `POST /api/transfers` - Execute a cross-currency, SEPA-validated transfer.
* `GET /api/transfers/{id}` - Fetch a specific transfer execution receipt.
* `GET /api/transfers` - Fetch paginated transfers. Supports query parameters: `iban`, `fromDate`, `toDate`.

### System Endpoints
* `GET /api/exchange-rates` - Fetch configuration-injected currency exchange rates.

---

## 🗄️ Database Inspection
To view the live, real-time ledger and account balances during execution:
1. Open `http://localhost:8080/h2-console` in your browser.
2. Use the following credentials:
   * **JDBC URL:** `jdbc:h2:mem:minibank`
   * **User Name:** `sa`
   * **Password:** *(Leave blank)*
3. Click **Connect** to interact with the raw SQL tables (`ACCOUNTS`, `TRANSFERS`, `TRANSACTIONS`).
