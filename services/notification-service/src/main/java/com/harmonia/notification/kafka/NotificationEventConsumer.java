package com.harmonia.notification.kafka;

import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.notification.service.NotificationService;
import com.harmonia.notification.support.EventPayloads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notifications;

    public NotificationEventConsumer(NotificationService notifications) {
        this.notifications = notifications;
    }

    @KafkaListener(topics = Topics.USER, groupId = "notification-service")
    public void onUser(DomainEvent event) {
        handle(event);
    }

    @KafkaListener(topics = Topics.CATALOG, groupId = "notification-service")
    public void onCatalog(DomainEvent event) {
        handle(event);
    }

    @KafkaListener(topics = Topics.PLAYLIST, groupId = "notification-service")
    public void onPlaylist(DomainEvent event) {
        handle(event);
    }

    @KafkaListener(topics = Topics.SOCIAL, groupId = "notification-service")
    public void onSocial(DomainEvent event) {
        handle(event);
    }

    public void handle(DomainEvent event) {
        if (event == null || event.eventType() == null) {
            return;
        }
        EventType type;
        try {
            type = EventType.valueOf(event.eventType());
        } catch (IllegalArgumentException ex) {
            log.debug("Ignoring unknown notification event {}", event.eventType());
            return;
        }
        Map<String, Object> payload = event.payload() == null ? Map.of() : event.payload();
        UUID actor = EventPayloads.uuid(event.userId());
        if (actor == null) {
            actor = EventPayloads.uuid(EventPayloads.value(payload, "userId", "user_id"));
        }
        switch (type) {
            case USER_REGISTERED -> handleRegistered(actor, payload);
            case ALBUM_PUBLISHED -> fanoutFollowers(
                    "ALBUM_PUBLISHED",
                    "New album from an artist you follow",
                    "A new album is available in the catalog.",
                    payload);
            case TRACK_PUBLISHED -> fanoutFollowers(
                    "TRACK_PUBLISHED",
                    "New track from an artist you follow",
                    "A new track just dropped.",
                    payload);
            case PLAYLIST_UPDATED -> handlePlaylistUpdated(payload);
            case ARTIST_FOLLOWED -> log.info(
                    "Observed ARTIST_FOLLOWED artist={} follower={}",
                    EventPayloads.value(payload, "artistId", "artist_id", "aggregateId"),
                    actor);
            default -> log.debug("Notification service ignoring {}", type);
        }
    }

    private void handleRegistered(UUID userId, Map<String, Object> payload) {
        String email = EventPayloads.text(EventPayloads.value(payload, "email"));
        Map<String, Object> metadata = NotificationService.withEmail(EventPayloads.sanitized(payload), email);
        notifications.dispatch(
                userId,
                "USER_REGISTERED",
                "Welcome to Harmonia",
                "Your account is ready. Start exploring the catalog.",
                metadata);
    }

    private void fanoutFollowers(String type, String title, String body, Map<String, Object> payload) {
        List<UUID> followers = EventPayloads.uuidList(payload, "followerUserIds", "follower_user_ids");
        if (followers.isEmpty()) {
            log.debug("Skipping fanout for {} - no followerUserIds in payload", type);
            return;
        }
        Map<String, Object> metadata = EventPayloads.sanitized(payload);
        for (UUID follower : followers) {
            notifications.dispatch(follower, type, title, body, metadata);
        }
    }

    private void handlePlaylistUpdated(Map<String, Object> payload) {
        List<UUID> collaborators = EventPayloads.uuidList(payload, "collaboratorIds", "collaborator_ids");
        if (collaborators.isEmpty()) {
            log.debug("Skipping PLAYLIST_UPDATED - no collaboratorIds");
            return;
        }
        Map<String, Object> metadata = EventPayloads.sanitized(payload);
        String title = "Collaborative playlist updated";
        String body = "A playlist you collaborate on was updated.";
        for (UUID collaborator : collaborators) {
            notifications.dispatch(collaborator, "PLAYLIST_UPDATED", title, body, metadata);
        }
    }
}
