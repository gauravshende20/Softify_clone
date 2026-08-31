package com.harmonia.playback.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StreamUrlResponse(
        String url,
        Long expiresIn
) {
}
