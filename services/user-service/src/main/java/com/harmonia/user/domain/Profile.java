package com.harmonia.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "display_name", nullable = false, length = 64)
    private String displayName;

    @Column(name = "avatar_key")
    private String avatarKey;

    @Column(length = 500)
    private String bio;

    @Column(length = 2)
    private String country;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Profile() {
    }

    public static Profile create(UUID accountId, String displayName) {
        Profile profile = new Profile();
        profile.id = accountId;
        profile.displayName = displayName;
        Instant now = Instant.now();
        profile.createdAt = now;
        profile.updatedAt = now;
        return profile;
    }

    public void update(String displayName, String avatarKey, String bio, String country) {
        this.displayName = displayName;
        this.avatarKey = avatarKey;
        this.bio = bio;
        this.country = country;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarKey() {
        return avatarKey;
    }

    public String getBio() {
        return bio;
    }

    public String getCountry() {
        return country;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
