package com.harmonia.catalog.web;

import com.harmonia.catalog.dto.PatchTrackStatusRequest;
import com.harmonia.catalog.dto.StreamUrlResponse;
import com.harmonia.catalog.dto.TrackResponse;
import com.harmonia.catalog.dto.TrackSummary;
import com.harmonia.catalog.dto.TrackUploadRequest;
import com.harmonia.catalog.dto.UpdateTrackRequest;
import com.harmonia.catalog.service.TrackService;
import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.common.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracks")
public class TrackController {

    private final TrackService tracks;

    public TrackController(TrackService tracks) {
        this.tracks = tracks;
    }

    @GetMapping
    public Object list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) UUID genre,
                       @RequestParam(required = false) List<UUID> ids,
                       @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        if (ids != null && !ids.isEmpty()) {
            return tracks.byIds(ids);
        }
        return tracks.search(q, genre, pageable);
    }

    @GetMapping("/{id}")
    public TrackResponse get(@PathVariable UUID id, CurrentUser user) {
        return tracks.get(id, user);
    }

    @GetMapping("/{id}/stream")
    public StreamUrlResponse stream(@PathVariable UUID id, CurrentUser user) {
        return tracks.stream(id, user);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public TrackResponse upload(CurrentUser user,
                                @Valid @RequestPart("metadata") TrackUploadRequest metadata,
                                @RequestPart("file") MultipartFile file) {
        return tracks.upload(user, metadata, file);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public TrackResponse publish(@PathVariable UUID id, CurrentUser user) {
        return tracks.publish(id, user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public TrackResponse update(@PathVariable UUID id, CurrentUser user, @Valid @RequestBody UpdateTrackRequest request) {
        return tracks.update(id, user, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public TrackResponse patchStatus(@PathVariable UUID id, CurrentUser user,
                                     @Valid @RequestBody PatchTrackStatusRequest request) {
        return tracks.patchStatus(id, user, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ARTIST', 'ADMIN')")
    public void delete(@PathVariable UUID id, CurrentUser user) {
        tracks.delete(id, user);
    }
}
