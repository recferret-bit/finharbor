# Games Service

Casino Games Microservice for managing casino games including slots, table games, and live casino.

## Overview

The Games Service is a Spring Boot microservice that provides CRUD operations for game management, game discovery, and filtering capabilities. It follows the microservices architecture pattern with REST API communication and Kafka integration for event-driven messaging.

## Features

- **Game Management**: Create, read, update, and delete casino games
- **Game Discovery**: Search and filter games by type, status, or name
- **Game Types**: Support for slots, table games (blackjack, roulette, poker, baccarat, craps), bingo, lottery, live casino, and virtual sports
- **REST API**: OpenAPI 3.0 compliant RESTful API
- **Database**: PostgreSQL for persistent storage
- **Event-Driven**: Kafka integration for asynchronous messaging
- **Health Monitoring**: Spring Boot Actuator for health checks and metrics
- **API Documentation**: Swagger UI for interactive API documentation

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **PostgreSQL**
- **Apache Kafka**
- **OpenAPI 3.0 / Swagger**
- **Maven**

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL 15+ (if running locally)
- Apache Kafka (if running locally)

## Quick Start

### Using Docker Compose

The easiest way to run the service with all dependencies:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Zookeeper and Kafka
- Games Service application

The service will be available at `http://localhost:8080`

### Local Development

1. **Start dependencies** (PostgreSQL and Kafka):
   ```bash
   docker-compose up -d postgres zookeeper kafka
   ```

2. **Build the application**:
   ```bash
   ./mvnw clean package
   ```

3. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

Or use your IDE to run `GamesServiceApplication.java`

## API Documentation

Once the service is running, you can access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **OpenAPI YAML**: See `docs/openapi/games-service-v1.yaml`

## API Endpoints

### Games

- `GET /api/v1/games` - Get all games (with optional filters)
- `GET /api/v1/games/{id}` - Get game by database ID
- `GET /api/v1/games/game-id/{gameId}` - Get game by unique gameId
- `POST /api/v1/games` - Create a new game
- `PUT /api/v1/games/{id}` - Update a game
- `DELETE /api/v1/games/{id}` - Delete a game

### Health

- `GET /health` - Health check endpoint
- `GET /actuator/health` - Spring Boot Actuator health endpoint

## Example API Calls

### Create a Game

```bash
curl -X POST http://localhost:8080/api/v1/games \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": "slot-mega-fortune-001",
    "name": "Mega Fortune",
    "description": "A progressive jackpot slot game with 5 reels and 20 paylines",
    "type": "SLOT",
    "status": "ACTIVE",
    "minBet": 0.10,
    "maxBet": 100.00,
    "rtp": 96.5,
    "provider": "NetEnt",
    "thumbnailUrl": "https://cdn.casino.example.com/games/mega-fortune-thumb.jpg"
  }'
```

### Get All Games

```bash
curl http://localhost:8080/api/v1/games
```

### Get Games by Type

```bash
curl http://localhost:8080/api/v1/games?type=SLOT
```

### Search Games

```bash
curl http://localhost:8080/api/v1/games?search=mega
```

## Configuration

Configuration is managed through `application.yml` and environment variables:

| Environment Variable | Description | Default |
|---------------------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/casino_games` |
| `DATABASE_USERNAME` | Database username | `postgres` |
| `DATABASE_PASSWORD` | Database password | `postgres` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `localhost:9092` |
| `SERVER_PORT` | Server port | `8080` |
| `DDL_AUTO` | Hibernate DDL mode | `update` |
| `LOG_LEVEL` | Application log level | `INFO` |

## Database Schema

The service uses a single `games` table with the following key fields:

- `id` - Primary key (auto-generated)
- `game_id` - Unique game identifier
- `name` - Game name
- `description` - Game description
- `type` - Game type (enum)
- `status` - Game status (enum)
- `min_bet` - Minimum bet amount
- `max_bet` - Maximum bet amount
- `rtp` - Return to Player percentage
- `provider` - Game provider name
- `thumbnail_url` - Thumbnail image URL
- `created_at` - Creation timestamp
- `updated_at` - Last update timestamp

## Game Types

- `SLOT` - Slot machines
- `BLACKJACK` - Blackjack table games
- `ROULETTE` - Roulette games
- `POKER` - Poker games
- `BACCARAT` - Baccarat games
- `CRAPS` - Craps games
- `BINGO` - Bingo games
- `LOTTERY` - Lottery games
- `LIVE_CASINO` - Live dealer casino games
- `VIRTUAL_SPORTS` - Virtual sports betting

## Game Status

- `ACTIVE` - Game is available for play
- `INACTIVE` - Game is not available
- `MAINTENANCE` - Game is under maintenance
- `COMING_SOON` - Game will be available soon

## Building for Production

```bash
./mvnw clean package -DskipTests
```

The JAR file will be created in `target/games-service-1.0.0.jar`

## Docker Build

```bash
docker build -t games-service:1.0.0 .
```

## Deployment

The service is designed to be deployed on Kubernetes. See the main project documentation for deployment strategies and CI/CD pipeline configuration.

## Monitoring

The service exposes Spring Boot Actuator endpoints:

- `/actuator/health` - Health check
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

## Testing

```bash
./mvnw test
```

## Contributing

This service follows the microservices architecture patterns documented in the main project. When making changes:

1. Update the OpenAPI specification in `docs/openapi/games-service-v1.yaml`
2. Follow the service lock protocol if modifying shared services
3. Update version numbers for breaking changes
4. Add appropriate tests

## License

Part of the Casino Backend project.
