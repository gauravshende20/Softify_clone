package com.harmonia.playback.store;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class RedisRecentlyPlayedStore implements RecentlyPlayedStore {

    static final int MAX_SIZE = 50;
    static final Duration TTL = Duration.ofDays(7);

    private final StringRedisTemplate redis;

    public RedisRecentlyPlayedStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void push(UUID userId, UUID trackId) {
        if (userId == null || trackId == null) {
            return;
        }
        String key = key(userId);
        String value = trackId.toString();
        try {
            redis.opsForList().remove(key, 0, value);
            redis.opsForList().leftPush(key, value);
            redis.opsForList().trim(key, 0, MAX_SIZE - 1);
            redis.expire(key, TTL);
        } catch (RuntimeException e) {
            throw HarmoniaException.serviceUnavailable(ErrorCode.UPSTREAM_UNAVAILABLE, "Recently played store unavailable", e);
        }
    }

    static String key(UUID userId) {
        return "playback:recent:" + userId;
    }
}
