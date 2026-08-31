package com.harmonia.analytics.service;

import com.harmonia.analytics.domain.PlayEvent;
import com.harmonia.analytics.domain.PlayEventType;
import com.harmonia.analytics.dto.AnalyticsOverviewResponse;
import com.harmonia.analytics.kafka.AnalyticsEventConsumer;
import com.harmonia.analytics.repo.EntityOpenEventRepository;
import com.harmonia.analytics.repo.PlayEventRepository;
import com.harmonia.analytics.repo.SearchEventRepository;
import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock PlayEventRepository playEvents;
    @Mock SearchEventRepository searchEvents;
    @Mock EntityOpenEventRepository entityOpenEvents;

    AnalyticsService service;
    AnalyticsEventConsumer consumer;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(playEvents, searchEvents, entityOpenEvents);
        consumer = new AnalyticsEventConsumer(service);
    }

    @Test
    void playbackStartedIsStoredAsPlayStarted() {
        UUID user = UUID.randomUUID();
        UUID track = UUID.randomUUID();
        UUID artist = UUID.randomUUID();
        DomainEvent event = DomainEvent.of(
                EventType.PLAYBACK_STARTED, "Track", track.toString(),
                "playback-service", "trace", user.toString(),
                Map.of("trackId", track.toString(), "artistId", artist.toString(), "positionMs", 1200));

        consumer.handle(event);

        ArgumentCaptor<PlayEvent> captor = ArgumentCaptor.forClass(PlayEvent.class);
        verify(playEvents).save(captor.capture());
        PlayEvent stored = captor.getValue();
        assertEquals(PlayEventType.PLAY_STARTED, stored.getEventType());
        assertEquals(track, stored.getTrackId());
        assertEquals(artist, stored.getArtistId());
        assertEquals(1200L, stored.getPositionMs());
    }

    @Test
    void skippedEventIsStored() {
        UUID user = UUID.randomUUID();
        UUID track = UUID.randomUUID();
        consumer.handle(DomainEvent.of(
                EventType.TRACK_SKIPPED, "Track", track.toString(),
                "playback-service", "trace", user.toString(),
                Map.of("trackId", track)));

        ArgumentCaptor<PlayEvent> captor = ArgumentCaptor.forClass(PlayEvent.class);
        verify(playEvents).save(captor.capture());
        assertEquals(PlayEventType.SKIPPED, captor.getValue().getEventType());
    }

    @Test
    void searchWithoutQueryIsIgnored() {
        consumer.handle(DomainEvent.of(
                EventType.SEARCH_PERFORMED, "Search", UUID.randomUUID().toString(),
                "search-service", "trace", UUID.randomUUID().toString(),
                Map.of()));
        verify(searchEvents, never()).save(any());
    }

    @Test
    void overviewMapsRepositoryAggregates() {
        UUID track = UUID.randomUUID();
        when(playEvents.countByEventType(PlayEventType.PLAY_STARTED)).thenReturn(42L);
        when(playEvents.countUniqueListeners()).thenReturn(7L);
        when(playEvents.topTracks(any(Pageable.class))).thenReturn(List.of(new PlayEventRepository.TrackCountView() {
            @Override
            public UUID getTrackId() {
                return track;
            }

            @Override
            public long getStreams() {
                return 12L;
            }
        }));
        when(playEvents.topArtists(any(Pageable.class))).thenReturn(List.of());

        AnalyticsOverviewResponse overview = service.overview();
        assertEquals(42L, overview.totalStreams());
        assertEquals(7L, overview.uniqueListeners());
        assertEquals(1, overview.topTracks().size());
        assertEquals(track, overview.topTracks().getFirst().trackId());
        assertTrue(overview.topArtists().isEmpty());
    }

    @Test
    void playlistOpenedIsRecorded() {
        UUID user = UUID.randomUUID();
        UUID playlist = UUID.randomUUID();
        consumer.handle(DomainEvent.of(
                EventType.PLAYLIST_OPENED, "Playlist", playlist.toString(),
                "playlist-service", "trace", user.toString(),
                Map.of("playlistId", playlist.toString())));
        verify(entityOpenEvents).save(any());
    }
}
