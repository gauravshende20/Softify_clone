package com.harmonia.recommendation.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class JsonCollectionSupport {

    private static final Logger log = LoggerFactory.getLogger(JsonCollectionSupport.class);

    private JsonCollectionSupport() {
    }

    static JsonNode arrayNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            return node;
        }
        if (node.isObject()) {
            for (String field : List.of("content", "items", "ids", "tracks", "artists", "genres", "albums")) {
                JsonNode child = node.get(field);
                if (child != null && child.isArray()) {
                    return child;
                }
            }
        }
        return null;
    }

    static List<UUID> extractIds(JsonNode node, String... idFields) {
        JsonNode array = arrayNode(node);
        if (array == null) {
            return List.of();
        }
        List<UUID> ids = new ArrayList<>();
        for (JsonNode item : array) {
            UUID id = extractId(item, idFields);
            if (id != null) {
                ids.add(id);
            }
        }
        return List.copyOf(ids);
    }

    static UUID extractId(JsonNode item, String... idFields) {
        if (item == null || item.isNull()) {
            return null;
        }
        if (item.isTextual()) {
            return parseUuid(item.asText());
        }
        if (item.isObject()) {
            for (String field : idFields) {
                JsonNode value = item.get(field);
                if (value != null && value.isTextual()) {
                    UUID parsed = parseUuid(value.asText());
                    if (parsed != null) {
                        return parsed;
                    }
                }
            }
        }
        return null;
    }

    static <T> List<T> extractObjects(ObjectMapper mapper, JsonNode node, Class<T> type) {
        JsonNode array = arrayNode(node);
        if (array == null) {
            return List.of();
        }
        List<T> values = new ArrayList<>();
        for (JsonNode item : array) {
            try {
                values.add(mapper.treeToValue(item, type));
            } catch (Exception e) {
                log.debug("Skipping unmapped collection item for {}", type.getSimpleName(), e);
            }
        }
        return List.copyOf(values);
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
