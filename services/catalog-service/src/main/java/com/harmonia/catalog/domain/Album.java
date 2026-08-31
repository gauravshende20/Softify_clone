package com.harmonia.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "albums")
public class Album {

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "artist_id", nullable = false, columnDefinition = "char(36)")
    private Artist artist;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "album_type", nullable = false, length = 16, columnDefinition = "enum('ALBUM','SINGLE','EP')")
    private AlbumType albumType;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "artwork_key")
    private String artworkKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, columnDefinition = "enum('DRAFT','PUBLISHED','HIDDEN')")
    private AlbumStatus status = AlbumStatus.DRAFT;

    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
    @OrderBy("trackNumber ASC")
    private List<Track> tracks = new ArrayList<>();

    protected Album() {
    }

    public static Album create(Artist artist, String title, AlbumType albumType, LocalDate releaseDate, String artworkKey) {
        Album album = new Album();
        album.id = UUID.randomUUID();
        album.artist = artist;
        album.title = title;
        album.albumType = albumType;
        album.releaseDate = releaseDate;
        album.artworkKey = artworkKey;
        album.status = AlbumStatus.DRAFT;
        return album;
    }

    public static Album seed(UUID id, Artist artist, String title, AlbumType albumType, LocalDate releaseDate,
                             String artworkKey) {
        Album album = create(artist, title, albumType, releaseDate, artworkKey);
        album.id = id;
        album.status = AlbumStatus.PUBLISHED;
        return album;
    }

    public void update(String title, AlbumType albumType, LocalDate releaseDate, String artworkKey) {
        this.title = title;
        this.albumType = albumType;
        this.releaseDate = releaseDate;
        this.artworkKey = artworkKey;
    }

    public void setStatus(AlbumStatus status) {
        this.status = status;
    }

    public void publish() {
        this.status = AlbumStatus.PUBLISHED;
    }

    public UUID getId() {
        return id;
    }

    public Artist getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public AlbumType getAlbumType() {
        return albumType;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public String getArtworkKey() {
        return artworkKey;
    }

    public AlbumStatus getStatus() {
        return status;
    }

    public List<Track> getTracks() {
        return tracks;
    }
}
