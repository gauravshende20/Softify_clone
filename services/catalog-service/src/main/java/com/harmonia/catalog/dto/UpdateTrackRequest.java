package com.harmonia.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateTrackRequest(
        @NotBlank @Size(max = 200) String title,
        UUID albumId,
        @Positive int durationMs,
        boolean explicit,
        Integer trackNumber,
        List<UUID> genreIds
) {
}
