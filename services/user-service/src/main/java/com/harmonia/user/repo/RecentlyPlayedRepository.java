package com.harmonia.user.repo;

import com.harmonia.user.domain.RecentlyPlayed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecentlyPlayedRepository extends JpaRepository<RecentlyPlayed, UUID> {
    Page<RecentlyPlayed> findByUserIdOrderByPlayedAtDesc(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    List<RecentlyPlayed> findByUserIdOrderByPlayedAtAsc(UUID userId, Pageable pageable);
}
