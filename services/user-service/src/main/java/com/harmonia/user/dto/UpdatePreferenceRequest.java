package com.harmonia.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePreferenceRequest(
        @Size(max = 16) String locale,
        Boolean explicitContent,
        @Pattern(regexp = "light|dark|system", message = "theme must be light, dark, or system") String theme
) {
}
