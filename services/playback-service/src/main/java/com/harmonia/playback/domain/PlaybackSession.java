package com.harmonia.playback.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlaybackSession(
        UUID userId,
        UUID currentTrackId,
        List<UUID> queue,
        int index,
        long positionMs,
        boolean shuffle,
        RepeatMode repeat,
        boolean paused,
        int volume,
        Instant updatedAt
) {
    public PlaybackSession {
        queue = queue == null ? List.of() : List.copyOf(queue);
        repeat = repeat == null ? RepeatMode.OFF : repeat;
        volume = Math.clamp(volume, 0, 100);
        index = Math.max(index, 0);
        positionMs = Math.max(positionMs, 0);
    }

    public static PlaybackSession start(UUID userId, List<UUID> queue, int index, long positionMs,
                                        boolean shuffle, RepeatMode repeat, int volume) {
        List<UUID> q = queue == null ? List.of() : List.copyOf(queue);
        UUID current = q.isEmpty() || index >= q.size() ? null : q.get(index);
        return new PlaybackSession(userId, current, q, index, positionMs, shuffle, repeat, false, volume, Instant.now());
    }

    public PlaybackSession touched() {
        return new PlaybackSession(userId, currentTrackId, queue, index, positionMs, shuffle, repeat, paused, volume,
                Instant.now());
    }

    public PlaybackSession withPaused(boolean value) {
        return new PlaybackSession(userId, currentTrackId, queue, index, positionMs, shuffle, repeat, value, volume,
                Instant.now());
    }

    public PlaybackSession withPosition(long ms) {
        return new PlaybackSession(userId, currentTrackId, queue, index, Math.max(ms, 0), shuffle, repeat, paused, volume,
                Instant.now());
    }

    public PlaybackSession withRepeat(RepeatMode mode) {
        return new PlaybackSession(userId, currentTrackId, queue, index, positionMs, shuffle, mode, paused, volume,
                Instant.now());
    }

    public PlaybackSession withQueue(List<UUID> newQueue, int newIndex, boolean paused) {
        List<UUID> q = newQueue == null ? List.of() : List.copyOf(newQueue);
        int idx = q.isEmpty() ? 0 : Math.clamp(newIndex, 0, q.size() - 1);
        UUID current = q.isEmpty() ? null : q.get(idx);
        return new PlaybackSession(userId, current, q, idx, 0, shuffle, repeat, paused, volume, Instant.now());
    }

    public PlaybackSession withShuffledQueue(List<UUID> newQueue, boolean enabled) {
        List<UUID> q = newQueue == null ? List.of() : List.copyOf(newQueue);
        UUID current = currentTrackId != null && q.contains(currentTrackId)
                ? currentTrackId
                : (q.isEmpty() ? null : q.getFirst());
        int idx = current == null ? 0 : q.indexOf(current);
        return new PlaybackSession(userId, current, q, Math.max(idx, 0), positionMs, enabled, repeat, paused, volume,
                Instant.now());
    }

    public boolean hasQueue() {
        return !queue.isEmpty();
    }
}
