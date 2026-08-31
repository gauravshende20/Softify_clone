# Harmonia Development Guide

## Prerequisites

- Java 21 (Temurin recommended)
- Maven 3.9+
- Node.js 22 and npm (frontend)
- Docker / Docker Compose
- Optional: kubectl for Kubernetes manifests

## Clone and configure

```bash
cp .env.example .env
# set JWT_SECRET to >= 32 random characters before sharing the stack
docker compose up -d
```

Default local credentials are development-only (`harmonia` / `harmonia` for MySQL and MinIO).

## Build and test

```bash
mvn -B test
cd frontend && npm ci && npm test && npm run build
```

Auth, analytics, and notification include focused unit tests. Other modules currently assert that the Spring Boot application class is present so the reactor stays green.

## Run a service

Typical order: Eureka → Config Server → Auth → remaining services → Gateway → frontend.

IDE: run `*Application` with env:

```
JWT_SECRET=local-dev-only-change-me-use-env-jwt-secret-32b
KAFKA_BOOTSTRAP_SERVERS=localhost:19092
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka
CONFIG_SERVER_URI=http://localhost:8888
```

Config is optional (`optional:configserver:...`); `application.yml` in each module has local defaults.

## Ports

8888 config, 8761 Eureka, 8080 gateway, 8081–8089 domain services, 3306 MySQL, 6379 Redis, 19092 Kafka (host), 9200 OpenSearch, 9000/9001 MinIO, 9090 Prometheus, 3000 Grafana, 4200 frontend.

## Adding a Flyway migration

Put `V{n}__description.sql` in `src/main/resources/db/migration` of the owning service. Never edit applied versions. Keep Hibernate `ddl-auto: validate`.

## Publishing an event

```java
events.publish(Topics.PLAYBACK, DomainEvent.of(
        EventType.PLAYBACK_STARTED, "Track", trackId.toString(),
        "playback-service", MDC.get("traceId"), userId.toString(),
        Map.of("trackId", trackId, "artistId", artistId, "positionMs", 0)));
```

Do not put passwords, raw tokens, or email addresses in logs.

## Code style

- Java 21, Spring Boot 3.5.5, parent `com.harmonia:harmonia-platform:1.0.0`.
- No Lombok. Records for DTOs. Constructor injection.
- Package by layer under `com.harmonia.<context>`.
- Follow `services/auth-service` as the reference implementation.

## Frontend

Angular 22 in `frontend/`. Dev server proxies nothing by default; point the API client at `http://localhost:8080`. The production nginx image proxies `/api/` to `api-gateway`.
