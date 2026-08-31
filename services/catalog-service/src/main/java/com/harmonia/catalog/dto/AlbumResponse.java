package com.harmonia.catalog.dto;

import com.harmonia.catalog.domain.AlbumStatus;
import com.harmonia.catalog.domain.AlbumType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AlbumResponse(
        UUID id,
        String title,
        AlbumType albumType,
        LocalDate releaseDate,
        String artworkKey,
        AlbumStatus status,
        ArtistSummary artist,
        List<TrackSummary> tracks
) {
}
