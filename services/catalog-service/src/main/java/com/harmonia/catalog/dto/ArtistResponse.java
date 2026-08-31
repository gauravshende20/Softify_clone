package com.harmonia.catalog.dto;

import com.harmonia.catalog.domain.ArtistStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ArtistResponse(
        UUID id,
        String name,
        String bio,
        String imageKey,
        boolean verified,
        ArtistStatus status,
        Instant createdAt,
        List<GenreResponse> genres,
        List<AlbumSummary> albums
) {
}
