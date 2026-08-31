package com.harmonia.playback.dto;

import com.harmonia.playback.domain.PlaybackSession;

public record PlaybackStateResponse(
        PlaybackSession session,
        TrackSnapshot track
) {
}
