# Harmonia Architecture

Harmonia is a music streaming platform built as independently deployable Spring Boot services behind an API gateway. Clients never talk to domain services directly.

## System context

```mermaid
flowchart LR
  subgraph Clients
    Web[Web app]
    Mobile[Mobile clients]
  end
  subgraph Edge
    GW[API Gateway :8080]
  end
  subgraph Platform
    Auth[Auth :8081]
    User[User :8082]
    Catalog[Catalog :8083]
    Playlist[Playlist :8084]
    Playback[Playback :8085]
    Search[Search :8086]
    Recs[Recommendations :8087]
    Analytics[Analytics :8088]
    Notify[Notifications :8089]
  end
  subgraph Data
    MySQL[(MySQL)]
    Redis[(Redis)]
    Kafka[[Kafka]]
    OS[(OpenSearch)]
    MinIO[(Object storage)]
  end
  Web --> GW
  Mobile --> GW
  GW --> Auth
  GW --> User
  GW --> Catalog
  GW --> Playlist
  GW --> Playback
  GW --> Search
  GW --> Recs
  GW --> Analytics
  GW --> Notify
  Auth --> MySQL
  Auth --> Kafka
  User --> MySQL
  Catalog --> MySQL
  Catalog --> MinIO
  Catalog --> Kafka
  Playlist --> MySQL
  Playback --> Redis
  Playback --> Kafka
  Search --> OS
  Recs --> Redis
  Analytics --> MySQL
  Analytics --> Kafka
  Notify --> MySQL
  Notify --> Kafka
```

## Control plane

| Component | Port | Role |
| --- | --- | --- |
| Config Server | 8888 | Native Git-less config from `config-repo/` |
| Eureka | 8761 | Service discovery used by the gateway (`lb://`) |
| API Gateway | 8080 | Routing, JWT resource server, Redis rate limit |
| Prometheus / Grafana | 9090 / 3000 | Metrics scrape of `/actuator/prometheus` |

## Domain services

| Service | Port | Persistence | Notes |
| --- | --- | --- | --- |
| auth-service | 8081 | `harmonia_auth` | Registration, login, JWT, refresh rotation |
| user-service | 8082 | `harmonia_user` | Profiles, library, follows |
| catalog-service | 8083 | `harmonia_catalog` + MinIO | Artists, albums, tracks, media |
| playlist-service | 8084 | `harmonia_playlist` | Playlists and collaborators |
| playback-service | 8085 | Redis | Queue, session, play events |
| search-service | 8086 | OpenSearch | Query + index consumers |
| recommendation-service | 8087 | Redis | Personalized shelves |
| analytics-service | 8088 | `harmonia_analytics` | Admin reporting from Kafka |
| notification-service | 8089 | `harmonia_notification` | In-app + email channels |

## Request path

```mermaid
sequenceDiagram
  participant C as Client
  participant G as API Gateway
  participant E as Eureka
  participant S as Domain service
  C->>G: HTTPS + Bearer JWT
  G->>G: Rate limit (Redis)
  G->>E: Resolve lb://service
  G->>S: Forward X-Correlation-Id
  S->>S: Resource-server JWT
  S-->>G: JSON + correlation
  G-->>C: Response
```

## Design principles

- **API-first**: versioned `/api/v1` routes, OpenAPI on each MVC service.
- **Async facts**: Kafka `DomainEvent` envelopes, never PII in logs.
- **Defense in depth**: gateway auth plus method security on services.
- **Fail independently**: circuit breakers and timeouts at the gateway.
- **Observable by default**: health probes, Prometheus, correlation IDs.
