package com.harmonia.catalog.dto;

import com.harmonia.catalog.domain.AlbumStatus;
import com.harmonia.catalog.domain.AlbumType;

import java.time.LocalDate;
import java.util.UUID;

public record AlbumSummary(
        UUID id,
        String title,
        AlbumType albumType,
        LocalDate releaseDate,
        String artworkKey,
        AlbumStatus status
) {
}
