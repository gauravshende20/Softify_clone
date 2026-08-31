package com.harmonia.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record UpdateFavoriteGenresRequest(
        @NotNull @Size(max = 20) List<UUID> genreIds
) {
}
