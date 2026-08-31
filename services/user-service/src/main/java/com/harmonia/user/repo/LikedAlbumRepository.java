package com.harmonia.user.repo;

import com.harmonia.user.domain.LikedAlbum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedAlbumRepository extends JpaRepository<LikedAlbum, LikedAlbum.Pk> {
}
