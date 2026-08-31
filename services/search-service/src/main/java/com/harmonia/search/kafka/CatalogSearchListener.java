package com.harmonia.search.kafka;

import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.search.service.SearchIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CatalogSearchListener {

    private static final Logger log = LoggerFactory.getLogger(CatalogSearchListener.class);

    private final SearchIndexService indexService;

    public CatalogSearchListener(SearchIndexService indexService) {
        this.indexService = indexService;
    }

    @KafkaListener(topics = Topics.CATALOG, groupId = "${spring.application.name}")
    public void onCatalogEvent(DomainEvent event) {
        if (event == null || event.eventType() == null) {
            return;
        }
        try {
            EventType type = EventType.valueOf(event.eventType());
            switch (type) {
                case TRACK_PUBLISHED -> indexService.upsert(indexService.fromEvent(event, "track"));
                case ALBUM_CREATED, ALBUM_PUBLISHED -> indexService.upsert(indexService.fromEvent(event, "album"));
                case TRACK_UNPUBLISHED -> indexService.delete("track", indexService.aggregateId(event));
                default -> log.debug("Ignoring catalog event {}", event.eventType());
            }
        } catch (IllegalArgumentException e) {
            log.debug("Unknown catalog event type {}", event.eventType());
        } catch (Exception e) {
            log.error("Failed to process catalog event {}", event.eventType(), e);
        }
    }
}
