package com.harmonia.catalog.service;

import com.harmonia.catalog.domain.Album;
import com.harmonia.catalog.domain.AlbumStatus;
import com.harmonia.catalog.domain.Artist;
import com.harmonia.catalog.domain.ArtistStatus;
import com.harmonia.catalog.dto.AlbumSummary;
import com.harmonia.catalog.dto.ArtistResponse;
import com.harmonia.catalog.dto.ArtistSummary;
import com.harmonia.catalog.dto.CreateArtistRequest;
import com.harmonia.catalog.dto.PatchArtistStatusRequest;
import com.harmonia.catalog.dto.UpdateArtistRequest;
import com.harmonia.catalog.mapper.CatalogMapper;
import com.harmonia.catalog.repo.AlbumRepository;
import com.harmonia.catalog.repo.ArtistRepository;
import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.common.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ArtistService {

    private final ArtistRepository artists;
    private final AlbumRepository albums;
    private final GenreService genreService;
    private final CatalogMapper mapper;

    public ArtistService(ArtistRepository artists,
                         AlbumRepository albums,
                         GenreService genreService,
                         CatalogMapper mapper) {
        this.artists = artists;
        this.albums = albums;
        this.genreService = genreService;
        this.mapper = mapper;
    }

    @Transactional
    public ArtistResponse create(CurrentUser user, CreateArtistRequest request) {
        Artist artist = Artist.create(
                request.name(), request.bio(), request.imageKey(), user.id(),
                genreService.requireAll(request.genreIds()));
        artists.save(artist);
        return mapper.toArtist(artist, List.of());
    }

    @Transactional(readOnly = true)
    public PageResponse<ArtistSummary> search(String q, UUID genreId, Pageable pageable) {
        Page<Artist> page = artists.search(blankToNull(q), genreId, ArtistStatus.ACTIVE, pageable);
        return PageResponse.of(
                page.getContent().stream().map(mapper::toArtistSummary).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ArtistResponse get(UUID id, CurrentUser user) {
        Artist artist = require(id);
        if (artist.getStatus() == ArtistStatus.HIDDEN && !CatalogAccess.canManage(user, artist) && !CatalogAccess.isStaff(user)) {
            throw HarmoniaException.notFound(ErrorCode.ARTIST_NOT_FOUND, "Artist not found");
        }
        List<Album> artistAlbums = albums.findByArtistIdOrderByReleaseDateDesc(id);
        boolean privileged = CatalogAccess.canManage(user, artist) || CatalogAccess.isStaff(user);
        List<AlbumSummary> summaries = artistAlbums.stream()
                .filter(album -> privileged || album.getStatus() == AlbumStatus.PUBLISHED)
                .map(mapper::toAlbumSummary)
                .toList();
        return mapper.toArtist(artist, summaries);
    }

    @Transactional
    public ArtistResponse update(UUID id, CurrentUser user, UpdateArtistRequest request) {
        Artist artist = require(id);
        CatalogAccess.requireManage(user, artist);
        artist.update(request.name(), request.bio(), request.imageKey(), genreService.requireAll(request.genreIds()));
        return mapper.toArtist(artist, List.of());
    }

    @Transactional
    public ArtistResponse patchStatus(UUID id, CurrentUser user, PatchArtistStatusRequest request) {
        CatalogAccess.requireStaff(user);
        Artist artist = require(id);
        artist.setStatus(request.status());
        return mapper.toArtist(artist, List.of());
    }

    @Transactional
    public void delete(UUID id, CurrentUser user) {
        Artist artist = require(id);
        CatalogAccess.requireManage(user, artist);
        artists.delete(artist);
    }

    Artist require(UUID id) {
        return artists.findById(id)
                .orElseThrow(() -> HarmoniaException.notFound(ErrorCode.ARTIST_NOT_FOUND, "Artist not found"));
    }

    private static String blankToNull(String q) {
        return q == null || q.isBlank() ? null : q.trim();
    }
}
