package com.harmonia.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "play_events")
public class PlayEvent {

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "char(36)")
    private UUID userId;

    @Column(name = "track_id", nullable = false, columnDefinition = "char(36)")
    private UUID trackId;

    @Column(name = "artist_id", columnDefinition = "char(36)")
    private UUID artistId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private PlayEventType eventType;

    @Column(name = "position_ms")
    private Long positionMs;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected PlayEvent() {
    }

    public PlayEvent(UUID userId, UUID trackId, UUID artistId, PlayEventType eventType,
                     Long positionMs, Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.trackId = trackId;
        this.artistId = artistId;
        this.eventType = eventType;
        this.positionMs = positionMs;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTrackId() {
        return trackId;
    }

    public UUID getArtistId() {
        return artistId;
    }

    public PlayEventType getEventType() {
        return eventType;
    }

    public Long getPositionMs() {
        return positionMs;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
