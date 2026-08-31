package com.harmonia.analytics.dto;

import java.util.List;
import java.util.UUID;

public record AnalyticsOverviewResponse(
        long totalStreams,
        long uniqueListeners,
        List<TrackPopularity> topTracks,
        List<ArtistPopularity> topArtists
) {
    public record TrackPopularity(UUID trackId, long streams) {
    }

    public record ArtistPopularity(UUID artistId, long streams) {
    }
}
