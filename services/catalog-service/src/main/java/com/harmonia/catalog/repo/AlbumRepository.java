package com.harmonia.catalog.repo;

import com.harmonia.catalog.domain.Album;
import com.harmonia.catalog.domain.AlbumStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AlbumRepository extends JpaRepository<Album, UUID> {

    List<Album> findByArtistIdOrderByReleaseDateDesc(UUID artistId);

    @Query(value = """
            select distinct a from Album a
            join a.artist artist
            left join artist.genres g
            where a.status = :status
              and (:q is null or lower(a.title) like lower(concat('%', :q, '%')))
              and (:genreId is null or g.id = :genreId)
            """,
            countQuery = """
            select count(distinct a.id) from Album a
            join a.artist artist
            left join artist.genres g
            where a.status = :status
              and (:q is null or lower(a.title) like lower(concat('%', :q, '%')))
              and (:genreId is null or g.id = :genreId)
            """)
    Page<Album> search(@Param("q") String q,
                       @Param("genreId") UUID genreId,
                       @Param("status") AlbumStatus status,
                       Pageable pageable);
}
