package com.harmonia.analytics.repo;

import com.harmonia.analytics.domain.PlayEvent;
import com.harmonia.analytics.domain.PlayEventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PlayEventRepository extends JpaRepository<PlayEvent, UUID> {

    long countByEventType(PlayEventType eventType);

    @Query("select count(distinct p.userId) from PlayEvent p")
    long countUniqueListeners();

    @Query("""
            select p.trackId as trackId, count(p) as streams
            from PlayEvent p
            where p.eventType = com.harmonia.analytics.domain.PlayEventType.PLAY_STARTED
            group by p.trackId
            order by count(p) desc
            """)
    List<TrackCountView> topTracks(Pageable pageable);

    @Query("""
            select p.artistId as artistId, count(p) as streams
            from PlayEvent p
            where p.artistId is not null
              and p.eventType = com.harmonia.analytics.domain.PlayEventType.PLAY_STARTED
            group by p.artistId
            order by count(p) desc
            """)
    List<ArtistCountView> topArtists(Pageable pageable);

    @Query("""
            select p.trackId as trackId, count(p) as streams
            from PlayEvent p
            where p.eventType = com.harmonia.analytics.domain.PlayEventType.PLAY_STARTED
              and p.occurredAt >= :from and p.occurredAt < :to
            group by p.trackId
            order by count(p) desc
            """)
    List<TrackCountView> popularTracks(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    List<PlayEvent> findTop50ByOrderByOccurredAtDesc();

    interface TrackCountView {
        UUID getTrackId();

        long getStreams();
    }

    interface ArtistCountView {
        UUID getArtistId();

        long getStreams();
    }
}
