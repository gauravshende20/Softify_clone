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
@Table(name = "followed_artists")
@IdClass(FollowedArtist.Pk.class)
public class FollowedArtist {

    @Id
    @Column(name = "user_id", columnDefinition = "char(36)")
    private UUID userId;

    @Id
    @Column(name = "artist_id", columnDefinition = "char(36)")
    private UUID artistId;

    @Column(name = "followed_at", nullable = false)
    private Instant followedAt;

    protected FollowedArtist() {
    }

    public FollowedArtist(UUID userId, UUID artistId) {
        this.userId = userId;
        this.artistId = artistId;
        this.followedAt = Instant.now();
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getArtistId() {
        return artistId;
    }

    public Instant getFollowedAt() {
        return followedAt;
    }

    public static class Pk implements Serializable {
        private UUID userId;
        private UUID artistId;

        public Pk() {
        }

        public Pk(UUID userId, UUID artistId) {
            this.userId = userId;
            this.artistId = artistId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pk pk)) {
                return false;
            }
            return Objects.equals(userId, pk.userId) && Objects.equals(artistId, pk.artistId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, artistId);
        }
    }
}
