package com.harmonia.notification.dto;

import com.harmonia.notification.domain.Notification;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String body,
        boolean read,
        Instant createdAt,
        Map<String, Object> metadata
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.isReadFlag(),
                notification.getCreatedAt(),
                notification.getMetadata());
    }
}
