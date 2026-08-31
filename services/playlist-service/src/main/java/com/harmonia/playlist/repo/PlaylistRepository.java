package com.harmonia.playlist.repo;

import com.harmonia.playlist.domain.Playlist;
import com.harmonia.playlist.domain.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {
    List<Playlist> findByOwnerIdOrderByUpdatedAtDesc(UUID ownerId);

    Page<Playlist> findByVisibilityOrderByUpdatedAtDesc(Visibility visibility, Pageable pageable);
}
