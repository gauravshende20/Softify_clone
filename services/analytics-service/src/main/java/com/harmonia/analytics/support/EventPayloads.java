package com.harmonia.analytics.support;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class EventPayloads {

    private EventPayloads() {
    }

    public static Object value(Map<String, Object> payload, String... keys) {
        if (payload == null) {
            return null;
        }
        for (String key : keys) {
            if (payload.containsKey(key) && payload.get(key) != null) {
                return payload.get(key);
            }
        }
        return null;
    }

    public static UUID uuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return UUID.fromString(text);
    }

    public static Long longValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        return Long.parseLong(text);
    }

    public static String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    public static Instant occurredAt(Instant eventTime) {
        return eventTime != null ? eventTime : Instant.now();
    }
}
