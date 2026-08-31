package com.harmonia.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recently_played")
public class RecentlyPlayed {

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "char(36)")
    private UUID userId;

    @Column(name = "track_id", nullable = false, columnDefinition = "char(36)")
    private UUID trackId;

    @Column(name = "played_at", nullable = false)
    private Instant playedAt;

    protected RecentlyPlayed() {
    }

    public static RecentlyPlayed create(UUID userId, UUID trackId) {
        RecentlyPlayed played = new RecentlyPlayed();
        played.id = UUID.randomUUID();
        played.userId = userId;
        played.trackId = trackId;
        played.playedAt = Instant.now();
        return played;
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

    public Instant getPlayedAt() {
        return playedAt;
    }
}
