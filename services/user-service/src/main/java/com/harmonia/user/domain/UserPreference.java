package com.harmonia.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @Column(name = "user_id", columnDefinition = "char(36)")
    private UUID userId;

    @Column(nullable = false, length = 16)
    private String locale = "en";

    @Column(name = "explicit_content", nullable = false)
    private boolean explicitContent = true;

    @Column(nullable = false, length = 16)
    private String theme = "dark";

    protected UserPreference() {
    }

    public static UserPreference defaults(UUID userId) {
        UserPreference preference = new UserPreference();
        preference.userId = userId;
        preference.locale = "en";
        preference.explicitContent = true;
        preference.theme = "dark";
        return preference;
    }

    public void update(String locale, Boolean explicitContent, String theme) {
        if (locale != null && !locale.isBlank()) {
            this.locale = locale;
        }
        if (explicitContent != null) {
            this.explicitContent = explicitContent;
        }
        if (theme != null && !theme.isBlank()) {
            this.theme = theme;
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public String getLocale() {
        return locale;
    }

    public boolean isExplicitContent() {
        return explicitContent;
    }

    public String getTheme() {
        return theme;
    }
}
