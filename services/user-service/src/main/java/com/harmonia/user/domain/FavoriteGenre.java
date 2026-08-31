package com.harmonia.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "favorite_genres")
@IdClass(FavoriteGenre.Pk.class)
public class FavoriteGenre {

    @Id
    @Column(name = "user_id", columnDefinition = "char(36)")
    private UUID userId;

    @Id
    @Column(name = "genre_id", columnDefinition = "char(36)")
    private UUID genreId;

    protected FavoriteGenre() {
    }

    public FavoriteGenre(UUID userId, UUID genreId) {
        this.userId = userId;
        this.genreId = genreId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getGenreId() {
        return genreId;
    }

    public static class Pk implements Serializable {
        private UUID userId;
        private UUID genreId;

        public Pk() {
        }

        public Pk(UUID userId, UUID genreId) {
            this.userId = userId;
            this.genreId = genreId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pk pk)) {
                return false;
            }
            return Objects.equals(userId, pk.userId) && Objects.equals(genreId, pk.genreId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, genreId);
        }
    }
}
