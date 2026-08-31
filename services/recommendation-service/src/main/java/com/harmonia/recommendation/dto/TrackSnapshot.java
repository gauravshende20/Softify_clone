package com.harmonia.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackSnapshot(
        UUID id,
        UUID trackId,
        String title,
        UUID artistId,
        String artistName,
        UUID albumId,
        String albumTitle,
        UUID genreId,
        String genre,
        Integer durationMs,
        Integer popularity,
        String artworkUrl
) {
    public UUID resolvedId() {
        return id != null ? id : trackId;
    }
}
