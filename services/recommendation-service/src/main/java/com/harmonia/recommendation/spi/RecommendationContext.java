package com.harmonia.recommendation.spi;

import java.util.List;
import java.util.UUID;

public record RecommendationContext(
        UUID userId,
        List<UUID> likedTrackIds,
        List<UUID> followedArtistIds,
        List<UUID> favoriteGenreIds,
        List<UUID> recentlyPlayedTrackIds,
        List<UUID> popularTrackIds
) {
    public RecommendationContext {
        likedTrackIds = likedTrackIds == null ? List.of() : List.copyOf(likedTrackIds);
        followedArtistIds = followedArtistIds == null ? List.of() : List.copyOf(followedArtistIds);
        favoriteGenreIds = favoriteGenreIds == null ? List.of() : List.copyOf(favoriteGenreIds);
        recentlyPlayedTrackIds = recentlyPlayedTrackIds == null ? List.of() : List.copyOf(recentlyPlayedTrackIds);
        popularTrackIds = popularTrackIds == null ? List.of() : List.copyOf(popularTrackIds);
    }
}
