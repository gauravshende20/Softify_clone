package com.harmonia.playback.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrackSnapshot(
        UUID id,
        String title,
        UUID artistId,
        String artistName,
        UUID albumId,
        String albumTitle,
        UUID genreId,
        String genre,
        Integer durationMs,
        Integer popularity,
        String artworkUrl,
        Boolean explicit
) {
}
