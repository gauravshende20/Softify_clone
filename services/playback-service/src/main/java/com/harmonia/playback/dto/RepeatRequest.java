package com.harmonia.playback.dto;

import com.harmonia.playback.domain.RepeatMode;
import jakarta.validation.constraints.NotNull;

public record RepeatRequest(
        @NotNull RepeatMode mode
) {
}
