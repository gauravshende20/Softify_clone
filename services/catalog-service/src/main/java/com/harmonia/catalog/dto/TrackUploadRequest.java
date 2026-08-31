package com.harmonia.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record TrackUploadRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull UUID artistId,
        UUID albumId,
        @Positive int durationMs,
        boolean explicit,
        Integer trackNumber,
        List<UUID> genreIds
) {
}
