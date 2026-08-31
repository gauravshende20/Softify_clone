CREATE TABLE notifications (
    id          CHAR(36)     NOT NULL PRIMARY KEY,
    user_id     CHAR(36)     NOT NULL,
    type        VARCHAR(64)  NOT NULL,
    title       VARCHAR(255) NOT NULL,
    body        TEXT         NOT NULL,
    read_flag   TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    metadata    JSON         NULL,
    KEY idx_notifications_user_read (user_id, read_flag, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notification_preferences (
    user_id         CHAR(36)   NOT NULL PRIMARY KEY,
    email_enabled   TINYINT(1) NOT NULL DEFAULT 1,
    in_app_enabled  TINYINT(1) NOT NULL DEFAULT 1,
    push_enabled    TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
