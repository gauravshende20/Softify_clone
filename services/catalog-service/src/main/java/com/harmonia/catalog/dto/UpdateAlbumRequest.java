package com.harmonia.catalog.dto;

import com.harmonia.catalog.domain.AlbumType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateAlbumRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull AlbumType albumType,
        LocalDate releaseDate,
        @Size(max = 255) String artworkKey
) {
}
