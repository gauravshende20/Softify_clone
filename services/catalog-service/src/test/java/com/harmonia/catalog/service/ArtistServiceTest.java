package com.harmonia.catalog.service;

import com.harmonia.catalog.domain.Artist;
import com.harmonia.catalog.domain.ArtistStatus;
import com.harmonia.catalog.dto.CreateArtistRequest;
import com.harmonia.catalog.dto.PatchArtistStatusRequest;
import com.harmonia.catalog.mapper.CatalogMapper;
import com.harmonia.catalog.repo.AlbumRepository;
import com.harmonia.catalog.repo.ArtistRepository;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.security.Roles;
import com.harmonia.common.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock ArtistRepository artists;
    @Mock AlbumRepository albums;
    @Mock com.harmonia.catalog.repo.TrackRepository tracks;
    @Mock GenreService genreService;

    ArtistService service;

    @BeforeEach
    void setUp() {
        service = new ArtistService(artists, albums, tracks, genreService, Mappers.getMapper(CatalogMapper.class));
    }

    @Test
    void createSetsOwnerAndActive() {
        UUID userId = UUID.randomUUID();
        CurrentUser user = new CurrentUser(userId, "a@b.c", Set.of(Roles.ARTIST));
        when(genreService.requireAll(any())).thenReturn(Set.of());
        when(artists.save(any(Artist.class))).thenAnswer(inv -> inv.getArgument(0));
        var response = service.create(user, new CreateArtistRequest("Luna Waves", "bio", null, List.of()));
        assertEquals("Luna Waves", response.name());
        assertEquals(ArtistStatus.ACTIVE, response.status());
    }

    @Test
    void hiddenArtistIsNotFoundForPublic() {
        UUID owner = UUID.randomUUID();
        Artist artist = Artist.create("Luna", "bio", null, owner, Set.of());
        artist.setStatus(ArtistStatus.HIDDEN);
        when(artists.findById(artist.getId())).thenReturn(Optional.of(artist));
        HarmoniaException ex = assertThrows(HarmoniaException.class, () -> service.get(artist.getId(), null));
        assertEquals(404, ex.getStatus());
    }

    @Test
    void moderatorCanHideArtist() {
        UUID owner = UUID.randomUUID();
        Artist artist = Artist.create("Luna", "bio", null, owner, Set.of());
        when(artists.findById(artist.getId())).thenReturn(Optional.of(artist));
        CurrentUser mod = new CurrentUser(UUID.randomUUID(), "mod@harmonia.local", Set.of(Roles.MODERATOR));
        service.patchStatus(artist.getId(), mod, new PatchArtistStatusRequest(ArtistStatus.HIDDEN));
        assertEquals(ArtistStatus.HIDDEN, artist.getStatus());
    }
}
