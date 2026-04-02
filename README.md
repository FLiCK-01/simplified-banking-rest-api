# MiniBank REST API

**Author:** Mihai Cosmagiu

## Overview
MiniBank is a robust, enterprise-grade RESTful banking API built using Java 25 and Spring Boot 4. It simulates core banking operations, including account management, secure multi-currency money transfers, and immutable ledger tracking. 

The system enforces strict real-world business rules, ensures concurrent transaction safety, and provides comprehensive error handling via a standardized JSON format.

## Key Features
* **Account Management:** Create and query `CHECKING` and `SAVINGS` accounts.
* **Transfer Engine:** Move funds between accounts with strict structural IBAN validation and SEPA-country enforcement using Apache Commons.
* **Dynamic Currency Conversion:** Seamlessly handles transfers between RON, EUR, USD, and GBP using configuration-injected exchange rates and precise `HALF_EVEN` banker's rounding.
* **Daily Transfer Limits:** Enforces a strict 5,000 EUR-equivalent daily transfer limit exclusively on `SAVINGS` accounts, calculating cross-currency boundaries dynamically in UTC.
* **Immutable Ledger:** Automatically generates double-entry `Transaction` records (`DEPOSIT`, `WITHDRAWAL`, `TRANSFER_IN`, `TRANSFER_OUT`) for full auditability.
* **Concurrency Safety:** Utilizes JPA `@Lock(LockModeType.PESSIMISTIC_WRITE)` database locks to prevent race conditions and ensure data integrity during simultaneous account transfers.
* **Idempotency:** Supports idempotency keys on transfer requests to safely prevent duplicate transactions during network retries.
* **Global Exception Handling:** Catches all business rule violations, routing them through a `@RestControllerAdvice` to guarantee a consistent `{"status": "REJECTED", "message": "..."}` response structure.

## Technology Stack
* **Language:** Java 25
* **Framework:** Spring Boot 4.0.3
* **Database & ORM:** H2 In-Memory Database, Spring Data JPA, Hibernate
* **Validation:** Jakarta Validation API, Hibernate Validator, Apache Commons Validator
* **Code Generation:** MapStruct 1.6.3, Lombok

## Getting Started

### Prerequisites
* Java 25+
* Maven 3.8+

### Running the Application
1. Clone the repository and navigate to the project root directory.
2. Build and run the project using Maven:
   ```bash
   mvn clean install
   mvn spring-boot:run
