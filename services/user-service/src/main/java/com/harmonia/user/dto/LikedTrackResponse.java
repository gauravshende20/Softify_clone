package com.harmonia.user.dto;

import java.time.Instant;
import java.util.UUID;

public record LikedTrackResponse(UUID trackId, Instant likedAt) {
}
