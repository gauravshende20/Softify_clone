package com.harmonia.user.dto;

public record MeResponse(
        ProfileResponse profile,
        PreferenceResponse preferences
) {
}
