package com.harmonia.playlist.web;

import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.common.security.CurrentUser;
import com.harmonia.playlist.dto.AddTrackRequest;
import com.harmonia.playlist.dto.CreatePlaylistRequest;
import com.harmonia.playlist.dto.PlaylistResponse;
import com.harmonia.playlist.dto.PlaylistSummary;
import com.harmonia.playlist.dto.ReorderTracksRequest;
import com.harmonia.playlist.dto.UpdatePlaylistRequest;
import com.harmonia.playlist.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/playlists")
public class PlaylistController {

    private final PlaylistService playlists;

    public PlaylistController(PlaylistService playlists) {
        this.playlists = playlists;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlaylistResponse create(CurrentUser user, @Valid @RequestBody CreatePlaylistRequest request) {
        return playlists.create(user.id(), request);
    }

    @GetMapping
    public List<PlaylistSummary> mine(CurrentUser user) {
        return playlists.mine(user.id());
    }

    @GetMapping("/me")
    public List<PlaylistSummary> mineAlias(CurrentUser user) {
        return playlists.mine(user.id());
    }

    @GetMapping("/public")
    public PageResponse<PlaylistSummary> browsePublic(@PageableDefault(size = 20) Pageable pageable) {
        return playlists.publicPlaylists(pageable);
    }

    @GetMapping("/{id}")
    public PlaylistResponse get(@PathVariable UUID id, CurrentUser user) {
        return playlists.get(id, user == null ? null : user.id());
    }

    @PutMapping("/{id}")
    public PlaylistResponse update(@PathVariable UUID id, CurrentUser user, @Valid @RequestBody UpdatePlaylistRequest request) {
        return playlists.update(id, user.id(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, CurrentUser user) {
        playlists.delete(id, user.id());
    }

    @PostMapping("/{id}/tracks")
    public PlaylistResponse addTrack(@PathVariable UUID id, CurrentUser user, @Valid @RequestBody AddTrackRequest request) {
        return playlists.addTrack(id, user.id(), request);
    }

    @DeleteMapping("/{id}/tracks/{trackId}")
    public PlaylistResponse removeTrack(@PathVariable UUID id, @PathVariable UUID trackId, CurrentUser user) {
        return playlists.removeTrack(id, user.id(), trackId);
    }

    @PutMapping("/{id}/tracks/reorder")
    public PlaylistResponse reorder(@PathVariable UUID id, CurrentUser user, @Valid @RequestBody ReorderTracksRequest request) {
        return playlists.reorder(id, user.id(), request);
    }
}
