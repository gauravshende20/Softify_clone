package com.harmonia.recommendation.service;

import com.harmonia.recommendation.client.CatalogClient;
import com.harmonia.recommendation.client.UserClient;
import com.harmonia.recommendation.client.UserTaste;
import com.harmonia.recommendation.dto.AlbumSnapshot;
import com.harmonia.recommendation.dto.ArtistSnapshot;
import com.harmonia.recommendation.dto.HomeRecommendations;
import com.harmonia.recommendation.dto.TrackSnapshot;
import com.harmonia.recommendation.spi.Recommendation;
import com.harmonia.recommendation.spi.RecommendationContext;
import com.harmonia.recommendation.spi.RecommendationEngine;
import com.harmonia.recommendation.store.RecommendationCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RecommendationEngine engine;
    private final CatalogClient catalog;
    private final UserClient users;
    private final RecommendationCache cache;

    public RecommendationService(RecommendationEngine engine,
                                 CatalogClient catalog,
                                 UserClient users,
                                 RecommendationCache cache) {
        this.engine = engine;
        this.catalog = catalog;
        this.users = users;
        this.cache = cache;
    }

    public List<Recommendation> recommendations(UUID userId) {
        return cache.get(userId).orElseGet(() -> {
            List<Recommendation> computed = compute(userId);
            cache.put(userId, computed);
            return computed;
        });
    }

    public HomeRecommendations home(UUID userId) {
        return cache.getHome(userId).orElseGet(() -> {
            List<Recommendation> madeForYou = recommendations(userId);
            List<TrackSnapshot> trending = safeList(() -> catalog.popularTracks(20));
            List<AlbumSnapshot> newReleases = safeList(() -> catalog.recentAlbums(20));
            List<ArtistSnapshot> popularArtists = safeList(() -> catalog.popularArtists(20));
            HomeRecommendations home = new HomeRecommendations(madeForYou, trending, newReleases, popularArtists);
            cache.putHome(userId, home);
            return home;
        });
    }

    private List<Recommendation> compute(UUID userId) {
        UserTaste taste = users.fetchTaste();
        List<TrackSnapshot> popular = catalog.popularTracks(50);
        List<UUID> popularIds = popular.stream()
                .map(TrackSnapshot::resolvedId)
                .filter(Objects::nonNull)
                .toList();
        RecommendationContext context = new RecommendationContext(
                userId,
                taste.likedTrackIds(),
                taste.followedArtistIds(),
                taste.favoriteGenreIds(),
                taste.recentlyPlayedTrackIds(),
                popularIds
        );
        List<Recommendation> recs = engine.recommend(context);
        log.debug("Computed {} recommendations for {}", recs.size(), userId);
        return recs;
    }

    private static <T> List<T> safeList(java.util.function.Supplier<List<T>> supplier) {
        try {
            List<T> value = supplier.get();
            return value == null ? List.of() : value;
        } catch (RuntimeException e) {
            log.warn("Catalog section unavailable for home recommendations", e);
            return List.of();
        }
    }
}
