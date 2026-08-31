package com.harmonia.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlbumSnapshot(
        UUID id,
        String title,
        UUID artistId,
        String artistName,
        String artworkUrl,
        Instant releaseDate,
        Instant createdAt
) {
}
