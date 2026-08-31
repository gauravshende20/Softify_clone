package com.harmonia.search.dto;

import java.util.List;

public record GroupedSearchResponse(
        List<SearchHit> tracks,
        List<SearchHit> artists,
        List<SearchHit> albums,
        List<SearchHit> playlists,
        List<SearchHit> genres
) {
    public static GroupedSearchResponse empty() {
        return new GroupedSearchResponse(List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public static GroupedSearchResponse of(List<SearchHit> hits) {
        return new GroupedSearchResponse(
                filter(hits, "track"),
                filter(hits, "artist"),
                filter(hits, "album"),
                filter(hits, "playlist"),
                filter(hits, "genre")
        );
    }

    private static List<SearchHit> filter(List<SearchHit> hits, String type) {
        return hits.stream().filter(hit -> type.equalsIgnoreCase(hit.type())).toList();
    }

    public int totalHits() {
        return tracks.size() + artists.size() + albums.size() + playlists.size() + genres.size();
    }
}
