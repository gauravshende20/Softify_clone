package com.harmonia.playback.client;

import com.harmonia.playback.dto.StreamUrlResponse;
import com.harmonia.playback.dto.TrackSnapshot;

import java.util.List;
import java.util.UUID;

public interface CatalogClient {

    TrackSnapshot getTrack(UUID trackId);

    StreamUrlResponse getStreamUrl(UUID trackId);

    List<UUID> albumTrackIds(UUID albumId);
}
