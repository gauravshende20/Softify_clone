package com.harmonia.catalog.repo;

import com.harmonia.catalog.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, UUID> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsBySlug(String slug);

    Optional<Genre> findBySlug(String slug);

    List<Genre> findByIdIn(Collection<UUID> ids);

    List<Genre> findAllByOrderByNameAsc();
}
