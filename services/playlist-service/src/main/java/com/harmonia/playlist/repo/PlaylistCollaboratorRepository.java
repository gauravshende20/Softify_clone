package com.harmonia.playlist.repo;

import com.harmonia.playlist.domain.CollaboratorRole;
import com.harmonia.playlist.domain.PlaylistCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlaylistCollaboratorRepository extends JpaRepository<PlaylistCollaborator, PlaylistCollaborator.Pk> {
    boolean existsByPlaylistIdAndUserId(UUID playlistId, UUID userId);

    boolean existsByPlaylistIdAndUserIdAndRole(UUID playlistId, UUID userId, CollaboratorRole role);
}
