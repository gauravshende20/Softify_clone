package com.harmonia.recommendation.spi;

import java.util.UUID;

public record Recommendation(
        UUID trackId,
        String reason,
        double score
) {
}
