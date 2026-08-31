package com.harmonia.catalog.dto;

import com.harmonia.catalog.domain.AlbumStatus;
import jakarta.validation.constraints.NotNull;

public record PatchAlbumStatusRequest(@NotNull AlbumStatus status) {
}
