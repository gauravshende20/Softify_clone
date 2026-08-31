package com.harmonia.playback.store;

import com.harmonia.playback.domain.PlaybackSession;

import java.util.Optional;
import java.util.UUID;

public interface PlaybackSessionStore {

    Optional<PlaybackSession> find(UUID userId);

    void save(PlaybackSession session);
}
