package com.harmonia.catalog.service;

import com.harmonia.catalog.domain.Album;
import com.harmonia.catalog.domain.Artist;
import com.harmonia.catalog.domain.Genre;
import com.harmonia.catalog.domain.Track;
import com.harmonia.catalog.domain.TrackStatus;
import com.harmonia.catalog.dto.PatchTrackStatusRequest;
import com.harmonia.catalog.dto.StreamUrlResponse;
import com.harmonia.catalog.dto.TrackResponse;
import com.harmonia.catalog.dto.TrackSummary;
import com.harmonia.catalog.dto.TrackUploadRequest;
import com.harmonia.catalog.dto.UpdateTrackRequest;
import com.harmonia.catalog.mapper.CatalogMapper;
import com.harmonia.catalog.repo.TrackRepository;
import com.harmonia.catalog.storage.StoragePort;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class TrackService {

    public static final long STREAM_EXPIRY_SECONDS = 900;
    private static final String PRODUCER = "catalog-service";

    private final TrackRepository tracks;
    private final ArtistService artistService;
    private final AlbumService albumService;
    private final GenreService genreService;
    private final StoragePort storage;
    private final CatalogMapper mapper;
    private final DomainEventPublisher events;

    public TrackService(TrackRepository tracks,
                        ArtistService artistService,
                        AlbumService albumService,
                        GenreService genreService,
                        StoragePort storage,
                        CatalogMapper mapper,
                        DomainEventPublisher events) {
        this.tracks = tracks;
        this.artistService = artistService;
        this.albumService = albumService;
        this.genreService = genreService;
        this.storage = storage;
        this.mapper = mapper;
        this.events = events;
    }

    @Transactional
    public TrackResponse upload(CurrentUser user, TrackUploadRequest metadata, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Audio file is required");
        }
        Artist artist = artistService.require(metadata.artistId());
        CatalogAccess.requireManage(user, artist);
        Album album = resolveAlbum(metadata.albumId(), artist);
        Set<Genre> genres = genreService.requireAll(metadata.genreIds());
        UUID trackId = UUID.randomUUID();
        String objectKey = "audio/" + artist.getId() + "/" + trackId + "/" + sanitize(file.getOriginalFilename());
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        try (InputStream in = file.getInputStream()) {
            storage.putAudio(objectKey, in, file.getSize(), contentType);
        } catch (HarmoniaException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new HarmoniaException(ErrorCode.STORAGE_FAILURE, 503, "Failed to store audio", ex);
        }
        Track track = Track.create(trackId, artist, album, metadata.title(), metadata.durationMs(),
                objectKey, contentType, file.getSize(), metadata.explicit(), metadata.trackNumber(), genres);
        tracks.save(track);
        publish(EventType.TRACK_UPLOADED, track, user.id(), Map.of(
                "title", track.getTitle(),
                "artistId", artist.getId().toString(),
                "status", track.getStatus().name()));
        return mapper.toTrack(track);
    }

    @Transactional
    public TrackResponse publish(UUID id, CurrentUser user) {
        Track track = require(id);
        CatalogAccess.requireManage(user, track.getArtist());
        if (track.getStatus() == TrackStatus.HIDDEN) {
            throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Hidden tracks cannot be published");
        }
        track.publish();
        publish(EventType.TRACK_PUBLISHED, track, user.id(), Map.of(
                "title", track.getTitle(),
                "artistId", track.getArtist().getId().toString()));
        return mapper.toTrack(track);
    }

    @Transactional(readOnly = true)
    public PageResponse<TrackSummary> search(String q, UUID genreId, Pageable pageable) {
        Page<Track> page = tracks.search(blankToNull(q), genreId, TrackStatus.PUBLISHED, pageable);
        return PageResponse.of(
                page.getContent().stream().map(mapper::toTrackSummary).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<TrackSummary> byIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return tracks.findByIdIn(ids).stream()
                .filter(track -> track.getStatus() == TrackStatus.PUBLISHED)
                .map(mapper::toTrackSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrackResponse get(UUID id, CurrentUser user) {
        return mapper.toTrack(requireVisible(id, user));
    }

    @Transactional(readOnly = true)
    public StreamUrlResponse stream(UUID id, CurrentUser user) {
        Track track = requireVisible(id, user);
        String url = storage.presignedAudioUrl(track.getObjectKey(), Duration.ofSeconds(STREAM_EXPIRY_SECONDS));
        return new StreamUrlResponse(url, STREAM_EXPIRY_SECONDS);
    }

    @Transactional
    public TrackResponse update(UUID id, CurrentUser user, UpdateTrackRequest request) {
        Track track = require(id);
        CatalogAccess.requireManage(user, track.getArtist());
        Album album = resolveAlbum(request.albumId(), track.getArtist());
        track.update(request.title(), album, request.durationMs(), request.explicit(), request.trackNumber(),
                genreService.requireAll(request.genreIds()));
        return mapper.toTrack(track);
    }

    @Transactional
    public TrackResponse patchStatus(UUID id, CurrentUser user, PatchTrackStatusRequest request) {
        CatalogAccess.requireStaff(user);
        Track track = require(id);
        TrackStatus previous = track.getStatus();
        track.setStatus(request.status());
        if (previous == TrackStatus.PUBLISHED && request.status() != TrackStatus.PUBLISHED) {
            publish(EventType.TRACK_UNPUBLISHED, track, user.id(), Map.of("status", request.status().name()));
        }
        return mapper.toTrack(track);
    }

    @Transactional
    public void delete(UUID id, CurrentUser user) {
        Track track = require(id);
        CatalogAccess.requireManage(user, track.getArtist());
        tracks.delete(track);
    }

    private Track requireVisible(UUID id, CurrentUser user) {
        Track track = require(id);
        boolean privileged = CatalogAccess.canManage(user, track.getArtist()) || CatalogAccess.isStaff(user);
        if (track.getStatus() != TrackStatus.PUBLISHED && !privileged) {
            throw HarmoniaException.notFound(ErrorCode.TRACK_NOT_FOUND, "Track not found");
        }
        return track;
    }

    private Track require(UUID id) {
        return tracks.findById(id)
                .orElseThrow(() -> HarmoniaException.notFound(ErrorCode.TRACK_NOT_FOUND, "Track not found"));
    }

    private Album resolveAlbum(UUID albumId, Artist artist) {
        if (albumId == null) {
            return null;
        }
        Album album = albumService.require(albumId);
        if (!album.getArtist().getId().equals(artist.getId())) {
            throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Album does not belong to the artist");
        }
        return album;
    }

    private void publish(EventType type, Track track, UUID userId, Map<String, Object> payload) {
        events.publish(Topics.CATALOG, DomainEvent.of(
                type, "Track", track.getId().toString(),
                PRODUCER, MDC.get("traceId"), userId.toString(), payload));
    }

    static String sanitize(String filename) {
        if (filename == null || filename.isBlank()) {
            return "audio.bin";
        }
        String base = filename.replace("\\", "/");
        int slash = base.lastIndexOf('/');
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        String cleaned = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.isBlank() || cleaned.replace("_", "").replace(".", "").replace("-", "").isBlank()) {
            return "audio.bin";
        }
        return cleaned;
    }

    private static String blankToNull(String q) {
        return q == null || q.isBlank() ? null : q.trim();
    }
}
