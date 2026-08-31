package com.harmonia.search.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchDocument(
        String id,
        String type,
        String title,
        String subtitle,
        String text,
        Integer popularity,
        String genre,
        Instant createdAt
) {
}
