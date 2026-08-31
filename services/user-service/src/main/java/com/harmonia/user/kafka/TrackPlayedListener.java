package com.harmonia.user.kafka;

import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.user.service.SocialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class TrackPlayedListener {

    private static final Logger log = LoggerFactory.getLogger(TrackPlayedListener.class);

    private final SocialService socialService;

    public TrackPlayedListener(SocialService socialService) {
        this.socialService = socialService;
    }

    @KafkaListener(topics = Topics.PLAYBACK, groupId = "user-service")
    public void onPlaybackEvent(DomainEvent event) {
        if (event == null || !EventType.TRACK_PLAYED.name().equals(event.eventType())) {
            return;
        }
        try {
            Map<String, Object> payload = event.payload();
            UUID userId = firstUuid(event.userId(), payload, "userId");
            UUID trackId = firstUuid(payloadValue(payload, "trackId"), event.aggregateId());
            if (userId == null || trackId == null) {
                log.warn("Ignoring TRACK_PLAYED without userId/trackId eventId={}", event.eventId());
                return;
            }
            socialService.recordPlayback(userId, trackId);
        } catch (RuntimeException ex) {
            log.error("Failed to record recently played for event {}", event.eventId(), ex);
            throw ex;
        }
    }

    private static UUID firstUuid(String... candidates) {
        for (String candidate : candidates) {
            UUID parsed = parseUuid(candidate);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private static UUID firstUuid(String direct, Map<String, Object> payload, String key) {
        UUID parsed = parseUuid(direct);
        if (parsed != null) {
            return parsed;
        }
        return parseUuid(payloadValue(payload, key));
    }

    private static String payloadValue(Map<String, Object> payload, String key) {
        if (payload == null || payload.get(key) == null) {
            return null;
        }
        return payload.get(key).toString();
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
