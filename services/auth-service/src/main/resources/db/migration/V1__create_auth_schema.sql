CREATE TABLE accounts (
    id              CHAR(36)     NOT NULL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    enabled         TINYINT(1)   NOT NULL DEFAULT 1,
    email_verified  TINYINT(1)   NOT NULL DEFAULT 0,
    failed_attempts INT          NOT NULL DEFAULT 0,
    locked_until    DATETIME(3)  NULL,
    created_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_accounts_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE account_roles (
    account_id CHAR(36)    NOT NULL,
    role       VARCHAR(32) NOT NULL,
    PRIMARY KEY (account_id, role),
    CONSTRAINT fk_account_roles_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id          CHAR(36)    NOT NULL PRIMARY KEY,
    account_id  CHAR(36)    NOT NULL,
    token_hash  CHAR(64)    NOT NULL,
    expires_at  DATETIME(3) NOT NULL,
    revoked     TINYINT(1)  NOT NULL DEFAULT 0,
    created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_refresh_token_hash (token_hash),
    KEY idx_refresh_account (account_id),
    CONSTRAINT fk_refresh_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE email_verification_tokens (
    id          CHAR(36)    NOT NULL PRIMARY KEY,
    account_id  CHAR(36)    NOT NULL,
    token_hash  CHAR(64)    NOT NULL,
    expires_at  DATETIME(3) NOT NULL,
    used        TINYINT(1)  NOT NULL DEFAULT 0,
    UNIQUE KEY uk_verify_hash (token_hash),
    CONSTRAINT fk_verify_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_tokens (
    id          CHAR(36)    NOT NULL PRIMARY KEY,
    account_id  CHAR(36)    NOT NULL,
    token_hash  CHAR(64)    NOT NULL,
    expires_at  DATETIME(3) NOT NULL,
    used        TINYINT(1)  NOT NULL DEFAULT 0,
    UNIQUE KEY uk_reset_hash (token_hash),
    CONSTRAINT fk_reset_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
