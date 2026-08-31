package com.harmonia.notification.dto;

import com.harmonia.notification.domain.NotificationPreference;

import java.util.UUID;

public record PreferenceResponse(
        UUID userId,
        boolean emailEnabled,
        boolean inAppEnabled,
        boolean pushEnabled
) {
    public static PreferenceResponse from(NotificationPreference preference) {
        return new PreferenceResponse(
                preference.getUserId(),
                preference.isEmailEnabled(),
                preference.isInAppEnabled(),
                preference.isPushEnabled());
    }
}
