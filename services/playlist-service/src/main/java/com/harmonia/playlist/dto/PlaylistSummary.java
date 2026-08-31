package com.harmonia.playlist.dto;

import com.harmonia.playlist.domain.Visibility;

import java.time.Instant;
import java.util.UUID;

public record PlaylistSummary(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        String coverKey,
        Visibility visibility,
        boolean collaborative,
        Instant createdAt,
        Instant updatedAt
) {
}
