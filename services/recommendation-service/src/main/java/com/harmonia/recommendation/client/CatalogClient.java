package com.harmonia.recommendation.client;

import com.harmonia.recommendation.dto.AlbumSnapshot;
import com.harmonia.recommendation.dto.ArtistSnapshot;
import com.harmonia.recommendation.dto.TrackSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogClient {

    List<TrackSnapshot> popularTracks(int size);

    List<TrackSnapshot> tracksByArtist(UUID artistId, int size);

    List<TrackSnapshot> tracksByGenre(UUID genreId, int size);

    Optional<TrackSnapshot> getTrack(UUID trackId);

    List<AlbumSnapshot> recentAlbums(int size);

    List<ArtistSnapshot> popularArtists(int size);
}
