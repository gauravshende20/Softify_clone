package com.harmonia.notification.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePreferencesRequest(
        @NotNull Boolean emailEnabled,
        @NotNull Boolean inAppEnabled,
        @NotNull Boolean pushEnabled
) {
}
