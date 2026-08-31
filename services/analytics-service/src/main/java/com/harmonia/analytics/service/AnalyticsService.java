package com.harmonia.analytics.service;

import com.harmonia.analytics.domain.EntityOpenEvent;
import com.harmonia.analytics.domain.PlayEvent;
import com.harmonia.analytics.domain.PlayEventType;
import com.harmonia.analytics.domain.SearchEvent;
import com.harmonia.analytics.dto.AnalyticsOverviewResponse;
import com.harmonia.analytics.dto.PopularTrackResponse;
import com.harmonia.analytics.dto.RecentEventsResponse;
import com.harmonia.analytics.repo.EntityOpenEventRepository;
import com.harmonia.analytics.repo.PlayEventRepository;
import com.harmonia.analytics.repo.SearchEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
    private static final int TOP_N = 10;

    private final PlayEventRepository playEvents;
    private final SearchEventRepository searchEvents;
    private final EntityOpenEventRepository entityOpenEvents;

    public AnalyticsService(PlayEventRepository playEvents,
                            SearchEventRepository searchEvents,
                            EntityOpenEventRepository entityOpenEvents) {
        this.playEvents = playEvents;
        this.searchEvents = searchEvents;
        this.entityOpenEvents = entityOpenEvents;
    }

    @Transactional
    public void recordPlay(UUID userId, UUID trackId, UUID artistId, PlayEventType type,
                           Long positionMs, Instant occurredAt) {
        if (userId == null || trackId == null || type == null) {
            log.debug("Skipping play event with missing identity user={} track={} type={}", userId, trackId, type);
            return;
        }
        playEvents.save(new PlayEvent(userId, trackId, artistId, type, positionMs, occurredAt));
    }

    @Transactional
    public void recordSearch(UUID userId, String query, Instant occurredAt) {
        if (query == null || query.isBlank()) {
            log.debug("Skipping search event with empty query");
            return;
        }
        String normalized = query.length() > 512 ? query.substring(0, 512) : query;
        searchEvents.save(new SearchEvent(userId, normalized, occurredAt));
    }

    @Transactional
    public void recordEntityOpen(UUID userId, String entityType, UUID entityId, Instant occurredAt) {
        if (userId == null || entityType == null || entityType.isBlank() || entityId == null) {
            log.debug("Skipping entity open with missing fields user={} type={} id={}", userId, entityType, entityId);
            return;
        }
        entityOpenEvents.save(new EntityOpenEvent(userId, entityType, entityId, occurredAt));
    }

    @Transactional(readOnly = true)
    public AnalyticsOverviewResponse overview() {
        long totalStreams = playEvents.countByEventType(PlayEventType.PLAY_STARTED);
        long uniqueListeners = playEvents.countUniqueListeners();
        List<AnalyticsOverviewResponse.TrackPopularity> topTracks = playEvents.topTracks(PageRequest.of(0, TOP_N))
                .stream()
                .map(row -> new AnalyticsOverviewResponse.TrackPopularity(row.getTrackId(), row.getStreams()))
                .toList();
        List<AnalyticsOverviewResponse.ArtistPopularity> topArtists = playEvents.topArtists(PageRequest.of(0, TOP_N))
                .stream()
                .map(row -> new AnalyticsOverviewResponse.ArtistPopularity(row.getArtistId(), row.getStreams()))
                .toList();
        return new AnalyticsOverviewResponse(totalStreams, uniqueListeners, topTracks, topArtists);
    }

    @Transactional(readOnly = true)
    public List<PopularTrackResponse> popularTracks(Instant from, Instant to) {
        return playEvents.popularTracks(from, to, PageRequest.of(0, TOP_N)).stream()
                .map(row -> new PopularTrackResponse(row.getTrackId(), row.getStreams()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RecentEventsResponse recent() {
        List<RecentEventsResponse.PlayView> plays = playEvents.findTop50ByOrderByOccurredAtDesc().stream()
                .map(p -> new RecentEventsResponse.PlayView(
                        p.getId(), p.getUserId(), p.getTrackId(), p.getArtistId(),
                        p.getEventType(), p.getPositionMs(), p.getOccurredAt()))
                .toList();
        List<RecentEventsResponse.SearchView> searches = searchEvents.findTop50ByOrderByOccurredAtDesc().stream()
                .map(s -> new RecentEventsResponse.SearchView(s.getId(), s.getUserId(), s.getQuery(), s.getOccurredAt()))
                .toList();
        List<RecentEventsResponse.EntityOpenView> opens = entityOpenEvents.findTop50ByOrderByOccurredAtDesc().stream()
                .map(e -> new RecentEventsResponse.EntityOpenView(
                        e.getId(), e.getUserId(), e.getEntityType(), e.getEntityId(), e.getOccurredAt()))
                .toList();
        return new RecentEventsResponse(plays, searches, opens);
    }
}
