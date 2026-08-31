package com.harmonia.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ArtistSnapshot(
        UUID id,
        String name,
        String imageUrl,
        Integer popularity,
        Integer followerCount
) {
}
