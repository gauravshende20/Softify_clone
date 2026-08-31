package com.harmonia.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "artists")
public class Artist {

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 2000)
    private String bio;

    @Column(name = "image_key")
    private String imageKey;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "created_by", nullable = false, columnDefinition = "char(36)")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, columnDefinition = "enum('ACTIVE','PENDING','HIDDEN')")
    private ArtistStatus status = ArtistStatus.PENDING;

    @ManyToMany
    @JoinTable(
            name = "artist_genres",
            joinColumns = @JoinColumn(name = "artist_id", columnDefinition = "char(36)"),
            inverseJoinColumns = @JoinColumn(name = "genre_id", columnDefinition = "char(36)")
    )
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "artist", fetch = FetchType.LAZY)
    @OrderBy("releaseDate DESC")
    private List<Album> albums = new ArrayList<>();

    protected Artist() {
    }

    public static Artist create(String name, String bio, String imageKey, UUID createdBy, Set<Genre> genres) {
        Artist artist = new Artist();
        artist.id = UUID.randomUUID();
        artist.name = name;
        artist.bio = bio;
        artist.imageKey = imageKey;
        artist.verified = false;
        artist.createdBy = createdBy;
        Instant now = Instant.now();
        artist.createdAt = now;
        artist.updatedAt = now;
        artist.status = ArtistStatus.ACTIVE;
        artist.genres = genres == null ? new HashSet<>() : new HashSet<>(genres);
        return artist;
    }

    public static Artist seed(UUID id, String name, String bio, String imageKey, UUID createdBy,
                              Set<Genre> genres, boolean verified) {
        Artist artist = create(name, bio, imageKey, createdBy, genres);
        artist.id = id;
        artist.verified = verified;
        artist.status = ArtistStatus.ACTIVE;
        return artist;
    }

    public void update(String name, String bio, String imageKey, Set<Genre> genres) {
        this.name = name;
        this.bio = bio;
        this.imageKey = imageKey;
        this.genres.clear();
        if (genres != null) {
            this.genres.addAll(genres);
        }
        this.updatedAt = Instant.now();
    }

    public void setStatus(ArtistStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }

    public String getImageKey() {
        return imageKey;
    }

    public boolean isVerified() {
        return verified;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public ArtistStatus getStatus() {
        return status;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public List<Album> getAlbums() {
        return albums;
    }
}
