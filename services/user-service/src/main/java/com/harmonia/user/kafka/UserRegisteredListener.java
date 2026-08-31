package com.harmonia.user.kafka;

import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.user.service.UserProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class UserRegisteredListener {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredListener.class);

    private final UserProfileService profiles;

    public UserRegisteredListener(UserProfileService profiles) {
        this.profiles = profiles;
    }

    @KafkaListener(topics = Topics.USER, groupId = "user-service")
    public void onUserEvent(DomainEvent event) {
        if (event == null || !EventType.USER_REGISTERED.name().equals(event.eventType())) {
            return;
        }
        Map<String, Object> payload = event.payload();
        if (payload != null && "PASSWORD_RESET".equals(String.valueOf(payload.get("type")))) {
            return;
        }
        try {
            UUID accountId = UUID.fromString(event.aggregateId());
            String email = payload == null || payload.get("email") == null ? "" : payload.get("email").toString();
            profiles.createFromRegistration(accountId, email);
        } catch (RuntimeException ex) {
            log.error("Failed to create profile for registered user {}", event.aggregateId(), ex);
            throw ex;
        }
    }
}
