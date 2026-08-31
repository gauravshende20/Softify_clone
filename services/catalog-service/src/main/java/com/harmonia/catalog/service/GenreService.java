package com.harmonia.catalog.service;

import com.harmonia.catalog.domain.Genre;
import com.harmonia.catalog.dto.CreateGenreRequest;
import com.harmonia.catalog.dto.GenreResponse;
import com.harmonia.catalog.mapper.CatalogMapper;
import com.harmonia.catalog.repo.GenreRepository;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class GenreService {

    private final GenreRepository genres;
    private final CatalogMapper mapper;

    public GenreService(GenreRepository genres, CatalogMapper mapper) {
        this.genres = genres;
        this.mapper = mapper;
    }

    @Transactional
    public GenreResponse create(CreateGenreRequest request) {
        if (genres.existsByNameIgnoreCase(request.name())) {
            throw HarmoniaException.conflict(ErrorCode.CONFLICT, "Genre already exists");
        }
        Genre genre = Genre.create(request.name());
        if (genres.existsBySlug(genre.getSlug())) {
            throw HarmoniaException.conflict(ErrorCode.CONFLICT, "Genre slug already exists");
        }
        return mapper.toGenre(genres.save(genre));
    }

    @Transactional(readOnly = true)
    public List<GenreResponse> list() {
        return genres.findAllByOrderByNameAsc().stream().map(mapper::toGenre).toList();
    }

    @Transactional(readOnly = true)
    public GenreResponse get(UUID id) {
        return mapper.toGenre(require(id));
    }

    @Transactional
    public GenreResponse update(UUID id, CreateGenreRequest request) {
        Genre genre = require(id);
        String slug = Genre.slugify(request.name());
        genres.findBySlug(slug).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw HarmoniaException.conflict(ErrorCode.CONFLICT, "Genre slug already exists");
            }
        });
        genre.rename(request.name());
        return mapper.toGenre(genre);
    }

    @Transactional
    public void delete(UUID id) {
        genres.delete(require(id));
    }

    Set<Genre> requireAll(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        List<Genre> found = genres.findByIdIn(ids);
        if (found.size() != new HashSet<>(ids).size()) {
            throw HarmoniaException.notFound(ErrorCode.GENRE_NOT_FOUND, "One or more genres were not found");
        }
        return new HashSet<>(found);
    }

    private Genre require(UUID id) {
        return genres.findById(id)
                .orElseThrow(() -> HarmoniaException.notFound(ErrorCode.GENRE_NOT_FOUND, "Genre not found"));
    }
}
