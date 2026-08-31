package com.harmonia.user.repo;

import com.harmonia.user.domain.LikedTrack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LikedTrackRepository extends JpaRepository<LikedTrack, LikedTrack.Pk> {
    boolean existsByUserIdAndTrackId(UUID userId, UUID trackId);

    void deleteByUserIdAndTrackId(UUID userId, UUID trackId);

    Page<LikedTrack> findByUserIdOrderByLikedAtDesc(UUID userId, Pageable pageable);
}
