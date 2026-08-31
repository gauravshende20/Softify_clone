package com.harmonia.user.dto;

import java.time.Instant;
import java.util.UUID;

public record RecentlyPlayedResponse(UUID id, UUID trackId, Instant playedAt) {
}
