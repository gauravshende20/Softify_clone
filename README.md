# Harmonia

Harmonia is a production-oriented music streaming platform: accounts, catalog, playlists, playback, search, recommendations, analytics, and notifications. It is a multi-module Java 21 / Spring Boot 3.5.5 system with an Angular client.

This repository is original Harmonia software. It is not affiliated with any third-party streaming brand.

## Architecture

```
Client  →  API Gateway (8080)
              ├─ auth-service            8081
              ├─ user-service            8082
              ├─ catalog-service         8083
              ├─ playlist-service        8084
              ├─ playback-service        8085
              ├─ search-service          8086
              ├─ recommendation-service  8087
              ├─ analytics-service       8088
              └─ notification-service    8089
```

Control plane: Config Server (8888), Eureka (8761). Data plane: MySQL, Redis, Kafka, OpenSearch, MinIO.

See [docs/architecture.md](docs/architecture.md) for diagrams.

## Quick start

```bash
cp .env.example .env
docker compose up -d
mvn -B test
mvn -B -DskipTests package
# optional full stack
docker compose -f docker-compose.yml -f docker-compose.apps.yml up --build
```

Frontend:

```bash
cd frontend
npm ci
npm start
```

## Documentation

| Guide | Contents |
| --- | --- |
| [docs/architecture.md](docs/architecture.md) | Services, data stores, request path |
| [docs/database.md](docs/database.md) | Schemas and ownership |
| [docs/api.md](docs/api.md) | HTTP API |
| [docs/security.md](docs/security.md) | JWT, roles, secrets |
| [docs/events.md](docs/events.md) | Kafka catalog |
| [docs/playback.md](docs/playback.md) | Listening session |
| [docs/search.md](docs/search.md) | OpenSearch indexing |
| [docs/deployment.md](docs/deployment.md) | Docker and Kubernetes |
| [docs/development-guide.md](docs/development-guide.md) | Local workflow |

## Modules

- `libs/` shared API, web, security, Kafka, observability
- `infra/` config-server, eureka-server, api-gateway
- `services/` nine domain services
- `frontend/` Angular 22 application
- `config-repo/` native Spring Cloud Config
- `k8s/` namespace, ConfigMap, example Secret, Deployments, Services, HPAs, Ingress
- `docker/` MySQL init, Prometheus, Grafana provisioning

## Security defaults

Development secrets in `.env.example` are **not** for shared or production use. Set `JWT_SECRET`, `DB_PASSWORD`, and MinIO keys per environment. Leave `MAIL_HOST` empty to keep email on the log-only adapter.

## License

Proprietary — all rights reserved unless a license file is added to this repository.
