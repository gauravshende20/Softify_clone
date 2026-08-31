package com.harmonia.playback.service;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.playback.client.CatalogClient;
import com.harmonia.playback.client.PlaylistClient;
import com.harmonia.playback.domain.PlaybackSession;
import com.harmonia.playback.domain.RepeatMode;
import com.harmonia.playback.dto.PlayRequest;
import com.harmonia.playback.dto.PlaybackStateResponse;
import com.harmonia.playback.dto.QueueResponse;
import com.harmonia.playback.dto.StreamUrlResponse;
import com.harmonia.playback.dto.TrackSnapshot;
import com.harmonia.playback.store.PlaybackSessionStore;
import com.harmonia.playback.store.RecentlyPlayedStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PlaybackService {

    private static final Logger log = LoggerFactory.getLogger(PlaybackService.class);
    private static final String PRODUCER = "playback-service";
    private static final int DEFAULT_VOLUME = 80;
    private static final long RESTART_THRESHOLD_MS = 3_000L;

    private final PlaybackSessionStore sessions;
    private final RecentlyPlayedStore recentlyPlayed;
    private final CatalogClient catalog;
    private final PlaylistClient playlists;
    private final DomainEventPublisher events;

    public PlaybackService(PlaybackSessionStore sessions,
                           RecentlyPlayedStore recentlyPlayed,
                           CatalogClient catalog,
                           PlaylistClient playlists,
                           DomainEventPublisher events) {
        this.sessions = sessions;
        this.recentlyPlayed = recentlyPlayed;
        this.catalog = catalog;
        this.playlists = playlists;
        this.events = events;
    }

    public PlaybackStateResponse play(UUID userId, PlayRequest request) {
        List<UUID> queue = resolveQueue(request);
        if (queue.isEmpty()) {
            throw HarmoniaException.badRequest(ErrorCode.QUEUE_EMPTY, "Playback queue is empty");
        }
        int index = 0;
        if (request.trackId() != null) {
            int found = queue.indexOf(request.trackId());
            if (found >= 0) {
                index = found;
            } else {
                List<UUID> withTrack = new ArrayList<>(queue.size() + 1);
                withTrack.add(request.trackId());
                withTrack.addAll(queue);
                queue = withTrack;
            }
        }
        PlaybackSession previous = sessions.find(userId).orElse(null);
        boolean shuffle = previous != null && previous.shuffle();
        RepeatMode repeat = previous != null ? previous.repeat() : RepeatMode.OFF;
        int volume = previous != null ? previous.volume() : DEFAULT_VOLUME;
        if (shuffle && queue.size() > 1) {
            queue = shuffleAround(queue, index);
            index = 0;
        }
        long position = request.positionMs() == null ? 0L : request.positionMs();
        UUID current = queue.get(index);
        catalog.getTrack(current);
        PlaybackSession session = PlaybackSession.start(userId, queue, index, position, shuffle, repeat, volume);
        persist(session);
        recentlyPlayed.push(userId, current);
        publish(EventType.PLAYBACK_STARTED, "PlaybackSession", userId.toString(), userId,
                Map.of("trackId", current.toString(), "positionMs", position, "queueSize", queue.size()));
        publish(EventType.TRACK_PLAYED, "Track", current.toString(), userId,
                Map.of("trackId", current.toString(), "positionMs", position));
        return toState(session);
    }

    public PlaybackStateResponse pause(UUID userId) {
        PlaybackSession session = requireSession(userId).withPaused(true);
        persist(session);
        return toState(session);
    }

    public PlaybackStateResponse resume(UUID userId) {
        PlaybackSession current = requireSession(userId);
        if (!current.hasQueue() || current.currentTrackId() == null) {
            throw HarmoniaException.badRequest(ErrorCode.QUEUE_EMPTY, "Playback queue is empty");
        }
        PlaybackSession session = current.withPaused(false);
        persist(session);
        publish(EventType.PLAYBACK_STARTED, "PlaybackSession", userId.toString(), userId,
                Map.of("trackId", session.currentTrackId().toString(), "positionMs", session.positionMs()));
        return toState(session);
    }

    public PlaybackStateResponse seek(UUID userId, long positionMs) {
        PlaybackSession session = requireSession(userId).withPosition(positionMs);
        persist(session);
        return toState(session);
    }

    public PlaybackStateResponse next(UUID userId) {
        PlaybackSession current = requireQueuedSession(userId);
        PlaybackSession nextSession;
        boolean completed = false;
        if (current.repeat() == RepeatMode.ONE) {
            nextSession = current.withPosition(0).withPaused(false);
        } else {
            int nextIndex = current.index() + 1;
            if (nextIndex >= current.queue().size()) {
                if (current.repeat() == RepeatMode.ALL) {
                    nextSession = current.withQueue(current.queue(), 0, false);
                } else {
                    nextSession = current.withPaused(true);
                    completed = true;
                }
            } else {
                nextSession = current.withQueue(current.queue(), nextIndex, false);
            }
        }
        persist(nextSession);
        if (completed) {
            publish(EventType.PLAYBACK_COMPLETED, "PlaybackSession", userId.toString(), userId,
                    Map.of("trackId", String.valueOf(current.currentTrackId()), "index", current.index()));
        } else if (nextSession.currentTrackId() != null) {
            recentlyPlayed.push(userId, nextSession.currentTrackId());
            publishStarted(userId, nextSession);
        }
        return toState(nextSession);
    }

    public PlaybackStateResponse previous(UUID userId) {
        PlaybackSession current = requireQueuedSession(userId);
        PlaybackSession previous;
        if (current.positionMs() > RESTART_THRESHOLD_MS) {
            previous = current.withPosition(0).withPaused(false);
        } else if (current.index() > 0) {
            previous = current.withQueue(current.queue(), current.index() - 1, false);
        } else if (current.repeat() == RepeatMode.ALL && current.queue().size() > 1) {
            previous = current.withQueue(current.queue(), current.queue().size() - 1, false);
        } else {
            previous = current.withPosition(0).withPaused(false);
        }
        persist(previous);
        if (previous.currentTrackId() != null) {
            recentlyPlayed.push(userId, previous.currentTrackId());
            publishStarted(userId, previous);
        }
        return toState(previous);
    }

    public QueueResponse enqueue(UUID userId, UUID trackId) {
        catalog.getTrack(trackId);
        PlaybackSession current = sessions.find(userId).orElse(null);
        if (current == null) {
            PlaybackSession session = PlaybackSession.start(userId, List.of(trackId), 0, 0, false, RepeatMode.OFF, DEFAULT_VOLUME)
                    .withPaused(true);
            persist(session);
            return toQueue(session);
        }
        List<UUID> queue = new ArrayList<>(current.queue());
        queue.add(trackId);
        PlaybackSession updated = current.withQueue(queue, current.index(), current.paused()).withPosition(current.positionMs());
        persist(updated);
        return toQueue(updated);
    }

    public QueueResponse removeFromQueue(UUID userId, UUID trackId) {
        PlaybackSession current = requireSession(userId);
        List<UUID> queue = new ArrayList<>(current.queue());
        int removedIndex = queue.indexOf(trackId);
        if (removedIndex < 0) {
            return toQueue(current);
        }
        queue.remove(removedIndex);
        if (queue.isEmpty()) {
            PlaybackSession emptied = current.withQueue(List.of(), 0, true);
            persist(emptied);
            return toQueue(emptied);
        }
        int newIndex = current.index();
        if (removedIndex < current.index()) {
            newIndex = current.index() - 1;
        } else if (removedIndex == current.index() && newIndex >= queue.size()) {
            newIndex = queue.size() - 1;
        }
        long position = removedIndex == current.index() ? 0L : current.positionMs();
        PlaybackSession updated = current.withQueue(queue, newIndex, current.paused()).withPosition(position);
        persist(updated);
        return toQueue(updated);
    }

    public QueueResponse queue(UUID userId) {
        return toQueue(requireSession(userId));
    }

    public PlaybackStateResponse shuffle(UUID userId, boolean enabled) {
        PlaybackSession current = requireSession(userId);
        if (current.shuffle() == enabled) {
            PlaybackSession touched = current.touched();
            persist(touched);
            return toState(touched);
        }
        if (!enabled || current.queue().size() <= 1) {
            PlaybackSession updated = current.withShuffledQueue(current.queue(), enabled);
            persist(updated);
            return toState(updated);
        }
        PlaybackSession shuffled = current.withShuffledQueue(shuffleAround(current.queue(), current.index()), true);
        persist(shuffled);
        return toState(shuffled);
    }

    public PlaybackStateResponse repeat(UUID userId, RepeatMode mode) {
        PlaybackSession session = requireSession(userId).withRepeat(mode);
        persist(session);
        return toState(session);
    }

    public PlaybackStateResponse state(UUID userId) {
        return toState(requireSession(userId));
    }

    public StreamUrlResponse streamUrl(UUID userId) {
        PlaybackSession session = requireQueuedSession(userId);
        return catalog.getStreamUrl(session.currentTrackId());
    }

    private List<UUID> resolveQueue(PlayRequest request) {
        if (request == null) {
            throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Play request is required");
        }
        if (request.queue() != null && !request.queue().isEmpty()) {
            return request.queue().stream().filter(id -> id != null).distinct().toList();
        }
        if (request.albumId() != null) {
            return catalog.albumTrackIds(request.albumId());
        }
        if (request.playlistId() != null) {
            return playlists.playlistTrackIds(request.playlistId());
        }
        if (request.trackId() != null) {
            return List.of(request.trackId());
        }
        throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST,
                "Provide trackId, albumId, playlistId, or queue to start playback");
    }

    private PlaybackSession requireSession(UUID userId) {
        return sessions.find(userId).orElseThrow(() ->
                HarmoniaException.notFound(ErrorCode.PLAYBACK_SESSION_NOT_FOUND, "No active playback session"));
    }

    private PlaybackSession requireQueuedSession(UUID userId) {
        PlaybackSession session = requireSession(userId);
        if (!session.hasQueue() || session.currentTrackId() == null) {
            throw HarmoniaException.badRequest(ErrorCode.QUEUE_EMPTY, "Playback queue is empty");
        }
        return session;
    }

    private void persist(PlaybackSession session) {
        sessions.save(session.touched());
    }

    private PlaybackStateResponse toState(PlaybackSession session) {
        TrackSnapshot track = null;
        if (session.currentTrackId() != null) {
            track = catalog.getTrack(session.currentTrackId());
        }
        return new PlaybackStateResponse(session, track);
    }

    private QueueResponse toQueue(PlaybackSession session) {
        return new QueueResponse(session.queue(), session.index(), session.currentTrackId(), session.shuffle(), session.repeat());
    }

    private void publishStarted(UUID userId, PlaybackSession session) {
        publish(EventType.PLAYBACK_STARTED, "PlaybackSession", userId.toString(), userId,
                Map.of("trackId", session.currentTrackId().toString(), "index", session.index()));
        publish(EventType.TRACK_PLAYED, "Track", session.currentTrackId().toString(), userId,
                Map.of("trackId", session.currentTrackId().toString(), "index", session.index()));
    }

    private void publish(EventType type, String aggregateType, String aggregateId, UUID userId, Map<String, Object> payload) {
        events.publish(Topics.PLAYBACK, DomainEvent.of(
                type, aggregateType, aggregateId, PRODUCER, MDC.get("traceId"),
                userId.toString(), payload));
        log.debug("Published {} for user {}", type, userId);
    }

    static List<UUID> shuffleAround(List<UUID> queue, int currentIndex) {
        if (queue.size() <= 1) {
            return List.copyOf(queue);
        }
        int idx = Math.clamp(currentIndex, 0, queue.size() - 1);
        UUID current = queue.get(idx);
        List<UUID> rest = new ArrayList<>(queue.size() - 1);
        for (int i = 0; i < queue.size(); i++) {
            if (i != idx) {
                rest.add(queue.get(i));
            }
        }
        Collections.shuffle(rest);
        List<UUID> shuffled = new ArrayList<>(queue.size());
        shuffled.add(current);
        shuffled.addAll(rest);
        return List.copyOf(shuffled);
    }
}
