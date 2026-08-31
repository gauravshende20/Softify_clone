# Harmonia Deployment

## Local infrastructure

```bash
cp .env.example .env
docker compose up -d
```

Brings up MySQL 8.4, Redis 7, Kafka 3.9.1 (KRaft), OpenSearch 2.18, MinIO (API 9000, console 9001), MinIO bucket init, and Prometheus.

Grafana is optional:

```bash
docker compose --profile grafana up -d
```

Host Kafka listener: `localhost:19092`. In-compose Kafka listener: `kafka:9092`.

## Local applications

```bash
mvn -B -DskipTests package
docker compose -f docker-compose.yml -f docker-compose.apps.yml up --build
```

Or run Spring Boot modules from the IDE against compose infrastructure. Config server reads `config-repo/`.

Frontend: `cd frontend && npm start` (dev) or the nginx image on port 4200 in compose.

## Container images

Each runnable module has:

```
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
USER 1000
EXPOSE <port>
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Build the jar before `docker build`.

## Kubernetes

Manifests live under `k8s/`.

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/config/configmap.yaml
kubectl apply -f k8s/secrets/secret.yaml   # from secret.yaml.example
kubectl apply -f k8s/infra -f k8s/gateway -f k8s/auth-service \
  -f k8s/user-service -f k8s/catalog-service -f k8s/playlist-service \
  -f k8s/playback-service -f k8s/search-service -f k8s/recommendation-service \
  -f k8s/analytics-service -f k8s/notification-service -f k8s/frontend
kubectl apply -f k8s/ingress.yaml
```

Assumptions:

- Managed MySQL, Redis, Kafka, OpenSearch, and object storage exist.
- `k8s/config/configmap.yaml` URLs point at those services.
- Images `harmonia/<module>:1.0.0` are pushed to your registry.

HPA is defined for api-gateway, playback-service, search-service, and catalog-service. Probes use `/actuator/health/liveness` and `/actuator/health/readiness`.

Ingress hosts: `api.harmonia.local` (gateway), `app.harmonia.local` (frontend).

Do not run single-replica MySQL/Kafka in production. See `k8s/infra/managed-dependencies.yaml`.

## Environment

See `.env.example`. Required in every environment: `JWT_SECRET`, `DB_PASSWORD`, MinIO keys. Leave `MAIL_HOST` empty for log-only email.
