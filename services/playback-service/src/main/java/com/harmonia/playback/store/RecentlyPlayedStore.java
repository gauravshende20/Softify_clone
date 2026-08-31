package com.harmonia.playback.store;

import java.util.UUID;

public interface RecentlyPlayedStore {

    void push(UUID userId, UUID trackId);
}
