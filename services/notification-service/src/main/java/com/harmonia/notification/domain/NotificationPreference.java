package com.harmonia.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @Column(name = "user_id", columnDefinition = "char(36)")
    private UUID userId;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled = true;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    protected NotificationPreference() {
    }

    public NotificationPreference(UUID userId) {
        this.userId = userId;
        this.emailEnabled = true;
        this.inAppEnabled = true;
        this.pushEnabled = false;
    }

    public void update(boolean emailEnabled, boolean inAppEnabled, boolean pushEnabled) {
        this.emailEnabled = emailEnabled;
        this.inAppEnabled = inAppEnabled;
        this.pushEnabled = pushEnabled;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public boolean isInAppEnabled() {
        return inAppEnabled;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }
}
