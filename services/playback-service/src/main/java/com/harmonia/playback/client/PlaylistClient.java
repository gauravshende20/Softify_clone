package com.harmonia.playback.client;

import java.util.List;
import java.util.UUID;

public interface PlaylistClient {

    List<UUID> playlistTrackIds(UUID playlistId);
}
