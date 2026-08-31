package com.harmonia.catalog.dto;

import com.harmonia.catalog.domain.TrackStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TrackResponse(
        UUID id,
        String title,
        int durationMs,
        boolean explicit,
        Integer trackNumber,
        TrackStatus status,
        UUID artistId,
        UUID albumId,
        String mimeType,
        long fileSize,
        Instant createdAt,
        List<GenreResponse> genres
) {
}
