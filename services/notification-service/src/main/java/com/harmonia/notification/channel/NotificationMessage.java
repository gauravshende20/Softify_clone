package com.harmonia.notification.channel;

import java.util.Map;
import java.util.UUID;

public record NotificationMessage(
        UUID userId,
        String type,
        String title,
        String body,
        Map<String, Object> metadata
) {
}
