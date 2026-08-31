package com.harmonia.analytics.kafka;

import com.harmonia.analytics.domain.PlayEventType;
import com.harmonia.analytics.service.AnalyticsService;
import com.harmonia.analytics.support.EventPayloads;
import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class AnalyticsEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventConsumer.class);

    private final AnalyticsService analyticsService;

    public AnalyticsEventConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(topics = Topics.PLAYBACK, groupId = "analytics-service")
    public void onPlayback(DomainEvent event) {
        handle(event);
    }

    @KafkaListener(topics = Topics.SEARCH, groupId = "analytics-service")
    public void onSearch(DomainEvent event) {
        handle(event);
    }

    @KafkaListener(topics = Topics.USER, groupId = "analytics-service")
    public void onUser(DomainEvent event) {
        handle(event);
    }

    @KafkaListener(topics = Topics.PLAYLIST, groupId = "analytics-service")
    public void onPlaylist(DomainEvent event) {
        handle(event);
    }

    @KafkaListener(topics = Topics.CATALOG, groupId = "analytics-service")
    public void onCatalog(DomainEvent event) {
        handle(event);
    }

    public void handle(DomainEvent event) {
        if (event == null || event.eventType() == null) {
            return;
        }
        EventType type;
        try {
            type = EventType.valueOf(event.eventType());
        } catch (IllegalArgumentException ex) {
            log.debug("Ignoring unknown analytics event type {}", event.eventType());
            return;
        }
        Map<String, Object> payload = event.payload() == null ? Map.of() : event.payload();
        Instant occurredAt = EventPayloads.occurredAt(event.occurredAt());
        UUID actor = EventPayloads.uuid(event.userId());
        if (actor == null) {
            actor = EventPayloads.uuid(EventPayloads.value(payload, "userId", "user_id"));
        }
        switch (type) {
            case TRACK_PLAYED, PLAYBACK_STARTED -> analyticsService.recordPlay(
                    actor,
                    firstUuid(payload, event.aggregateId(), "trackId", "track_id"),
                    EventPayloads.uuid(EventPayloads.value(payload, "artistId", "artist_id")),
                    PlayEventType.PLAY_STARTED,
                    EventPayloads.longValue(EventPayloads.value(payload, "positionMs", "position_ms")),
                    occurredAt);
            case PLAYBACK_COMPLETED -> analyticsService.recordPlay(
                    actor,
                    firstUuid(payload, event.aggregateId(), "trackId", "track_id"),
                    EventPayloads.uuid(EventPayloads.value(payload, "artistId", "artist_id")),
                    PlayEventType.COMPLETED,
                    EventPayloads.longValue(EventPayloads.value(payload, "positionMs", "position_ms")),
                    occurredAt);
            case TRACK_SKIPPED -> analyticsService.recordPlay(
                    actor,
                    firstUuid(payload, event.aggregateId(), "trackId", "track_id"),
                    EventPayloads.uuid(EventPayloads.value(payload, "artistId", "artist_id")),
                    PlayEventType.SKIPPED,
                    EventPayloads.longValue(EventPayloads.value(payload, "positionMs", "position_ms")),
                    occurredAt);
            case SEARCH_PERFORMED -> analyticsService.recordSearch(
                    actor,
                    EventPayloads.text(EventPayloads.value(payload, "query", "q", "searchQuery")),
                    occurredAt);
            case PLAYLIST_OPENED -> analyticsService.recordEntityOpen(
                    actor, "PLAYLIST",
                    firstUuid(payload, event.aggregateId(), "playlistId", "entityId", "entity_id"),
                    occurredAt);
            case ARTIST_OPENED -> analyticsService.recordEntityOpen(
                    actor, "ARTIST",
                    firstUuid(payload, event.aggregateId(), "artistId", "entityId", "entity_id"),
                    occurredAt);
            case ALBUM_OPENED -> analyticsService.recordEntityOpen(
                    actor, "ALBUM",
                    firstUuid(payload, event.aggregateId(), "albumId", "entityId", "entity_id"),
                    occurredAt);
            default -> log.debug("Analytics ignoring event {}", type);
        }
    }

    private static UUID firstUuid(Map<String, Object> payload, String aggregateId, String... keys) {
        UUID fromPayload = EventPayloads.uuid(EventPayloads.value(payload, keys));
        if (fromPayload != null) {
            return fromPayload;
        }
        return EventPayloads.uuid(aggregateId);
    }
}
