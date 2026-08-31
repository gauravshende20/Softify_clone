package com.harmonia.playback.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record QueueTrackRequest(
        @NotNull UUID trackId
) {
}
