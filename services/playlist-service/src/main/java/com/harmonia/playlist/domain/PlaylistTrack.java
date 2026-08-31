package com.harmonia.playlist.domain;

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
@Table(name = "playlist_tracks")
@IdClass(PlaylistTrack.Pk.class)
public class PlaylistTrack {

    @Id
    @Column(name = "playlist_id", columnDefinition = "char(36)")
    private UUID playlistId;

    @Id
    @Column(name = "track_id", columnDefinition = "char(36)")
    private UUID trackId;

    @Column(nullable = false)
    private int position;

    @Column(name = "added_by", nullable = false, columnDefinition = "char(36)")
    private UUID addedBy;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    protected PlaylistTrack() {
    }

    public static PlaylistTrack create(UUID playlistId, UUID trackId, int position, UUID addedBy) {
        PlaylistTrack item = new PlaylistTrack();
        item.playlistId = playlistId;
        item.trackId = trackId;
        item.position = position;
        item.addedBy = addedBy;
        item.addedAt = Instant.now();
        return item;
    }

    public UUID getPlaylistId() {
        return playlistId;
    }

    public UUID getTrackId() {
        return trackId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public UUID getAddedBy() {
        return addedBy;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public static class Pk implements Serializable {
        private UUID playlistId;
        private UUID trackId;

        public Pk() {
        }

        public Pk(UUID playlistId, UUID trackId) {
            this.playlistId = playlistId;
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
            return Objects.equals(playlistId, pk.playlistId) && Objects.equals(trackId, pk.trackId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(playlistId, trackId);
        }
    }
}
