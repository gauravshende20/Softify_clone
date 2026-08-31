package com.harmonia.catalog.repo;

import com.harmonia.catalog.domain.Artist;
import com.harmonia.catalog.domain.ArtistStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ArtistRepository extends JpaRepository<Artist, UUID> {

    @Query(value = """
            select distinct a from Artist a
            left join a.genres g
            where a.status = :status
              and (:q is null or lower(a.name) like lower(concat('%', :q, '%')))
              and (:genreId is null or g.id = :genreId)
            """,
            countQuery = """
            select count(distinct a.id) from Artist a
            left join a.genres g
            where a.status = :status
              and (:q is null or lower(a.name) like lower(concat('%', :q, '%')))
              and (:genreId is null or g.id = :genreId)
            """)
    Page<Artist> search(@Param("q") String q,
                        @Param("genreId") UUID genreId,
                        @Param("status") ArtistStatus status,
                        Pageable pageable);
}
