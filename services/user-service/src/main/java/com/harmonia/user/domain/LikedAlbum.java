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
@Table(name = "liked_albums")
@IdClass(LikedAlbum.Pk.class)
public class LikedAlbum {

    @Id
    @Column(name = "user_id", columnDefinition = "char(36)")
    private UUID userId;

    @Id
    @Column(name = "album_id", columnDefinition = "char(36)")
    private UUID albumId;

    @Column(name = "liked_at", nullable = false)
    private Instant likedAt;

    protected LikedAlbum() {
    }

    public LikedAlbum(UUID userId, UUID albumId) {
        this.userId = userId;
        this.albumId = albumId;
        this.likedAt = Instant.now();
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getAlbumId() {
        return albumId;
    }

    public Instant getLikedAt() {
        return likedAt;
    }

    public static class Pk implements Serializable {
        private UUID userId;
        private UUID albumId;

        public Pk() {
        }

        public Pk(UUID userId, UUID albumId) {
            this.userId = userId;
            this.albumId = albumId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pk pk)) {
                return false;
            }
            return Objects.equals(userId, pk.userId) && Objects.equals(albumId, pk.albumId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, albumId);
        }
    }
}
