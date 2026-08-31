package com.harmonia.user.repo;

import com.harmonia.user.domain.FavoriteGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FavoriteGenreRepository extends JpaRepository<FavoriteGenre, FavoriteGenre.Pk> {
    List<FavoriteGenre> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
