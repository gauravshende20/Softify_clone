package com.harmonia.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_events")
public class SearchEvent {

    @Id
    @Column(columnDefinition = "char(36)")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "char(36)")
    private UUID userId;

    @Column(nullable = false, length = 512)
    private String query;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected SearchEvent() {
    }

    public SearchEvent(UUID userId, String query, Instant occurredAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.query = query;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getQuery() {
        return query;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
