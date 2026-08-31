package com.harmonia.playback.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.playback.domain.PlaybackSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
public class RedisPlaybackSessionStore implements PlaybackSessionStore {

    private static final Logger log = LoggerFactory.getLogger(RedisPlaybackSessionStore.class);
    static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisPlaybackSessionStore(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<PlaybackSession> find(UUID userId) {
        try {
            String json = redis.opsForValue().get(key(userId));
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, PlaybackSession.class));
        } catch (JsonProcessingException e) {
            log.warn("Corrupt playback session for user {}", userId, e);
            return Optional.empty();
        } catch (RuntimeException e) {
            throw HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE, "Session store unavailable", e);
        }
    }

    @Override
    public void save(PlaybackSession session) {
        try {
            redis.opsForValue().set(key(session.userId()), objectMapper.writeValueAsString(session), TTL);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize playback session", e);
        } catch (RuntimeException e) {
            throw HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE, "Session store unavailable", e);
        }
    }

    static String key(UUID userId) {
        return "playback:session:" + userId;
    }
}
