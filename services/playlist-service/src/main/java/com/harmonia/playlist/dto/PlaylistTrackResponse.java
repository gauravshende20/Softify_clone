package com.harmonia.playlist.dto;

import java.time.Instant;
import java.util.UUID;

public record PlaylistTrackResponse(UUID trackId, int position, UUID addedBy, Instant addedAt) {
}
