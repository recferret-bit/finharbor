# Payment Service

Payment Service for Casino Backend - Handles payment processing, payment status queries, and payment history.

## Overview

The Payment Service is a microservice responsible for:
- Processing payment transactions
- Managing payment status and history
- Publishing payment events to Kafka (`core.payment.initiated`, `core.payment.completed`, `core.payment.failed`)

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **PostgreSQL** (database)
- **Apache Kafka** (event streaming)
- **OpenAPI 3.0** (API specification)

## API Documentation

The service exposes a REST API documented with OpenAPI 3.0. Access the interactive API documentation at:
- Swagger UI: `http://localhost:8080/api/payments/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/payments/api-docs`

## Endpoints

### POST `/api/payments/process`
Process a payment transaction.

**Request:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "amount": 100.50,
  "currency": "USD",
  "paymentMethod": "CREDIT_CARD",
  "paymentMethodId": "card_token_123",
  "description": "Deposit to account"
}
```

**Response:**
```json
{
  "paymentId": "660e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "amount": 100.50,
  "currency": "USD",
  "transactionId": "TXN-ABC12345",
  "timestamp": "2026-01-31T10:30:00"
}
```

### GET `/api/payments/{paymentId}`
Get payment details by ID.

### GET `/api/payments/history/{userId}`
Get payment history for a user.

**Query Parameters:**
- `limit` (default: 20, max: 100) - Number of payments to return
- `offset` (default: 0) - Number of payments to skip

### GET `/api/payments/health`
Health check endpoint.

## Kafka Events

The service publishes the following events to Kafka:

### `core.payment.initiated`
Published when a payment is initiated.

### `core.payment.completed`
Published when a payment is successfully completed.

### `core.payment.failed`
Published when a payment fails.

## Configuration

### Environment Variables

- `DB_URL` - PostgreSQL database URL (default: `jdbc:postgresql://localhost:5432/casino_payments`)
- `DB_USERNAME` - Database username (default: `postgres`)
- `DB_PASSWORD` - Database password (default: `postgres`)
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka bootstrap servers (default: `localhost:9092`)
- `SERVER_PORT` - Server port (default: `8080`)

### Application Properties

See `src/main/resources/application.yml` for configuration options.

## Building and Running

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Apache Kafka 3.0+

### Build

```bash
mvn clean package
```

### Run Locally

```bash
# Start PostgreSQL and Kafka (using Docker Compose or locally)

# Run the service
mvn spring-boot:run
```

### Docker

```bash
# Build image
docker build -t casino/payment-service:2.5.1 .

# Run container
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/casino_payments \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=postgres \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  casino/payment-service:2.5.1
```

## Database Migration

The service uses Flyway for database migrations. Migration scripts are located in `src/main/resources/db/migration/`.

To run migrations manually:
```bash
mvn flyway:migrate
```

## Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Deployment

### Kubernetes

Deploy to Kubernetes using the manifests in `k8s/`:

```bash
# Apply secrets (create secrets.yaml with actual values first)
kubectl apply -f k8s/secrets.yaml

# Apply deployment
kubectl apply -f k8s/deployment.yaml
```

### CI/CD

The service is automatically deployed via GitHub Actions when changes are merged to the `main` branch.

## Security

- All sensitive data (API keys, passwords) must be stored in environment variables or Kubernetes secrets
- Never commit secrets to version control
- Use HTTPS in production
- Implement proper authentication/authorization (JWT tokens)

## Monitoring

- Health endpoint: `/api/payments/health`
- Actuator endpoints: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`

## Team

**Owned by:** Core Team Backend  
**Code Review:** Requires approval from `@core-team-backend`

## Version

Current version: **2.5.1**

## License

Proprietary - Casino Backend Project
