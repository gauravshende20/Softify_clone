package com.harmonia.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateArtistRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 2000) String bio,
        @Size(max = 255) String imageKey,
        List<UUID> genreIds
) {
}
