package com.harmonia.recommendation.dto;

import com.harmonia.recommendation.spi.Recommendation;

import java.util.List;

public record HomeRecommendations(
        List<Recommendation> madeForYou,
        List<TrackSnapshot> trending,
        List<AlbumSnapshot> newReleases,
        List<ArtistSnapshot> popularArtists
) {
}
