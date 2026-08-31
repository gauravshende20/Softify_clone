package com.harmonia.recommendation.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmonia.recommendation.dto.TrackSnapshot;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class RestUserClient implements UserClient {

    private static final Logger log = LoggerFactory.getLogger(RestUserClient.class);

    private final RestClient userRestClient;
    private final ObjectMapper objectMapper;

    public RestUserClient(@Qualifier("userRestClient") RestClient userRestClient, ObjectMapper objectMapper) {
        this.userRestClient = userRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    @Retry(name = "user")
    @CircuitBreaker(name = "user", fallbackMethod = "fetchTasteFallback")
    public UserTaste fetchTaste() {
        List<TrackSnapshot> likedTracks = getTracks("/api/v1/me/likes/tracks");
        List<UUID> likedIds = likedTracks.isEmpty()
                ? getIds("/api/v1/me/likes/tracks", "trackId", "id")
                : likedTracks.stream().map(TrackSnapshot::resolvedId).toList();
        List<UUID> followed = firstNonEmpty(
                getIds("/api/v1/me/follows/artists", "artistId", "id"),
                getIds("/api/v1/me/follows", "artistId", "id")
        );
        List<TrackSnapshot> recentlyPlayedTracks = getTracks("/api/v1/me/recently-played");
        List<UUID> recentlyPlayed = recentlyPlayedTracks.isEmpty()
                ? getIds("/api/v1/me/recently-played", "trackId", "id")
                : recentlyPlayedTracks.stream().map(TrackSnapshot::resolvedId).toList();
        List<UUID> genres = firstNonEmpty(
                getIds("/api/v1/me/genres", "genreId", "id"),
                getIds("/api/v1/me/likes/genres", "genreId", "id")
        );
        return new UserTaste(likedIds, followed, genres, recentlyPlayed, likedTracks, recentlyPlayedTracks);
    }

    @SuppressWarnings("unused")
    private UserTaste fetchTasteFallback(Throwable t) {
        log.warn("User taste APIs unavailable; falling back to popular/trending only", t);
        return UserTaste.empty();
    }

    private List<TrackSnapshot> getTracks(String path) {
        JsonNode node = get(path);
        return JsonCollectionSupport.extractObjects(objectMapper, node, TrackSnapshot.class);
    }

    private List<UUID> getIds(String path, String... idFields) {
        return JsonCollectionSupport.extractIds(get(path), idFields);
    }

    private JsonNode get(String path) {
        try {
            return userRestClient.get().uri(path).retrieve().body(JsonNode.class);
        } catch (Exception e) {
            log.debug("User API {} failed", path, e);
            return null;
        }
    }

    @SafeVarargs
    private static List<UUID> firstNonEmpty(List<UUID>... lists) {
        for (List<UUID> list : lists) {
            if (list != null && !list.isEmpty()) {
                return list;
            }
        }
        return List.of();
    }
}
