package com.harmonia.catalog.web;

import com.harmonia.catalog.dto.AlbumResponse;
import com.harmonia.catalog.dto.AlbumSummary;
import com.harmonia.catalog.dto.CreateAlbumRequest;
import com.harmonia.catalog.dto.PatchAlbumStatusRequest;
import com.harmonia.catalog.dto.UpdateAlbumRequest;
import com.harmonia.catalog.service.AlbumService;
import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumService albums;

    public AlbumController(AlbumService albums) {
        this.albums = albums;
    }

    @GetMapping
    public PageResponse<AlbumSummary> list(@RequestParam(required = false) String q,
                                           @RequestParam(required = false) UUID genre,
                                           @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return albums.search(q, genre, pageable);
    }

    @GetMapping("/{id}")
    public AlbumResponse get(@PathVariable UUID id, CurrentUser user) {
        return albums.get(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public AlbumResponse create(CurrentUser user, @Valid @RequestBody CreateAlbumRequest request) {
        return albums.create(user, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public AlbumResponse update(@PathVariable UUID id, CurrentUser user, @Valid @RequestBody UpdateAlbumRequest request) {
        return albums.update(id, user, request);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public AlbumResponse publish(@PathVariable UUID id, CurrentUser user) {
        return albums.publish(id, user);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public AlbumResponse patchStatus(@PathVariable UUID id, CurrentUser user,
                                     @Valid @RequestBody PatchAlbumStatusRequest request) {
        return albums.patchStatus(id, user, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public void delete(@PathVariable UUID id, CurrentUser user) {
        albums.delete(id, user);
    }
}
