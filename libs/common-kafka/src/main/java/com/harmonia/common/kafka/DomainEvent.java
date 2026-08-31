package com.harmonia.common.kafka;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        UUID eventId,
        String eventType,
        int version,
        Instant occurredAt,
        String aggregateType,
        String aggregateId,
        String producer,
        String traceId,
        String userId,
        Map<String, Object> payload
) {
    public static DomainEvent of(EventType type, String aggregateType, String aggregateId,
                                 String producer, String traceId, String userId, Map<String, Object> payload) {
        return new DomainEvent(
                UUID.randomUUID(),
                type.name(),
                1,
                Instant.now(),
                aggregateType,
                aggregateId,
                producer,
                traceId,
                userId,
                payload
        );
    }
}
