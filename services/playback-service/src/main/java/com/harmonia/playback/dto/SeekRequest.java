package com.harmonia.playback.dto;

import jakarta.validation.constraints.Min;

public record SeekRequest(
        @Min(0) long positionMs
) {
}
