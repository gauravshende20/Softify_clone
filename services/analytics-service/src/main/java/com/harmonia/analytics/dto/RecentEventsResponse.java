package com.harmonia.analytics.dto;

import com.harmonia.analytics.domain.PlayEventType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RecentEventsResponse(
        List<PlayView> plays,
        List<SearchView> searches,
        List<EntityOpenView> entityOpens
) {
    public record PlayView(
            UUID id,
            UUID userId,
            UUID trackId,
            UUID artistId,
            PlayEventType eventType,
            Long positionMs,
            Instant occurredAt
    ) {
    }

    public record SearchView(UUID id, UUID userId, String query, Instant occurredAt) {
    }

    public record EntityOpenView(
            UUID id,
            UUID userId,
            String entityType,
            UUID entityId,
            Instant occurredAt
    ) {
    }
}
