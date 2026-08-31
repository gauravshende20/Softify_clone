package com.harmonia.recommendation.engine;

import com.harmonia.recommendation.client.CatalogClient;
import com.harmonia.recommendation.dto.TrackSnapshot;
import com.harmonia.recommendation.spi.Recommendation;
import com.harmonia.recommendation.spi.RecommendationContext;
import com.harmonia.recommendation.spi.RecommendationEngine;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Primary
public class RuleBasedRecommendationEngine implements RecommendationEngine {

    private static final int LIMIT = 20;

    private final CatalogClient catalog;

    public RuleBasedRecommendationEngine(CatalogClient catalog) {
        this.catalog = catalog;
    }

    @Override
    public List<Recommendation> recommend(RecommendationContext ctx) {
        List<TrackSnapshot> likedTracks = fetchTracks(ctx.likedTrackIds(), 15);
        List<TrackSnapshot> recentTracks = fetchTracks(ctx.recentlyPlayedTrackIds(), 15);

        Set<UUID> likedArtists = artistIds(likedTracks);
        Set<UUID> likedGenres = genreIds(likedTracks);
        Set<String> likedGenreNames = genreNames(likedTracks);
        Set<UUID> recentArtists = artistIds(recentTracks);
        Set<UUID> recentGenres = genreIds(recentTracks);
        Set<String> recentGenreNames = genreNames(recentTracks);
        Set<UUID> followedArtists = new HashSet<>(ctx.followedArtistIds());
        Set<UUID> favoriteGenres = new HashSet<>(ctx.favoriteGenreIds());

        Map<UUID, TrackSnapshot> candidates = new LinkedHashMap<>();
        addAll(candidates, catalog.popularTracks(50));
        ctx.followedArtistIds().stream().limit(8)
                .forEach(id -> addAll(candidates, catalog.tracksByArtist(id, 15)));
        ctx.favoriteGenreIds().stream().limit(8)
                .forEach(id -> addAll(candidates, catalog.tracksByGenre(id, 15)));
        likedArtists.stream().limit(8)
                .forEach(id -> addAll(candidates, catalog.tracksByArtist(id, 10)));
        likedGenres.stream().limit(5)
                .forEach(id -> addAll(candidates, catalog.tracksByGenre(id, 10)));

        Set<UUID> recent = new HashSet<>(ctx.recentlyPlayedTrackIds());
        List<TrackSnapshot> pool = new ArrayList<>(candidates.values());
        List<TrackSnapshot> withoutRecent = pool.stream()
                .filter(track -> !recent.contains(track.resolvedId()))
                .toList();
        List<TrackSnapshot> scoredPool = withoutRecent.size() >= LIMIT ? withoutRecent : pool;

        List<Scored> scored = new ArrayList<>();
        for (TrackSnapshot track : scoredPool) {
            UUID id = track.resolvedId();
            if (id == null) {
                continue;
            }
            double score = 0;
            String reason = "popular right now";
            boolean likedSimilar = contains(likedArtists, track.artistId())
                    || contains(likedGenres, track.genreId())
                    || contains(likedGenreNames, track.genre());
            if (likedSimilar) {
                score += 3;
                reason = "similar to tracks you like";
            }
            if (contains(followedArtists, track.artistId())) {
                score += 2;
                if (score == 2) {
                    reason = "from an artist you follow";
                }
            }
            if (contains(favoriteGenres, track.genreId())) {
                score += 2;
                if ("popular right now".equals(reason)) {
                    reason = "matches your favorite genre";
                }
            }
            boolean recentNeighbor = contains(recentArtists, track.artistId())
                    || contains(recentGenres, track.genreId())
                    || contains(recentGenreNames, track.genre());
            if (recentNeighbor) {
                score += 1;
                if ("popular right now".equals(reason)) {
                    reason = "similar to recently played";
                }
            }
            boolean popular = ctx.popularTrackIds().contains(id)
                    || (track.popularity() != null && track.popularity() >= 50);
            if (popular) {
                score += 1;
            }
            if (score <= 0) {
                continue;
            }
            scored.add(new Scored(id, reason, score));
        }

        if (scored.isEmpty()) {
            return ctx.popularTrackIds().stream()
                    .filter(id -> !recent.contains(id) || ctx.popularTrackIds().size() < LIMIT)
                    .limit(LIMIT)
                    .map(id -> new Recommendation(id, "popular right now", 1.0))
                    .toList();
        }

        List<Recommendation> ranked = scored.stream()
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .map(item -> new Recommendation(item.trackId(), item.reason(), item.score()))
                .toList();

        List<Recommendation> withoutRecentRecs = ranked.stream()
                .filter(rec -> !recent.contains(rec.trackId()))
                .toList();
        List<Recommendation> chosen = withoutRecentRecs.size() >= LIMIT ? withoutRecentRecs : ranked;
        return chosen.stream().limit(LIMIT).toList();
    }

    private List<TrackSnapshot> fetchTracks(List<UUID> ids, int limit) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .limit(limit)
                .map(catalog::getTrack)
                .flatMap(Optional::stream)
                .toList();
    }

    private static void addAll(Map<UUID, TrackSnapshot> candidates, List<TrackSnapshot> tracks) {
        if (tracks == null) {
            return;
        }
        for (TrackSnapshot track : tracks) {
            if (track != null && track.resolvedId() != null) {
                candidates.putIfAbsent(track.resolvedId(), track);
            }
        }
    }

    private static Set<UUID> artistIds(List<TrackSnapshot> tracks) {
        return tracks.stream().map(TrackSnapshot::artistId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<UUID> genreIds(List<TrackSnapshot> tracks) {
        return tracks.stream().map(TrackSnapshot::genreId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static Set<String> genreNames(List<TrackSnapshot> tracks) {
        return tracks.stream()
                .map(TrackSnapshot::genre)
                .filter(name -> name != null && !name.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private static boolean contains(Set<UUID> ids, UUID id) {
        return id != null && ids.contains(id);
    }

    private static boolean contains(Set<String> values, String value) {
        return value != null && values.contains(value.toLowerCase());
    }

    private record Scored(UUID trackId, String reason, double score) {
    }
}
