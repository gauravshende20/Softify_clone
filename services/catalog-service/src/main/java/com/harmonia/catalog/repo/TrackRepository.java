package com.harmonia.catalog.repo;

import com.harmonia.catalog.domain.Track;
import com.harmonia.catalog.domain.TrackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TrackRepository extends JpaRepository<Track, UUID> {

    List<Track> findByAlbumIdOrderByTrackNumberAsc(UUID albumId);

    List<Track> findByIdIn(List<UUID> ids);

    @Query(value = """
            select distinct t from Track t
            left join t.genres g
            where t.status = :status
              and (:q is null or lower(t.title) like lower(concat('%', :q, '%')))
              and (:genreId is null or g.id = :genreId)
            """,
            countQuery = """
            select count(distinct t.id) from Track t
            left join t.genres g
            where t.status = :status
              and (:q is null or lower(t.title) like lower(concat('%', :q, '%')))
              and (:genreId is null or g.id = :genreId)
            """)
    Page<Track> search(@Param("q") String q,
                       @Param("genreId") UUID genreId,
                       @Param("status") TrackStatus status,
                       Pageable pageable);
}
