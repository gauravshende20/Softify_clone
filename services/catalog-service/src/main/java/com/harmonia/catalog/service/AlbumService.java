package com.harmonia.catalog.service;

import com.harmonia.catalog.domain.Album;
import com.harmonia.catalog.domain.AlbumStatus;
import com.harmonia.catalog.domain.Artist;
import com.harmonia.catalog.domain.Track;
import com.harmonia.catalog.domain.TrackStatus;
import com.harmonia.catalog.dto.AlbumResponse;
import com.harmonia.catalog.dto.AlbumSummary;
import com.harmonia.catalog.dto.CreateAlbumRequest;
import com.harmonia.catalog.dto.PatchAlbumStatusRequest;
import com.harmonia.catalog.dto.TrackSummary;
import com.harmonia.catalog.dto.UpdateAlbumRequest;
import com.harmonia.catalog.mapper.CatalogMapper;
import com.harmonia.catalog.repo.AlbumRepository;
import com.harmonia.catalog.repo.TrackRepository;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.common.security.CurrentUser;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AlbumService {

    private static final String PRODUCER = "catalog-service";

    private final AlbumRepository albums;
    private final TrackRepository tracks;
    private final ArtistService artistService;
    private final CatalogMapper mapper;
    private final DomainEventPublisher events;

    public AlbumService(AlbumRepository albums,
                        TrackRepository tracks,
                        ArtistService artistService,
                        CatalogMapper mapper,
                        DomainEventPublisher events) {
        this.albums = albums;
        this.tracks = tracks;
        this.artistService = artistService;
        this.mapper = mapper;
        this.events = events;
    }

    @Transactional
    public AlbumResponse create(CurrentUser user, CreateAlbumRequest request) {
        Artist artist = artistService.require(request.artistId());
        CatalogAccess.requireManage(user, artist);
        Album album = Album.create(artist, request.title(), request.albumType(), request.releaseDate(), request.artworkKey());
        albums.save(album);
        events.publish(Topics.CATALOG, DomainEvent.of(
                EventType.ALBUM_CREATED, "Album", album.getId().toString(),
                PRODUCER, MDC.get("traceId"), user.id().toString(),
                Map.of("title", album.getTitle(), "artistId", artist.getId().toString())));
        return toResponse(album, List.of());
    }

    @Transactional(readOnly = true)
    public PageResponse<AlbumSummary> search(String q, UUID genreId, Pageable pageable) {
        Page<Album> page = albums.search(blankToNull(q), genreId, AlbumStatus.PUBLISHED, pageable);
        return PageResponse.of(
                page.getContent().stream().map(mapper::toAlbumSummary).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public AlbumResponse get(UUID id, CurrentUser user) {
        Album album = require(id);
        boolean privileged = CatalogAccess.canManage(user, album.getArtist()) || CatalogAccess.isStaff(user);
        if (album.getStatus() != AlbumStatus.PUBLISHED && !privileged) {
            throw HarmoniaException.notFound(ErrorCode.ALBUM_NOT_FOUND, "Album not found");
        }
        List<Track> albumTracks = tracks.findByAlbumIdOrderByTrackNumberAsc(id);
        List<TrackSummary> summaries = albumTracks.stream()
                .filter(track -> privileged || track.getStatus() == TrackStatus.PUBLISHED)
                .map(mapper::toTrackSummary)
                .toList();
        return toResponse(album, summaries);
    }

    @Transactional
    public AlbumResponse update(UUID id, CurrentUser user, UpdateAlbumRequest request) {
        Album album = require(id);
        CatalogAccess.requireManage(user, album.getArtist());
        album.update(request.title(), request.albumType(), request.releaseDate(), request.artworkKey());
        return toResponse(album, List.of());
    }

    @Transactional
    public AlbumResponse publish(UUID id, CurrentUser user) {
        Album album = require(id);
        CatalogAccess.requireManage(user, album.getArtist());
        album.publish();
        events.publish(Topics.CATALOG, DomainEvent.of(
                EventType.ALBUM_PUBLISHED, "Album", album.getId().toString(),
                PRODUCER, MDC.get("traceId"), user.id().toString(),
                Map.of("title", album.getTitle(), "artistId", album.getArtist().getId().toString())));
        return toResponse(album, List.of());
    }

    @Transactional
    public AlbumResponse patchStatus(UUID id, CurrentUser user, PatchAlbumStatusRequest request) {
        CatalogAccess.requireStaff(user);
        Album album = require(id);
        album.setStatus(request.status());
        return toResponse(album, List.of());
    }

    @Transactional
    public void delete(UUID id, CurrentUser user) {
        Album album = require(id);
        CatalogAccess.requireManage(user, album.getArtist());
        albums.delete(album);
    }

    Album require(UUID id) {
        return albums.findById(id)
                .orElseThrow(() -> HarmoniaException.notFound(ErrorCode.ALBUM_NOT_FOUND, "Album not found"));
    }

    private AlbumResponse toResponse(Album album, List<TrackSummary> tracks) {
        return mapper.toAlbum(album, mapper.toArtistSummary(album.getArtist()), tracks);
    }

    private static String blankToNull(String q) {
        return q == null || q.isBlank() ? null : q.trim();
    }
}
