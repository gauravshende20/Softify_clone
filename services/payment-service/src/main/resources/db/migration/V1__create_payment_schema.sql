CREATE TABLE subscriptions (
    id                     CHAR(36) NOT NULL PRIMARY KEY,
    user_id                CHAR(36) NOT NULL,
    stripe_customer_id     VARCHAR(255) NULL,
    stripe_subscription_id VARCHAR(255) NULL,
    stripe_price_id        VARCHAR(255) NOT NULL,
    status                 VARCHAR(32) NOT NULL,
    current_period_end     DATETIME(3) NULL,
    cancel_at_period_end   TINYINT(1) NOT NULL DEFAULT 0,
    created_at             DATETIME(3) NOT NULL,
    updated_at             DATETIME(3) NOT NULL,
    UNIQUE KEY uk_subscription_user (user_id),
    UNIQUE KEY uk_subscription_stripe (stripe_subscription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE processed_stripe_events (
    stripe_event_id VARCHAR(255) NOT NULL PRIMARY KEY,
    processed_at    DATETIME(3) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
