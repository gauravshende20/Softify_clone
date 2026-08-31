CREATE TABLE genres (
    id   CHAR(36)    NOT NULL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    slug VARCHAR(64) NOT NULL,
    UNIQUE KEY uk_genres_name (name),
    UNIQUE KEY uk_genres_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE artists (
    id         CHAR(36)      NOT NULL PRIMARY KEY,
    name       VARCHAR(128)  NOT NULL,
    bio        VARCHAR(2000) NULL,
    image_key  VARCHAR(255)  NULL,
    verified   TINYINT(1)    NOT NULL DEFAULT 0,
    created_by CHAR(36)      NOT NULL,
    created_at DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    status     ENUM('ACTIVE', 'PENDING', 'HIDDEN') NOT NULL DEFAULT 'PENDING',
    KEY idx_artists_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE artist_genres (
    artist_id CHAR(36) NOT NULL,
    genre_id  CHAR(36) NOT NULL,
    PRIMARY KEY (artist_id, genre_id),
    CONSTRAINT fk_ag_artist FOREIGN KEY (artist_id) REFERENCES artists (id) ON DELETE CASCADE,
    CONSTRAINT fk_ag_genre FOREIGN KEY (genre_id) REFERENCES genres (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE albums (
    id           CHAR(36)     NOT NULL PRIMARY KEY,
    artist_id    CHAR(36)     NOT NULL,
    title        VARCHAR(200) NOT NULL,
    album_type   ENUM('ALBUM', 'SINGLE', 'EP') NOT NULL,
    release_date DATE         NULL,
    artwork_key  VARCHAR(255) NULL,
    status       ENUM('DRAFT', 'PUBLISHED', 'HIDDEN') NOT NULL DEFAULT 'DRAFT',
    KEY idx_albums_title (title),
    CONSTRAINT fk_album_artist FOREIGN KEY (artist_id) REFERENCES artists (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tracks (
    id           CHAR(36)     NOT NULL PRIMARY KEY,
    artist_id    CHAR(36)     NOT NULL,
    album_id     CHAR(36)     NULL,
    title        VARCHAR(200) NOT NULL,
    duration_ms  INT          NOT NULL,
    object_key   VARCHAR(512) NOT NULL,
    mime_type    VARCHAR(128) NOT NULL,
    file_size    BIGINT       NOT NULL,
    explicit     TINYINT(1)   NOT NULL DEFAULT 0,
    status       ENUM('DRAFT', 'PUBLISHED', 'HIDDEN') NOT NULL DEFAULT 'DRAFT',
    track_number INT          NULL,
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_tracks_title (title),
    CONSTRAINT fk_track_artist FOREIGN KEY (artist_id) REFERENCES artists (id),
    CONSTRAINT fk_track_album FOREIGN KEY (album_id) REFERENCES albums (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE track_genres (
    track_id CHAR(36) NOT NULL,
    genre_id CHAR(36) NOT NULL,
    PRIMARY KEY (track_id, genre_id),
    CONSTRAINT fk_tg_track FOREIGN KEY (track_id) REFERENCES tracks (id) ON DELETE CASCADE,
    CONSTRAINT fk_tg_genre FOREIGN KEY (genre_id) REFERENCES genres (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
