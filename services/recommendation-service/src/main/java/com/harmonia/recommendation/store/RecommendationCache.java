package com.harmonia.recommendation.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmonia.recommendation.dto.HomeRecommendations;
import com.harmonia.recommendation.spi.Recommendation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RecommendationCache {

    private static final Logger log = LoggerFactory.getLogger(RecommendationCache.class);
    static final Duration TTL = Duration.ofMinutes(10);
    private static final TypeReference<List<Recommendation>> REC_LIST = new TypeReference<>() {
    };

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RecommendationCache(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public Optional<List<Recommendation>> get(UUID userId) {
        return read(key(userId), REC_LIST);
    }

    public void put(UUID userId, List<Recommendation> recommendations) {
        write(key(userId), recommendations);
    }

    public Optional<HomeRecommendations> getHome(UUID userId) {
        return read(homeKey(userId), new TypeReference<>() {
        });
    }

    public void putHome(UUID userId, HomeRecommendations home) {
        write(homeKey(userId), home);
    }

    private <T> Optional<T> read(String key, TypeReference<T> type) {
        try {
            String json = redis.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, type));
        } catch (Exception e) {
            log.warn("Failed to read recommendation cache {}", key, e);
            return Optional.empty();
        }
    }

    private void write(String key, Object value) {
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(value), TTL);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize recommendation cache {}", key, e);
        } catch (RuntimeException e) {
            log.warn("Failed to write recommendation cache {}", key, e);
        }
    }

    static String key(UUID userId) {
        return "recs:" + userId;
    }

    static String homeKey(UUID userId) {
        return "recs:home:" + userId;
    }
}
