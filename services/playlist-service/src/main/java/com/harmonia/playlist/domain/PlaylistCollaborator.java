package com.harmonia.playlist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "playlist_collaborators")
@IdClass(PlaylistCollaborator.Pk.class)
public class PlaylistCollaborator {

    @Id
    @Column(name = "playlist_id", columnDefinition = "char(36)")
    private UUID playlistId;

    @Id
    @Column(name = "user_id", columnDefinition = "char(36)")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CollaboratorRole role = CollaboratorRole.EDITOR;

    protected PlaylistCollaborator() {
    }

    public PlaylistCollaborator(UUID playlistId, UUID userId, CollaboratorRole role) {
        this.playlistId = playlistId;
        this.userId = userId;
        this.role = role;
    }

    public UUID getPlaylistId() {
        return playlistId;
    }

    public UUID getUserId() {
        return userId;
    }

    public CollaboratorRole getRole() {
        return role;
    }

    public static class Pk implements Serializable {
        private UUID playlistId;
        private UUID userId;

        public Pk() {
        }

        public Pk(UUID playlistId, UUID userId) {
            this.playlistId = playlistId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pk pk)) {
                return false;
            }
            return Objects.equals(playlistId, pk.playlistId) && Objects.equals(userId, pk.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(playlistId, userId);
        }
    }
}
