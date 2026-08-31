package com.harmonia.playback.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
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
public class RestPlaylistClient implements PlaylistClient {

    private static final Logger log = LoggerFactory.getLogger(RestPlaylistClient.class);

    private final RestClient playlistRestClient;

    public RestPlaylistClient(@Qualifier("playlistRestClient") RestClient playlistRestClient) {
        this.playlistRestClient = playlistRestClient;
    }

    @Override
    @Retry(name = "playlist")
    @CircuitBreaker(name = "playlist", fallbackMethod = "playlistTrackIdsFallback")
    public List<UUID> playlistTrackIds(UUID playlistId) {
        try {
            PlaylistTracksPayload payload = playlistRestClient.get()
                    .uri("/api/v1/playlists/{id}", playlistId)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (req, res) -> {
                        throw HarmoniaException.notFound(ErrorCode.PLAYLIST_NOT_FOUND, "Playlist not found: " + playlistId);
                    })
                    .body(PlaylistTracksPayload.class);
            if (payload == null) {
                throw HarmoniaException.notFound(ErrorCode.PLAYLIST_NOT_FOUND, "Playlist not found: " + playlistId);
            }
            List<UUID> ids = payload.resolvedTrackIds();
            if (ids.isEmpty()) {
                throw HarmoniaException.badRequest(ErrorCode.QUEUE_EMPTY, "Playlist has no playable tracks");
            }
            return ids;
        } catch (HarmoniaException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Playlist playlistTrackIds failed for {}", playlistId, e);
            throw new IllegalStateException("Playlist request failed", e);
        }
    }

    @SuppressWarnings("unused")
    private List<UUID> playlistTrackIdsFallback(UUID playlistId, Throwable t) {
        if (t instanceof HarmoniaException he) {
            throw he;
        }
        Throwable cause = t == null ? null : t.getCause();
        if (cause instanceof HarmoniaException he) {
            throw he;
        }
        throw HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE, "Playlist service unavailable", t);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlaylistTracksPayload(
            UUID id,
            List<UUID> trackIds,
            List<PlaylistItem> items,
            List<PlaylistItem> tracks
    ) {
        List<UUID> resolvedTrackIds() {
            if (trackIds != null && !trackIds.isEmpty()) {
                return trackIds.stream().filter(Objects::nonNull).toList();
            }
            if (items != null && !items.isEmpty()) {
                return items.stream().map(PlaylistItem::resolved).filter(Objects::nonNull).toList();
            }
            if (tracks != null && !tracks.isEmpty()) {
                return tracks.stream().map(PlaylistItem::resolved).filter(Objects::nonNull).toList();
            }
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record PlaylistItem(UUID trackId, UUID id) {
        UUID resolved() {
            return trackId != null ? trackId : id;
        }
    }
}
