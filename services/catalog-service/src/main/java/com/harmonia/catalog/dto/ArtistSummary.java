package com.harmonia.catalog.dto;

import com.harmonia.catalog.domain.ArtistStatus;

import java.util.UUID;

public record ArtistSummary(
        UUID id,
        String name,
        String imageKey,
        boolean verified,
        ArtistStatus status
) {
}
