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
public class PlaylistSearchListener {

    private static final Logger log = LoggerFactory.getLogger(PlaylistSearchListener.class);

    private final SearchIndexService indexService;

    public PlaylistSearchListener(SearchIndexService indexService) {
        this.indexService = indexService;
    }

    @KafkaListener(topics = Topics.PLAYLIST, groupId = "${spring.application.name}")
    public void onPlaylistEvent(DomainEvent event) {
        if (event == null || event.eventType() == null) {
            return;
        }
        try {
            EventType type = EventType.valueOf(event.eventType());
            switch (type) {
                case PLAYLIST_CREATED, PLAYLIST_UPDATED -> {
                    if (indexService.isPublic(event)) {
                        indexService.upsert(indexService.fromEvent(event, "playlist"));
                    } else {
                        indexService.delete("playlist", indexService.aggregateId(event));
                    }
                }
                case PLAYLIST_DELETED -> indexService.delete("playlist", indexService.aggregateId(event));
                default -> log.debug("Ignoring playlist event {}", event.eventType());
            }
        } catch (IllegalArgumentException e) {
            log.debug("Unknown playlist event type {}", event.eventType());
        } catch (Exception e) {
            log.error("Failed to process playlist event {}", event.eventType(), e);
        }
    }
}
