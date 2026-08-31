CREATE TABLE profiles (
    id           CHAR(36)     NOT NULL PRIMARY KEY,
    display_name VARCHAR(64)  NOT NULL,
    avatar_key   VARCHAR(255) NULL,
    bio          VARCHAR(500) NULL,
    country      CHAR(2)      NULL,
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE user_preferences (
    user_id          CHAR(36)    NOT NULL PRIMARY KEY,
    locale           VARCHAR(16) NOT NULL DEFAULT 'en',
    explicit_content TINYINT(1)  NOT NULL DEFAULT 1,
    theme            VARCHAR(16) NOT NULL DEFAULT 'dark',
    CONSTRAINT fk_prefs_profile FOREIGN KEY (user_id) REFERENCES profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE favorite_genres (
    user_id  CHAR(36) NOT NULL,
    genre_id CHAR(36) NOT NULL,
    PRIMARY KEY (user_id, genre_id),
    CONSTRAINT fk_fav_genre_user FOREIGN KEY (user_id) REFERENCES profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE followed_artists (
    user_id     CHAR(36)    NOT NULL,
    artist_id   CHAR(36)    NOT NULL,
    followed_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, artist_id),
    CONSTRAINT fk_follow_user FOREIGN KEY (user_id) REFERENCES profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE liked_tracks (
    user_id  CHAR(36)    NOT NULL,
    track_id CHAR(36)    NOT NULL,
    liked_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, track_id),
    CONSTRAINT fk_liked_track_user FOREIGN KEY (user_id) REFERENCES profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE liked_albums (
    user_id  CHAR(36)    NOT NULL,
    album_id CHAR(36)    NOT NULL,
    liked_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (user_id, album_id),
    CONSTRAINT fk_liked_album_user FOREIGN KEY (user_id) REFERENCES profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE recently_played (
    id        CHAR(36)    NOT NULL PRIMARY KEY,
    user_id   CHAR(36)    NOT NULL,
    track_id  CHAR(36)    NOT NULL,
    played_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_recently_played_user_time (user_id, played_at),
    CONSTRAINT fk_recent_user FOREIGN KEY (user_id) REFERENCES profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
