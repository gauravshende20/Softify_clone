package com.harmonia.user.dto;

import java.time.Instant;
import java.util.UUID;

public record FollowedArtistResponse(UUID artistId, Instant followedAt) {
}
