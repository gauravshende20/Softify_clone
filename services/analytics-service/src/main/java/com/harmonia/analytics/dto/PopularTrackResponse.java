package com.harmonia.analytics.dto;

import java.util.UUID;

public record PopularTrackResponse(UUID trackId, long streams) {
}
