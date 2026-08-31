package com.harmonia.catalog.web;

import com.harmonia.catalog.dto.ArtistResponse;
import com.harmonia.catalog.dto.ArtistSummary;
import com.harmonia.catalog.dto.CreateArtistRequest;
import com.harmonia.catalog.dto.PatchArtistStatusRequest;
import com.harmonia.catalog.dto.UpdateArtistRequest;
import com.harmonia.catalog.service.ArtistService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistService artists;

    public ArtistController(ArtistService artists) {
        this.artists = artists;
    }

    @GetMapping
    public PageResponse<ArtistSummary> list(@RequestParam(required = false) String q,
                                            @RequestParam(required = false) UUID genre,
                                            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return artists.search(q, genre, pageable);
    }

    @GetMapping("/{id}")
    public ArtistResponse get(@PathVariable UUID id, CurrentUser user) {
        return artists.get(id, user);
    }

    @GetMapping("/{id}/albums")
    public List<com.harmonia.catalog.dto.AlbumSummary> albums(@PathVariable UUID id, CurrentUser user) {
        return artists.albums(id, user);
    }

    @GetMapping("/{id}/tracks")
    public List<com.harmonia.catalog.dto.TrackSummary> tracks(@PathVariable UUID id, CurrentUser user) {
        return artists.tracks(id, user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ArtistResponse create(CurrentUser user, @Valid @RequestBody CreateArtistRequest request) {
        return artists.create(user, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public ArtistResponse update(@PathVariable UUID id, CurrentUser user, @Valid @RequestBody UpdateArtistRequest request) {
        return artists.update(id, user, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ArtistResponse patchStatus(@PathVariable UUID id, CurrentUser user,
                                      @Valid @RequestBody PatchArtistStatusRequest request) {
        return artists.patchStatus(id, user, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public void delete(@PathVariable UUID id, CurrentUser user) {
        artists.delete(id, user);
    }
}
