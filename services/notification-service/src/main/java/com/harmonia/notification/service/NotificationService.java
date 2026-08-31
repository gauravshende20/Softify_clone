package com.harmonia.notification.service;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.notification.channel.EmailChannel;
import com.harmonia.notification.channel.InAppChannel;
import com.harmonia.notification.channel.NotificationMessage;
import com.harmonia.notification.domain.Notification;
import com.harmonia.notification.domain.NotificationPreference;
import com.harmonia.notification.dto.NotificationResponse;
import com.harmonia.notification.dto.PreferenceResponse;
import com.harmonia.notification.dto.UpdatePreferencesRequest;
import com.harmonia.notification.repo.NotificationPreferenceRepository;
import com.harmonia.notification.repo.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final NotificationPreferenceRepository preferences;
    private final InAppChannel inAppChannel;
    private final EmailChannel emailChannel;

    public NotificationService(NotificationRepository notifications,
                               NotificationPreferenceRepository preferences,
                               InAppChannel inAppChannel,
                               EmailChannel emailChannel) {
        this.notifications = notifications;
        this.preferences = preferences;
        this.inAppChannel = inAppChannel;
        this.emailChannel = emailChannel;
    }

    public void dispatch(UUID userId, String type, String title, String body, Map<String, Object> metadata) {
        if (userId == null) {
            log.debug("Skipping notification {} without user", type);
            return;
        }
        NotificationPreference preference = preferences.findById(userId)
                .orElseGet(() -> new NotificationPreference(userId));
        Map<String, Object> inAppMetadata = new LinkedHashMap<>(metadata == null ? Map.of() : metadata);
        inAppMetadata.remove("email");
        if (preference.isInAppEnabled()) {
            inAppChannel.send(new NotificationMessage(userId, type, title, body, inAppMetadata));
        }
        if (preference.isEmailEnabled()) {
            emailChannel.send(new NotificationMessage(userId, type, title, body, metadata));
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(UUID userId, int page, int size) {
        Page<Notification> result = notifications.findByUserIdOrderByReadFlagAscCreatedAtDesc(
                userId, PageRequest.of(page, size));
        return PageResponse.of(
                result.getContent().stream().map(NotificationResponse::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements());
    }

    @Transactional
    public NotificationResponse markRead(UUID userId, UUID notificationId) {
        Notification notification = notifications.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> HarmoniaException.notFound(ErrorCode.NOT_FOUND, "Notification not found"));
        notification.markRead();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notifications.markAllRead(userId);
    }

    @Transactional
    public PreferenceResponse getPreferences(UUID userId) {
        NotificationPreference preference = preferences.findById(userId)
                .orElseGet(() -> preferences.save(new NotificationPreference(userId)));
        return PreferenceResponse.from(preference);
    }

    @Transactional
    public PreferenceResponse updatePreferences(UUID userId, UpdatePreferencesRequest request) {
        NotificationPreference preference = preferences.findById(userId)
                .orElseGet(() -> new NotificationPreference(userId));
        preference.update(request.emailEnabled(), request.inAppEnabled(), request.pushEnabled());
        return PreferenceResponse.from(preferences.save(preference));
    }

    public static Map<String, Object> withEmail(Map<String, Object> metadata, String email) {
        Map<String, Object> copy = new LinkedHashMap<>(metadata == null ? Map.of() : metadata);
        if (email != null && !email.isBlank()) {
            copy.put("email", email);
        }
        return copy;
    }
}
