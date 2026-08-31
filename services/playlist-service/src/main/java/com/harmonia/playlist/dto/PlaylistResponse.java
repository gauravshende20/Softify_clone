package com.harmonia.playlist.dto;

import com.harmonia.playlist.domain.Visibility;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistResponse(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        String coverKey,
        Visibility visibility,
        boolean collaborative,
        Instant createdAt,
        Instant updatedAt,
        List<PlaylistTrackResponse> tracks
) {
}
