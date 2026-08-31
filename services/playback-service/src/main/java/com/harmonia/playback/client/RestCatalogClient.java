package com.harmonia.playback.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.playback.dto.StreamUrlResponse;
import com.harmonia.playback.dto.TrackSnapshot;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class RestCatalogClient implements CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(RestCatalogClient.class);

    private final RestClient catalogRestClient;

    public RestCatalogClient(@Qualifier("catalogRestClient") RestClient catalogRestClient) {
        this.catalogRestClient = catalogRestClient;
    }

    @Override
    @Retry(name = "catalog")
    @CircuitBreaker(name = "catalog", fallbackMethod = "getTrackFallback")
    public TrackSnapshot getTrack(UUID trackId) {
        try {
            TrackSnapshot track = catalogRestClient.get()
                    .uri("/api/v1/tracks/{id}", trackId)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (req, res) -> {
                        throw HarmoniaException.notFound(ErrorCode.TRACK_NOT_FOUND, "Track not found: " + trackId);
                    })
                    .body(TrackSnapshot.class);
            if (track == null || track.id() == null) {
                throw HarmoniaException.notFound(ErrorCode.TRACK_NOT_FOUND, "Track not found: " + trackId);
            }
            return track;
        } catch (HarmoniaException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Catalog getTrack failed for {}", trackId, e);
            throw new IllegalStateException("Catalog request failed", e);
        }
    }

    @Override
    @Retry(name = "catalog")
    @CircuitBreaker(name = "catalog", fallbackMethod = "getStreamUrlFallback")
    public StreamUrlResponse getStreamUrl(UUID trackId) {
        try {
            StreamUrlResponse response = catalogRestClient.get()
                    .uri("/api/v1/tracks/{id}/stream", trackId)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (req, res) -> {
                        throw HarmoniaException.notFound(ErrorCode.TRACK_NOT_FOUND, "Track not found: " + trackId);
                    })
                    .body(StreamUrlResponse.class);
            if (response == null || response.url() == null || response.url().isBlank()) {
                throw HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE,
                        "Catalog did not return a stream URL");
            }
            return new StreamUrlResponse(response.url(), response.expiresIn() == null ? 3600L : response.expiresIn());
        } catch (HarmoniaException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Catalog getStreamUrl failed for {}", trackId, e);
            throw new IllegalStateException("Catalog request failed", e);
        }
    }

    @Override
    @Retry(name = "catalog")
    @CircuitBreaker(name = "catalog", fallbackMethod = "albumTrackIdsFallback")
    public List<UUID> albumTrackIds(UUID albumId) {
        try {
            AlbumTracksPayload payload = catalogRestClient.get()
                    .uri("/api/v1/albums/{id}", albumId)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (req, res) -> {
                        throw HarmoniaException.notFound(ErrorCode.ALBUM_NOT_FOUND, "Album not found: " + albumId);
                    })
                    .body(AlbumTracksPayload.class);
            if (payload == null) {
                throw HarmoniaException.notFound(ErrorCode.ALBUM_NOT_FOUND, "Album not found: " + albumId);
            }
            List<UUID> ids = payload.resolvedTrackIds();
            if (ids.isEmpty()) {
                throw HarmoniaException.badRequest(ErrorCode.QUEUE_EMPTY, "Album has no playable tracks");
            }
            return ids;
        } catch (HarmoniaException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Catalog albumTrackIds failed for {}", albumId, e);
            throw new IllegalStateException("Catalog request failed", e);
        }
    }

    @SuppressWarnings("unused")
    private TrackSnapshot getTrackFallback(UUID trackId, Throwable t) {
        throw unwrap(t);
    }

    @SuppressWarnings("unused")
    private StreamUrlResponse getStreamUrlFallback(UUID trackId, Throwable t) {
        throw unwrap(t);
    }

    @SuppressWarnings("unused")
    private List<UUID> albumTrackIdsFallback(UUID albumId, Throwable t) {
        throw unwrap(t);
    }

    private static HarmoniaException unwrap(Throwable t) {
        if (t instanceof HarmoniaException he) {
            return he;
        }
        Throwable cause = t == null ? null : t.getCause();
        if (cause instanceof HarmoniaException he) {
            return he;
        }
        return HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE, "Catalog service unavailable", t);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AlbumTracksPayload(
            UUID id,
            List<TrackSnapshot> tracks,
            List<UUID> trackIds,
            List<AlbumItem> items
    ) {
        List<UUID> resolvedTrackIds() {
            if (trackIds != null && !trackIds.isEmpty()) {
                return trackIds.stream().filter(Objects::nonNull).toList();
            }
            if (tracks != null && !tracks.isEmpty()) {
                return tracks.stream().map(TrackSnapshot::id).filter(Objects::nonNull).toList();
            }
            if (items != null && !items.isEmpty()) {
                return items.stream().map(AlbumItem::resolved).filter(Objects::nonNull).toList();
            }
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AlbumItem(UUID trackId, UUID id) {
        UUID resolved() {
            return trackId != null ? trackId : id;
        }
    }
}
