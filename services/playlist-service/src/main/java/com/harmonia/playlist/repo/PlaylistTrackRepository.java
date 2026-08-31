package com.harmonia.playlist.repo;

import com.harmonia.playlist.domain.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, PlaylistTrack.Pk> {
    List<PlaylistTrack> findByPlaylistIdOrderByPositionAsc(UUID playlistId);

    boolean existsByPlaylistIdAndTrackId(UUID playlistId, UUID trackId);

    void deleteByPlaylistIdAndTrackId(UUID playlistId, UUID trackId);

    void deleteByPlaylistId(UUID playlistId);

    long countByPlaylistId(UUID playlistId);
}
