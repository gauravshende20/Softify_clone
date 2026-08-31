package com.harmonia.user.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String displayName,
        String avatarKey,
        String bio,
        String country,
        Instant createdAt,
        Instant updatedAt,
        List<UUID> favoriteGenreIds
) {
}
