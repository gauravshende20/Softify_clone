package com.harmonia.playlist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "owner_id", nullable = false, columnDefinition = "char(36)")
    private UUID ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "cover_key")
    private String coverKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, columnDefinition = "enum('PUBLIC','PRIVATE')")
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable = false)
    private boolean collaborative;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Playlist() {
    }

    public static Playlist create(UUID ownerId, String name, String description, String coverKey,
                                  Visibility visibility, boolean collaborative) {
        Playlist playlist = new Playlist();
        playlist.id = UUID.randomUUID();
        playlist.ownerId = ownerId;
        playlist.name = name;
        playlist.description = description;
        playlist.coverKey = coverKey;
        playlist.visibility = visibility == null ? Visibility.PRIVATE : visibility;
        playlist.collaborative = collaborative;
        Instant now = Instant.now();
        playlist.createdAt = now;
        playlist.updatedAt = now;
        return playlist;
    }

    public void update(String name, String description, String coverKey, Visibility visibility, Boolean collaborative) {
        this.name = name;
        this.description = description;
        this.coverKey = coverKey;
        if (visibility != null) {
            this.visibility = visibility;
        }
        if (collaborative != null) {
            this.collaborative = collaborative;
        }
        touch();
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverKey() {
        return coverKey;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public boolean isCollaborative() {
        return collaborative;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
