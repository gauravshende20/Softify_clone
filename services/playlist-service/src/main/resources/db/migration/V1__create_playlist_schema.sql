CREATE TABLE playlists (
    id            CHAR(36)      NOT NULL PRIMARY KEY,
    owner_id      CHAR(36)      NOT NULL,
    name          VARCHAR(100)  NOT NULL,
    description   VARCHAR(500)  NULL,
    cover_key     VARCHAR(255)  NULL,
    visibility    ENUM('PUBLIC', 'PRIVATE') NOT NULL DEFAULT 'PRIVATE',
    collaborative TINYINT(1)    NOT NULL DEFAULT 0,
    created_at    DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)   NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    KEY idx_playlists_owner (owner_id),
    KEY idx_playlists_visibility (visibility)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE playlist_tracks (
    playlist_id CHAR(36)    NOT NULL,
    track_id    CHAR(36)    NOT NULL,
    position    INT         NOT NULL,
    added_by    CHAR(36)    NOT NULL,
    added_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (playlist_id, track_id),
    UNIQUE KEY uk_playlist_position (playlist_id, position),
    CONSTRAINT fk_pt_playlist FOREIGN KEY (playlist_id) REFERENCES playlists (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE playlist_collaborators (
    playlist_id CHAR(36)    NOT NULL,
    user_id     CHAR(36)    NOT NULL,
    role        VARCHAR(16) NOT NULL DEFAULT 'EDITOR',
    PRIMARY KEY (playlist_id, user_id),
    CONSTRAINT fk_pc_playlist FOREIGN KEY (playlist_id) REFERENCES playlists (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
