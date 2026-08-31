package com.harmonia.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGenreRequest(@NotBlank @Size(max = 64) String name) {
}
