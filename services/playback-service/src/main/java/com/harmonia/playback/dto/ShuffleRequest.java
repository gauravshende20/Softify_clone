package com.harmonia.playback.dto;

import jakarta.validation.constraints.NotNull;

public record ShuffleRequest(
        @NotNull Boolean enabled
) {
}
