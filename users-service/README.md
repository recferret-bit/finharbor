# Users Service

Casino Backend - Users Service

## Overview

The Users Service is a microservice responsible for managing user accounts, profiles, and authentication data. It is owned by the **Core Team** and provides REST APIs for user management operations.

## Features

- User CRUD operations (Create, Read, Update, Delete)
- User status management (ACTIVE, INACTIVE, SUSPENDED, BANNED)
- KYC status tracking (NOT_VERIFIED, PENDING, VERIFIED, REJECTED)
- Kafka event publishing for user lifecycle events
- OpenAPI 3.0 specification
- PostgreSQL database integration
- Health checks and metrics endpoints

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL**
- **Apache Kafka**
- **OpenAPI 3.0** (Swagger/SpringDoc)
- **Docker**
- **Kubernetes**

## API Documentation

- **OpenAPI Spec**: `docs/openapi/user-service-v1.yaml`
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

## API Endpoints

### Base URL
- Production: `https://api.example.com/v1`
- Staging: `https://api-staging.example.com/v1`
- Local: `http://localhost:8080/v1`

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/users` | List users (paginated) |
| GET | `/v1/users/{userId}` | Get user by ID |
| POST | `/v1/users` | Create new user |
| PUT | `/v1/users/{userId}` | Update user |
| DELETE | `/v1/users/{userId}` | Delete user |

## Consumers

This service is used by:

- **Retail Team**: `checkout-service`, `cart-service`
- **AML Team**: `transaction-monitor-service`
- **Redesign Team**: Frontend web app

## Kafka Events

The service publishes the following events to the `user-events` topic:

- **UserCreatedEvent**: Published when a new user is created
- **UserUpdatedEvent**: Published when a user is updated

### Event Schema

```json
{
  "userId": "uuid",
  "email": "string",
  "username": "string",
  "status": "ACTIVE|INACTIVE|SUSPENDED|BANNED",
  "kycStatus": "NOT_VERIFIED|PENDING|VERIFIED|REJECTED",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/casino_users` |
| `DATABASE_USERNAME` | Database username | `casino_user` |
| `DATABASE_PASSWORD` | Database password | `casino_password` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `localhost:9092` |
| `SERVER_PORT` | Server port | `8080` |
| `LOG_LEVEL` | Logging level | `INFO` |

## Local Development

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Apache Kafka (or Docker Compose)

### Running Locally

1. **Start PostgreSQL**:
   ```bash
   docker run -d --name postgres \
     -e POSTGRES_DB=casino_users \
     -e POSTGRES_USER=casino_user \
     -e POSTGRES_PASSWORD=casino_password \
     -p 5432:5432 \
     postgres:14
   ```

2. **Run database migrations**:
   ```bash
   psql -h localhost -U casino_user -d casino_users -f db/migration/V1__create_users_table.sql
   ```

3. **Start Kafka** (if not using Docker Compose):
   ```bash
   # Follow Kafka setup instructions
   ```

4. **Build the application**:
   ```bash
   mvn clean package
   ```

5. **Run the application**:
   ```bash
   java -jar target/users-service-1.0.0.jar
   ```

   Or with Maven:
   ```bash
   mvn spring-boot:run
   ```

### Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

## Docker

### Build Docker Image

```bash
docker build -t users-service:latest .
```

### Run with Docker

```bash
docker run -d \
  --name users-service \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/casino_users \
  -e DATABASE_USERNAME=casino_user \
  -e DATABASE_PASSWORD=casino_password \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  users-service:latest
```

## Kubernetes Deployment

### Deploy to Kubernetes

```bash
# Apply ConfigMap
kubectl apply -f kubernetes/configmap.yaml

# Create secrets (replace with actual values)
kubectl create secret generic users-service-secrets \
  --from-literal=database-url='jdbc:postgresql://postgres:5432/casino_users' \
  --from-literal=database-username='casino_user' \
  --from-literal=database-password='casino_password'

# Deploy service
kubectl apply -f kubernetes/deployment.yaml
```

### Health Checks

- **Liveness**: `http://localhost:8080/actuator/health/liveness`
- **Readiness**: `http://localhost:8080/actuator/health/readiness`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`

## Database Schema

### Users Table

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    kyc_status VARCHAR(20) NOT NULL DEFAULT 'NOT_VERIFIED',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## API Versioning

This service follows API versioning best practices:

- **Current Version**: v1
- **Version in URL**: `/v1/users`
- **Backward Compatibility**: Maintained for mobile clients
- **OpenAPI Spec**: Versioned in `docs/openapi/user-service-v1.yaml`

## Team Ownership

- **Team**: Core Team
- **Owners**: @core-team-backend
- **Service Path**: `/services/user-service/`

## Contributing

1. Follow the project's Git branching strategy
2. Create OpenAPI spec before implementation
3. Write tests for new features
4. Update documentation
5. Follow code review process

## License

Proprietary - Casino Project
