package com.harmonia.catalog.dto;

import com.harmonia.catalog.domain.ArtistStatus;
import jakarta.validation.constraints.NotNull;

public record PatchArtistStatusRequest(@NotNull ArtistStatus status) {
}
