package com.harmonia.playback.dto;

import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.UUID;

public record PlayRequest(
        UUID trackId,
        UUID playlistId,
        UUID albumId,
        List<UUID> queue,
        @Min(0) Long positionMs
) {
}
