package com.harmonia.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "entity_open_events")
public class EntityOpenEvent {

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "char(36)")
    private UUID userId;

    @Column(name = "entity_type", nullable = false, length = 32)
    private String entityType;

    @Column(name = "entity_id", nullable = false, columnDefinition = "char(36)")
    private UUID entityId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected EntityOpenEvent() {
    }

    public EntityOpenEvent(UUID userId, String entityType, UUID entityId, Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
