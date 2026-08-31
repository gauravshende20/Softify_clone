CREATE DATABASE IF NOT EXISTS harmonia_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS harmonia_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS harmonia_catalog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS harmonia_playlist CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS harmonia_analytics CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS harmonia_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON harmonia_auth.* TO 'harmonia'@'%';
GRANT ALL PRIVILEGES ON harmonia_user.* TO 'harmonia'@'%';
GRANT ALL PRIVILEGES ON harmonia_catalog.* TO 'harmonia'@'%';
GRANT ALL PRIVILEGES ON harmonia_playlist.* TO 'harmonia'@'%';
GRANT ALL PRIVILEGES ON harmonia_analytics.* TO 'harmonia'@'%';
GRANT ALL PRIVILEGES ON harmonia_notification.* TO 'harmonia'@'%';
FLUSH PRIVILEGES;
