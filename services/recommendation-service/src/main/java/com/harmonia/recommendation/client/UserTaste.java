package com.harmonia.recommendation.client;

import com.harmonia.recommendation.dto.TrackSnapshot;

import java.util.List;
import java.util.UUID;

public record UserTaste(
        List<UUID> likedTrackIds,
        List<UUID> followedArtistIds,
        List<UUID> favoriteGenreIds,
        List<UUID> recentlyPlayedTrackIds,
        List<TrackSnapshot> likedTracks,
        List<TrackSnapshot> recentlyPlayedTracks
) {
    public static UserTaste empty() {
        return new UserTaste(List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
