package com.harmonia.playlist.dto;

import com.harmonia.playlist.domain.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePlaylistRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @Size(max = 255) String coverKey,
        Visibility visibility,
        Boolean collaborative
) {
}
