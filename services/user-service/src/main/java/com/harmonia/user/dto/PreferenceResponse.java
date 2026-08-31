package com.harmonia.user.dto;

public record PreferenceResponse(
        String locale,
        boolean explicitContent,
        String theme
) {
}
