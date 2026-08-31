package com.harmonia.user.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicProfileResponse(
        UUID id,
        String displayName,
        String avatarKey,
        String bio,
        String country,
        Instant createdAt,
        List<UUID> favoriteGenreIds
) {
}
