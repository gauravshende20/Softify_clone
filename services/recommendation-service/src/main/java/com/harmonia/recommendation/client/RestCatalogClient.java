package com.harmonia.recommendation.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.recommendation.dto.AlbumSnapshot;
import com.harmonia.recommendation.dto.ArtistSnapshot;
import com.harmonia.recommendation.dto.TrackSnapshot;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Component
public class RestCatalogClient implements CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(RestCatalogClient.class);

    private final RestClient catalogRestClient;
    private final ObjectMapper objectMapper;

    public RestCatalogClient(@Qualifier("catalogRestClient") RestClient catalogRestClient, ObjectMapper objectMapper) {
        this.catalogRestClient = catalogRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Retry(name = "catalog")
    @CircuitBreaker(name = "catalog", fallbackMethod = "popularTracksFallback")
    public List<TrackSnapshot> popularTracks(int size) {
        return getTracks(uri -> uri.path("/api/v1/tracks")
                .queryParam("sort", "popular")
                .queryParam("size", size)
                .build());
    }

    @Override
    @Retry(name = "catalog")
    @CircuitBreaker(name = "catalog", fallbackMethod = "tracksByArtistFallback")
    public List<TrackSnapshot> tracksByArtist(UUID artistId, int size) {
        return getTracks(uri -> uri.path("/api/v1/tracks")
                .queryParam("artistId", artistId)
                .queryParam("size", size)
                .build());
    }

    @Override
    @Retry(name = "catalog")
    @CircuitBreaker(name = "catalog", fallbackMethod = "tracksByGenreFallback")
    public List<TrackSnapshot> tracksByGenre(UUID genreId, int size) {
        return getTracks(uri -> uri.path("/api/v1/tracks")
                .queryParam("genreId", genreId)
                .queryParam("size", size)
                .build());
    }

    @Override
    @Retry(name = "catalog")
    @CircuitBreaker(name = "catalog", fallbackMethod = "getTrackFallback")
    public Optional<TrackSnapshot> getTrack(UUID trackId) {
        try {
            TrackSnapshot track = catalogRestClient.get()
                    .uri("/api/v1/tracks/{id}", trackId)
                    .retrieve()
                    .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(), (req, res) -> {
                        throw HarmoniaException.notFound(ErrorCode.TRACK_NOT_FOUND, "Track not found: " + trackId);
                    })
                    .body(TrackSnapshot.class);
            return Optional.ofNullable(track);
        } catch (HarmoniaException e) {
            if (e.getCode() == ErrorCode.TRACK_NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        } catch (Exception e) {
            log.warn("Catalog getTrack failed for {}", trackId, e);
            throw new IllegalStateException("Catalog request failed", e);
        }
    }

    @Override
    @Retry(name = "catalog")
    @CircuitBreaker(name = "catalog", fallbackMethod = "recentAlbumsFallback")
    public List<AlbumSnapshot> recentAlbums(int size) {
        return getCollection("/api/v1/albums?sort=recent&size=" + size, AlbumSnapshot.class);
    }

    @Override
    @Retry(name = "catalog")
    @CircuitBreaker(name = "catalog", fallbackMethod = "popularArtistsFallback")
    public List<ArtistSnapshot> popularArtists(int size) {
        return getCollection("/api/v1/artists?sort=popular&size=" + size, ArtistSnapshot.class);
    }

    private List<TrackSnapshot> getTracks(Function<UriBuilder, java.net.URI> uriFunction) {
        try {
            JsonNode node = catalogRestClient.get()
                    .uri(uriFunction)
                    .retrieve()
                    .body(JsonNode.class);
            return JsonCollectionSupport.extractObjects(objectMapper, node, TrackSnapshot.class);
        } catch (HarmoniaException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Catalog track collection request failed", e);
            throw new IllegalStateException("Catalog request failed", e);
        }
    }

    private <T> List<T> getCollection(String path, Class<T> type) {
        try {
            JsonNode node = catalogRestClient.get()
                    .uri(path)
                    .retrieve()
                    .body(JsonNode.class);
            return JsonCollectionSupport.extractObjects(objectMapper, node, type);
        } catch (HarmoniaException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Catalog collection request failed for {}", path, e);
            throw new IllegalStateException("Catalog request failed", e);
        }
    }

    @SuppressWarnings("unused")
    private List<TrackSnapshot> popularTracksFallback(int size, Throwable t) {
        log.warn("Falling back to empty popular tracks", t);
        return List.of();
    }

    @SuppressWarnings("unused")
    private List<TrackSnapshot> tracksByArtistFallback(UUID artistId, int size, Throwable t) {
        log.warn("Falling back to empty artist tracks for {}", artistId, t);
        return List.of();
    }

    @SuppressWarnings("unused")
    private List<TrackSnapshot> tracksByGenreFallback(UUID genreId, int size, Throwable t) {
        log.warn("Falling back to empty genre tracks for {}", genreId, t);
        return List.of();
    }

    @SuppressWarnings("unused")
    private Optional<TrackSnapshot> getTrackFallback(UUID trackId, Throwable t) {
        if (t instanceof HarmoniaException he) {
            throw he;
        }
        log.warn("Falling back to empty track {}", trackId, t);
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    private List<AlbumSnapshot> recentAlbumsFallback(int size, Throwable t) {
        log.warn("Falling back to empty recent albums", t);
        return List.of();
    }

    @SuppressWarnings("unused")
    private List<ArtistSnapshot> popularArtistsFallback(int size, Throwable t) {
        log.warn("Falling back to empty popular artists", t);
        return List.of();
    }
}
