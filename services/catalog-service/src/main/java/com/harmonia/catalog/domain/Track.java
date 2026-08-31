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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artist_id", nullable = false, columnDefinition = "char(36)")
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", columnDefinition = "char(36)")
    private Album album;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "duration_ms", nullable = false)
    private int durationMs;

    @Column(name = "object_key", nullable = false, length = 512)
    private String objectKey;

    @Column(name = "mime_type", nullable = false, length = 128)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private boolean explicit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, columnDefinition = "enum('DRAFT','PUBLISHED','HIDDEN')")
    private TrackStatus status = TrackStatus.DRAFT;

    @Column(name = "track_number")
    private Integer trackNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToMany
    @JoinTable(
            name = "track_genres",
            joinColumns = @JoinColumn(name = "track_id", columnDefinition = "char(36)"),
            inverseJoinColumns = @JoinColumn(name = "genre_id", columnDefinition = "char(36)")
    )
    private Set<Genre> genres = new HashSet<>();

    protected Track() {
    }

    public static Track create(UUID id, Artist artist, Album album, String title, int durationMs,
                               String objectKey, String mimeType, long fileSize, boolean explicit,
                               Integer trackNumber, Set<Genre> genres) {
        Track track = new Track();
        track.id = id;
        track.artist = artist;
        track.album = album;
        track.title = title;
        track.durationMs = durationMs;
        track.objectKey = objectKey;
        track.mimeType = mimeType;
        track.fileSize = fileSize;
        track.explicit = explicit;
        track.status = TrackStatus.DRAFT;
        track.trackNumber = trackNumber;
        track.createdAt = Instant.now();
        track.genres = genres == null ? new HashSet<>() : new HashSet<>(genres);
        return track;
    }

    public static Track seed(UUID id, Artist artist, Album album, String title, int durationMs,
                             String objectKey, Integer trackNumber, Set<Genre> genres) {
        Track track = create(id, artist, album, title, durationMs, objectKey, "audio/wav", 2_469_644L, false, trackNumber, genres);
        track.status = TrackStatus.PUBLISHED;
        return track;
    }

    public void update(String title, Album album, int durationMs, boolean explicit, Integer trackNumber, Set<Genre> genres) {
        this.title = title;
        this.album = album;
        this.durationMs = durationMs;
        this.explicit = explicit;
        this.trackNumber = trackNumber;
        this.genres.clear();
        if (genres != null) {
            this.genres.addAll(genres);
        }
    }

    public void publish() {
        this.status = TrackStatus.PUBLISHED;
    }

    public void setStatus(TrackStatus status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public Artist getArtist() {
        return artist;
    }

    public Album getAlbum() {
        return album;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public TrackStatus getStatus() {
        return status;
    }

    public Integer getTrackNumber() {
        return trackNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<Genre> getGenres() {
        return genres;
    }
}
