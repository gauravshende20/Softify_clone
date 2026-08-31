package com.harmonia.catalog.dto;

import com.harmonia.catalog.domain.TrackStatus;

import java.util.UUID;

public record TrackSummary(
        UUID id,
        String title,
        int durationMs,
        boolean explicit,
        Integer trackNumber,
        TrackStatus status,
        UUID artistId,
        String artistName,
        UUID albumId,
        String albumTitle,
        String artworkKey
) {
}
