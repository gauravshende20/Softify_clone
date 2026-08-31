CREATE TABLE play_events (
    id           CHAR(36)    NOT NULL PRIMARY KEY,
    user_id      CHAR(36)    NOT NULL,
    track_id     CHAR(36)    NOT NULL,
    artist_id    CHAR(36)    NULL,
    event_type   VARCHAR(32) NOT NULL,
    position_ms  BIGINT      NULL,
    occurred_at  DATETIME(3) NOT NULL,
    KEY idx_play_track_occurred (track_id, occurred_at),
    KEY idx_play_user_occurred (user_id, occurred_at),
    KEY idx_play_artist_occurred (artist_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE search_events (
    id          CHAR(36)     NOT NULL PRIMARY KEY,
    user_id     CHAR(36)     NULL,
    query       VARCHAR(512) NOT NULL,
    occurred_at DATETIME(3)  NOT NULL,
    KEY idx_search_occurred (occurred_at),
    KEY idx_search_user_occurred (user_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE entity_open_events (
    id          CHAR(36)    NOT NULL PRIMARY KEY,
    user_id     CHAR(36)    NOT NULL,
    entity_type VARCHAR(32) NOT NULL,
    entity_id   CHAR(36)    NOT NULL,
    occurred_at DATETIME(3) NOT NULL,
    KEY idx_entity_open_occurred (occurred_at),
    KEY idx_entity_open_user (user_id, occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
