package com.harmonia.notification.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EventPayloads {

    private static final List<String> SENSITIVE_KEYS = List.of(
            "verificationToken", "token", "password", "email", "resetToken");

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

    public static String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    public static List<UUID> uuidList(Map<String, Object> payload, String... keys) {
        Object raw = value(payload, keys);
        if (raw == null) {
            return List.of();
        }
        if (raw instanceof Collection<?> collection) {
            List<UUID> ids = new ArrayList<>();
            for (Object item : collection) {
                UUID id = uuid(item);
                if (id != null) {
                    ids.add(id);
                }
            }
            return ids;
        }
        UUID single = uuid(raw);
        return single == null ? List.of() : List.of(single);
    }

    public static Map<String, Object> sanitized(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        payload.forEach((key, value) -> {
            if (!SENSITIVE_KEYS.contains(key)) {
                copy.put(key, value);
            }
        });
        return copy;
    }

    public static boolean truthy(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
