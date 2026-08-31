package com.harmonia.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "liked_tracks")
@IdClass(LikedTrack.Pk.class)
public class LikedTrack {

    @Id
    @Column(name = "user_id", columnDefinition = "char(36)")
    private UUID userId;

    @Id
    @Column(name = "track_id", columnDefinition = "char(36)")
    private UUID trackId;

    @Column(name = "liked_at", nullable = false)
    private Instant likedAt;

    protected LikedTrack() {
    }

    public LikedTrack(UUID userId, UUID trackId) {
        this.userId = userId;
        this.trackId = trackId;
        this.likedAt = Instant.now();
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTrackId() {
        return trackId;
    }

    public Instant getLikedAt() {
        return likedAt;
    }

    public static class Pk implements Serializable {
        private UUID userId;
        private UUID trackId;

        public Pk() {
        }

        public Pk(UUID userId, UUID trackId) {
            this.userId = userId;
            this.trackId = trackId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pk pk)) {
                return false;
            }
            return Objects.equals(userId, pk.userId) && Objects.equals(trackId, pk.trackId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, trackId);
        }
    }
}
