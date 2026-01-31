# Casino Balance Service

Balance management microservice for the Casino Backend project.

## Overview

This service provides RESTful APIs for managing user balances, processing deposits and withdrawals, and retrieving transaction history. It follows microservices best practices with OpenAPI specifications, Kafka event-driven communication, and PostgreSQL for data persistence.

## Features

- **Balance Management**: Get user balance, deposit funds, withdraw funds
- **Transaction History**: Paginated transaction history with filtering
- **Event-Driven**: Publishes balance update events to Kafka
- **OpenAPI 3.0**: Complete API specification for client integration
- **Database Transactions**: Pessimistic locking for concurrent balance updates
- **Idempotency**: Prevents duplicate transactions using external transaction IDs

## Technology Stack

- **Java 17**: Programming language
- **Spring Boot 3.2.0**: Application framework
- **Spring Data JPA**: Database access
- **PostgreSQL**: Relational database
- **Apache Kafka**: Event streaming
- **OpenAPI 3.0**: API specification
- **Maven**: Build tool

## API Endpoints

### Get Balance
```
GET /api/v1/balance/balance/{userId}
```

### Deposit Funds
```
POST /api/v1/balance/balance/{userId}/deposit
Content-Type: application/json

{
  "amount": 100.00,
  "currency": "USD",
  "transactionId": "txn_123456789",
  "description": "Deposit via credit card"
}
```

### Withdraw Funds
```
POST /api/v1/balance/balance/{userId}/withdraw
Content-Type: application/json

{
  "amount": 50.00,
  "currency": "USD",
  "transactionId": "txn_987654321",
  "description": "Withdrawal to bank account"
}
```

### Get Transaction History
```
GET /api/v1/balance/balance/{userId}/transactions?page=0&size=20&transactionType=DEPOSIT
```

### Health Check
```
GET /api/v1/balance/health
```

## OpenAPI Documentation

Once the service is running, access the Swagger UI at:
- http://localhost:8080/api/v1/balance/swagger-ui.html
- API Docs: http://localhost:8080/api/v1/balance/api-docs

## Database Schema

### balances
- `id` (UUID, Primary Key)
- `user_id` (UUID, Unique, Indexed)
- `balance` (DECIMAL(19,2))
- `currency` (VARCHAR(3))
- `last_updated` (TIMESTAMP)
- `version` (BIGINT, for optimistic locking)

### transactions
- `id` (UUID, Primary Key)
- `user_id` (UUID, Indexed)
- `amount` (DECIMAL(19,2))
- `currency` (VARCHAR(3))
- `transaction_type` (ENUM: DEPOSIT, WITHDRAWAL, BET, WIN, REFUND, BONUS)
- `status` (ENUM: PENDING, COMPLETED, FAILED, CANCELLED)
- `description` (VARCHAR(500))
- `timestamp` (TIMESTAMP, Indexed)
- `balance_after` (DECIMAL(19,2))
- `external_transaction_id` (VARCHAR, Unique, Indexed)

## Kafka Events

The service publishes `BALANCE_UPDATED` events to the `balance-updated` topic:

```json
{
  "userId": "uuid",
  "balance": "1000.50",
  "currency": "USD",
  "transactionId": "uuid",
  "timestamp": 1234567890,
  "eventType": "BALANCE_UPDATED"
}
```

## Configuration

Environment variables:

- `DATABASE_URL`: PostgreSQL connection URL (default: `jdbc:postgresql://localhost:5432/casino_db`)
- `DATABASE_USERNAME`: Database username (default: `casino_user`)
- `DATABASE_PASSWORD`: Database password (default: `casino_pass`)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers (default: `localhost:9092`)
- `KAFKA_TOPIC_BALANCE_UPDATED`: Kafka topic for balance updates (default: `balance-updated`)

## Building and Running

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 15+
- Apache Kafka (or use docker-compose)

### Local Development

1. Start dependencies:
```bash
docker-compose up -d postgres kafka zookeeper
```

2. Build the project:
```bash
mvn clean package
```

3. Run the service:
```bash
mvn spring-boot:run
```

### Docker

Build and run with Docker Compose:
```bash
docker-compose up --build
```

Or build the image separately:
```bash
docker build -t balance-service .
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/casino_db \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  balance-service
```

## Database Migrations

The service uses JPA with `ddl-auto: validate`. Database schema should be managed through migration tools (Flyway/Liquibase) in production. For development, you can use:

```sql
CREATE TABLE balances (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    balance DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    version BIGINT NOT NULL
);

CREATE INDEX idx_user_id ON balances(user_id);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    timestamp TIMESTAMP NOT NULL,
    balance_after DECIMAL(19,2) NOT NULL,
    external_transaction_id VARCHAR(255) UNIQUE
);

CREATE INDEX idx_user_id ON transactions(user_id);
CREATE INDEX idx_transaction_type ON transactions(transaction_type);
CREATE INDEX idx_timestamp ON transactions(timestamp);
CREATE INDEX idx_external_transaction_id ON transactions(external_transaction_id);
```

## Testing

Run unit tests:
```bash
mvn test
```

## Error Handling

The service returns standardized error responses:

```json
{
  "error": "INSUFFICIENT_FUNDS",
  "message": "Insufficient funds. Current balance: 50.00, Requested: 100.00",
  "timestamp": "2025-01-31T12:00:00",
  "path": "/api/v1/balance/balance/user-123/withdraw"
}
```

Error codes:
- `USER_NOT_FOUND`: User does not exist (404)
- `INSUFFICIENT_FUNDS`: Insufficient balance for withdrawal (400)
- `INVALID_REQUEST`: Invalid input or duplicate transaction (400)
- `INTERNAL_SERVER_ERROR`: Unexpected server error (500)

## Security Considerations

- Implement authentication/authorization middleware
- Add rate limiting for API endpoints
- Encrypt sensitive data at rest
- Use HTTPS in production
- Validate and sanitize all inputs
- Implement audit logging

## License

Proprietary - Casino Backend Project
