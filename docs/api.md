# Harmonia HTTP API

All public traffic enters the API gateway on port 8080. Services also expose the same paths locally for development.

Base path: `/api/v1`. Errors use the shared `ApiError` envelope (`timestamp`, `status`, `error`, `message`, `path`, `traceId`, `fieldErrors`).

## Auth (`auth-service`)

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | public | Create listener or artist account |
| POST | `/api/v1/auth/login` | public | Issue access + refresh tokens |
| POST | `/api/v1/auth/refresh` | public | Rotate refresh token |
| POST | `/api/v1/auth/logout` | user | Revoke refresh tokens |
| POST | `/api/v1/auth/verify-email` | public | Consume verification token |
| POST | `/api/v1/auth/forgot-password` | public | Always 202 (no account enumeration) |
| POST | `/api/v1/auth/reset-password` | public | Consume reset token |
| GET | `/api/v1/auth/me` | user | Current account |

Access tokens are JWT (HS256, 15 minutes). Refresh tokens are opaque and stored hashed.

## Analytics (admin)

`@PreAuthorize("hasRole('ADMIN')")`

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/v1/analytics/overview` | `totalStreams`, `uniqueListeners`, `topTracks`, `topArtists` |
| GET | `/api/v1/analytics/tracks/popular?from=&to=` | ISO-8601 instants; default last 7 days |
| GET | `/api/v1/analytics/recent` | Latest play, search, and entity-open events |

`topArtists` is empty when Kafka payloads do not include `artistId`. Streams count `PLAY_STARTED` rows.

## Notifications

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/v1/notifications` | Current user, unread first (`page`, `size`) |
| POST | `/api/v1/notifications/{id}/read` | Mark one as read |
| POST | `/api/v1/notifications/read-all` | Mark all as read |
| GET | `/api/v1/notifications/preferences` | Create defaults if missing |
| PUT | `/api/v1/notifications/preferences` | `{ emailEnabled, inAppEnabled, pushEnabled }` |

## Catalog, users, playlists, playback, search

Gateway routes (public catalog/search GET unless noted):

| Prefix | Service |
| --- | --- |
| `/api/v1/users/**`, `/api/v1/me/**`, `/api/v1/library/**` | user-service |
| `/api/v1/artists/**`, `/api/v1/albums/**`, `/api/v1/tracks/**`, `/api/v1/genres/**`, `/api/v1/storage/**` | catalog-service |
| `/api/v1/playlists/**` | playlist-service |
| `/api/v1/playback/**` | playback-service |
| `/api/v1/search/**` | search-service |
| `/api/v1/recommendations/**` | recommendation-service |

Public GET is allowed for tracks, artists, albums, genres, search, and public playlists. Mutations require a valid JWT.

## Observability endpoints

`/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`, `/actuator/prometheus` (gateway also exposes `/actuator/gateway`).
