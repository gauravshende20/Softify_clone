package com.harmonia.user.repo;

import com.harmonia.user.domain.FollowedArtist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FollowedArtistRepository extends JpaRepository<FollowedArtist, FollowedArtist.Pk> {
    boolean existsByUserIdAndArtistId(UUID userId, UUID artistId);

    void deleteByUserIdAndArtistId(UUID userId, UUID artistId);

    List<FollowedArtist> findByUserIdOrderByFollowedAtDesc(UUID userId);
}
