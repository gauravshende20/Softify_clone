package com.harmonia.search.service;

import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.search.config.SearchProperties;
import com.harmonia.search.document.SearchDocument;
import com.harmonia.search.index.SearchIndexInitializer;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchIndexService {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexService.class);

    private final OpenSearchClient client;
    private final SearchProperties properties;
    private final SearchIndexInitializer initializer;

    public SearchIndexService(OpenSearchClient client,
                              SearchProperties properties,
                              SearchIndexInitializer initializer) {
        this.client = client;
        this.properties = properties;
        this.initializer = initializer;
    }

    public void upsert(SearchDocument document) {
        if (document == null || document.id() == null || document.type() == null) {
            return;
        }
        try {
            initializer.ensureIndex();
            client.index(i -> i
                    .index(properties.getIndex())
                    .id(documentId(document.type(), document.id()))
                    .document(document));
        } catch (Exception e) {
            log.error("Failed to upsert {} {}", document.type(), document.id(), e);
        }
    }

    public void delete(String type, String id) {
        if (type == null || id == null) {
            return;
        }
        try {
            initializer.ensureIndex();
            client.delete(d -> d.index(properties.getIndex()).id(documentId(type, id)));
        } catch (OpenSearchException e) {
            if (e.status() != 404) {
                log.error("Failed to delete {} {}", type, id, e);
            }
        } catch (Exception e) {
            log.error("Failed to delete {} {}", type, id, e);
        }
    }

    public SearchDocument fromEvent(DomainEvent event, String type) {
        Map<String, Object> payload = event.payload() == null ? Map.of() : event.payload();
        String id = firstNonBlank(event.aggregateId(), str(payload, "id"));
        String title = firstNonBlank(str(payload, "title", "name"), event.aggregateType());
        String subtitle = str(payload, "artistName", "artist", "subtitle", "ownerName", "owner");
        String genre = str(payload, "genre", "genreName");
        String description = str(payload, "description", "albumTitle", "album");
        String text = List.of(title, subtitle, genre, description).stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(" "));
        return new SearchDocument(
                id,
                type.toLowerCase(Locale.ROOT),
                title,
                subtitle.isBlank() ? null : subtitle,
                text,
                integer(payload, "popularity"),
                genre.isBlank() ? null : genre,
                instant(payload.get("createdAt"), event.occurredAt())
        );
    }

    public boolean isPublic(DomainEvent event) {
        Map<String, Object> payload = event.payload() == null ? Map.of() : event.payload();
        Object visibility = payload.get("visibility");
        if (visibility == null) {
            return true;
        }
        String value = visibility.toString();
        return "PUBLIC".equalsIgnoreCase(value) || "public".equalsIgnoreCase(value);
    }

    public String aggregateId(DomainEvent event) {
        if (event == null) {
            return null;
        }
        Map<String, Object> payload = event.payload() == null ? Map.of() : event.payload();
        return firstNonBlank(event.aggregateId(), str(payload, "id"));
    }

    static String documentId(String type, String id) {
        return type.toLowerCase(Locale.ROOT) + "_" + id;
    }

    private static String str(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value != null) {
                String text = value.toString().trim();
                if (!text.isEmpty() && !"null".equalsIgnoreCase(text)) {
                    return text;
                }
            }
        }
        return "";
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static Integer integer(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Instant instant(Object value, Instant fallback) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof Number number) {
            long epoch = number.longValue();
            return epoch > 10_000_000_000L ? Instant.ofEpochMilli(epoch) : Instant.ofEpochSecond(epoch);
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Instant.parse(text);
            } catch (Exception ignored) {
                return fallback;
            }
        }
        return fallback == null ? Instant.now() : fallback;
    }
}
