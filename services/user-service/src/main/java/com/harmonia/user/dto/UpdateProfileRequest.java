package com.harmonia.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 64) String displayName,
        @Size(max = 255) String avatarKey,
        @Size(max = 500) String bio,
        @Size(min = 2, max = 2) @Pattern(regexp = "[A-Za-z]{2}", message = "country must be an ISO-3166 alpha-2 code")
        String country
) {
}
