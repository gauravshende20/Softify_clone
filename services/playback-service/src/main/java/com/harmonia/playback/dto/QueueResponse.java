package com.harmonia.playback.dto;

import com.harmonia.playback.domain.RepeatMode;

import java.util.List;
import java.util.UUID;

public record QueueResponse(
        List<UUID> queue,
        int index,
        UUID currentTrackId,
        boolean shuffle,
        RepeatMode repeat
) {
}
