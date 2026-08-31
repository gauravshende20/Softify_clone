package com.harmonia.playback.service;

import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.playback.client.CatalogClient;
import com.harmonia.playback.client.PlaylistClient;
import com.harmonia.playback.domain.PlaybackSession;
import com.harmonia.playback.domain.RepeatMode;
import com.harmonia.playback.dto.PlayRequest;
import com.harmonia.playback.dto.PlaybackStateResponse;
import com.harmonia.playback.dto.TrackSnapshot;
import com.harmonia.playback.store.PlaybackSessionStore;
import com.harmonia.playback.store.RecentlyPlayedStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PlaybackServiceTest {

    @Mock CatalogClient catalog;
    @Mock PlaylistClient playlists;
    @Mock DomainEventPublisher events;

    InMemorySessionStore sessions;
    InMemoryRecentStore recentlyPlayed;
    PlaybackService service;

    UUID userId;
    UUID trackA;
    UUID trackB;
    UUID trackC;

    @BeforeEach
    void setUp() {
        sessions = new InMemorySessionStore();
        recentlyPlayed = new InMemoryRecentStore();
        service = new PlaybackService(sessions, recentlyPlayed, catalog, playlists, events);
        userId = UUID.randomUUID();
        trackA = UUID.randomUUID();
        trackB = UUID.randomUUID();
        trackC = UUID.randomUUID();
        lenient().when(catalog.getTrack(any())).thenAnswer(inv -> snapshot(inv.getArgument(0)));
    }

    @Test
    void playWithOnlyTrackIdUsesSingleItemQueue() {
        PlaybackStateResponse state = service.play(userId, new PlayRequest(trackA, null, null, null, null));
        assertEquals(List.of(trackA), state.session().queue());
        assertEquals(0, state.session().index());
        assertEquals(trackA, state.session().currentTrackId());
        assertFalse(state.session().paused());
        assertEquals(trackA, recentlyPlayed.last(userId));
    }

    @Test
    void nextAdvancesIndex() {
        seed(List.of(trackA, trackB, trackC), 0, RepeatMode.OFF);
        PlaybackStateResponse state = service.next(userId);
        assertEquals(trackB, state.session().currentTrackId());
        assertEquals(1, state.session().index());
        assertFalse(state.session().paused());
    }

    @Test
    void nextWrapsWhenRepeatAll() {
        seed(List.of(trackA, trackB, trackC), 2, RepeatMode.ALL);
        PlaybackStateResponse state = service.next(userId);
        assertEquals(trackA, state.session().currentTrackId());
        assertEquals(0, state.session().index());
        assertFalse(state.session().paused());
    }

    @Test
    void nextStopsWhenRepeatOffAtEnd() {
        seed(List.of(trackA, trackB), 1, RepeatMode.OFF);
        PlaybackStateResponse state = service.next(userId);
        assertEquals(trackB, state.session().currentTrackId());
        assertEquals(1, state.session().index());
        assertTrue(state.session().paused());
    }

    @Test
    void nextRepeatsSameTrackWhenRepeatOne() {
        seed(List.of(trackA, trackB, trackC), 1, RepeatMode.ONE);
        PlaybackStateResponse state = service.next(userId);
        assertEquals(trackB, state.session().currentTrackId());
        assertEquals(1, state.session().index());
        assertEquals(0, state.session().positionMs());
        assertFalse(state.session().paused());
    }

    @Test
    void previousGoesToPriorTrack() {
        seed(List.of(trackA, trackB, trackC), 2, RepeatMode.OFF);
        PlaybackStateResponse state = service.previous(userId);
        assertEquals(trackB, state.session().currentTrackId());
        assertEquals(1, state.session().index());
    }

    @Test
    void previousRestartsCurrentWhenPositionPastThreshold() {
        PlaybackSession session = new PlaybackSession(
                userId, trackB, List.of(trackA, trackB, trackC), 1, 5_000, false, RepeatMode.OFF, false, 80, Instant.now());
        sessions.save(session);
        PlaybackStateResponse state = service.previous(userId);
        assertEquals(trackB, state.session().currentTrackId());
        assertEquals(1, state.session().index());
        assertEquals(0, state.session().positionMs());
    }

    @Test
    void previousWrapsWhenRepeatAllAtStart() {
        seed(List.of(trackA, trackB, trackC), 0, RepeatMode.ALL);
        PlaybackStateResponse state = service.previous(userId);
        assertEquals(trackC, state.session().currentTrackId());
        assertEquals(2, state.session().index());
    }

    @Test
    void shuffleKeepsCurrentTrackFirst() {
        seed(List.of(trackA, trackB, trackC), 1, RepeatMode.OFF);
        PlaybackStateResponse state = service.shuffle(userId, true);
        assertTrue(state.session().shuffle());
        assertEquals(trackB, state.session().currentTrackId());
        assertEquals(0, state.session().index());
        assertEquals(trackB, state.session().queue().getFirst());
        assertEquals(3, state.session().queue().size());
        assertTrue(state.session().queue().containsAll(List.of(trackA, trackB, trackC)));
    }

    @Test
    void shuffleDisableKeepsCurrentOrder() {
        seed(List.of(trackA, trackB, trackC), 0, RepeatMode.OFF);
        PlaybackSession shuffled = service.shuffle(userId, true).session();
        PlaybackSession restored = service.shuffle(userId, false).session();
        assertFalse(restored.shuffle());
        assertEquals(shuffled.queue(), restored.queue());
        assertEquals(shuffled.currentTrackId(), restored.currentTrackId());
    }

    @Test
    void repeatModeIsStoredOnSession() {
        seed(List.of(trackA, trackB), 0, RepeatMode.OFF);
        assertEquals(RepeatMode.ALL, service.repeat(userId, RepeatMode.ALL).session().repeat());
        assertEquals(RepeatMode.ONE, service.repeat(userId, RepeatMode.ONE).session().repeat());
        assertEquals(RepeatMode.OFF, service.repeat(userId, RepeatMode.OFF).session().repeat());
    }

    @Test
    void shuffleAroundPlacesCurrentFirstAndKeepsAllTracks() {
        List<UUID> original = List.of(trackA, trackB, trackC);
        List<UUID> shuffled = PlaybackService.shuffleAround(original, 2);
        assertEquals(trackC, shuffled.getFirst());
        assertEquals(3, shuffled.size());
        assertTrue(shuffled.containsAll(original));
    }

    private void seed(List<UUID> queue, int index, RepeatMode repeat) {
        sessions.save(new PlaybackSession(
                userId, queue.get(index), queue, index, 0, false, repeat, false, 80, Instant.now()));
    }

    private static TrackSnapshot snapshot(UUID id) {
        return new TrackSnapshot(id, "Track " + id, UUID.randomUUID(), "Artist", null, null, null, "pop",
                180_000, 50, null, false);
    }

    static final class InMemorySessionStore implements PlaybackSessionStore {
        private final Map<UUID, PlaybackSession> data = new ConcurrentHashMap<>();

        @Override
        public Optional<PlaybackSession> find(UUID userId) {
            return Optional.ofNullable(data.get(userId));
        }

        @Override
        public void save(PlaybackSession session) {
            data.put(session.userId(), session);
        }
    }

    static final class InMemoryRecentStore implements RecentlyPlayedStore {
        private final Map<UUID, List<UUID>> data = new ConcurrentHashMap<>();

        @Override
        public void push(UUID userId, UUID trackId) {
            data.computeIfAbsent(userId, id -> new ArrayList<>()).addFirst(trackId);
        }

        UUID last(UUID userId) {
            List<UUID> list = data.getOrDefault(userId, List.of());
            return list.isEmpty() ? null : list.getFirst();
        }
    }
}
