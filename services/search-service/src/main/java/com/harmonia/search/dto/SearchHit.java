package com.harmonia.search.dto;

public record SearchHit(
        String id,
        String type,
        String title,
        String subtitle,
        double score
) {
}
