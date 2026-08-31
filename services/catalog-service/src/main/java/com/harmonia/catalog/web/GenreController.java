package com.harmonia.catalog.web;

import com.harmonia.catalog.dto.CreateGenreRequest;
import com.harmonia.catalog.dto.GenreResponse;
import com.harmonia.catalog.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/genres")
public class GenreController {

    private final GenreService genres;

    public GenreController(GenreService genres) {
        this.genres = genres;
    }

    @GetMapping
    public List<GenreResponse> list() {
        return genres.list();
    }

    @GetMapping("/{id}")
    public GenreResponse get(@PathVariable UUID id) {
        return genres.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public GenreResponse create(@Valid @RequestBody CreateGenreRequest request) {
        return genres.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public GenreResponse update(@PathVariable UUID id, @Valid @RequestBody CreateGenreRequest request) {
        return genres.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        genres.delete(id);
    }
}
