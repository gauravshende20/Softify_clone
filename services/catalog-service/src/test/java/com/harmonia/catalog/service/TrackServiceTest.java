package com.harmonia.catalog.service;

import com.harmonia.catalog.domain.Artist;
import com.harmonia.catalog.domain.Track;
import com.harmonia.catalog.domain.TrackStatus;
import com.harmonia.catalog.dto.TrackUploadRequest;
import com.harmonia.catalog.mapper.CatalogMapper;
import com.harmonia.catalog.repo.TrackRepository;
import com.harmonia.catalog.storage.StoragePort;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.security.Roles;
import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.common.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock TrackRepository tracks;
    @Mock ArtistService artistService;
    @Mock AlbumService albumService;
    @Mock GenreService genreService;
    @Mock StoragePort storage;
    @Mock DomainEventPublisher events;

    TrackService service;

    @BeforeEach
    void setUp() {
        service = new TrackService(tracks, artistService, albumService, genreService, storage,
                Mappers.getMapper(CatalogMapper.class), events);
    }

    @Test
    void sanitizeStripsPaths() {
        assertEquals("song.mp3", TrackService.sanitize("../song.mp3"));
        assertEquals("audio.bin", TrackService.sanitize("***"));
    }

    @Test
    void uploadStoresDraftAndPublishesUploaded() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        CurrentUser user = new CurrentUser(userId, "artist@harmonia.local", Set.of(Roles.ARTIST));
        Artist artist = Artist.create("Luna", "bio", null, userId, Set.of());
        when(artistService.require(artistId)).thenReturn(artist);
        when(genreService.requireAll(any())).thenReturn(Set.of());
        doNothing().when(storage).putAudio(anyString(), any(), anyLong(), anyString());
        when(tracks.save(any(Track.class))).thenAnswer(inv -> inv.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "demo.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        TrackUploadRequest request = new TrackUploadRequest("Sea Glass", artistId, null, 180000, false, 1, null);
        var response = service.upload(user, request, file);

        assertEquals("Sea Glass", response.title());
        assertEquals(TrackStatus.DRAFT, response.status());
        verify(storage).putAudio(anyString(), any(), eq(3L), eq("audio/mpeg"));
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(events).publish(eq(Topics.CATALOG), captor.capture());
        assertEquals(EventType.TRACK_UPLOADED.name(), captor.getValue().eventType());
    }

    @Test
    void publishRejectsNonOwner() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        Artist artist = Artist.create("Luna", "bio", null, owner, Set.of());
        Track track = Track.create(UUID.randomUUID(), artist, null, "Song", 1000, "k", "audio/mpeg", 1, false, 1, Set.of());
        when(tracks.findById(track.getId())).thenReturn(Optional.of(track));
        CurrentUser user = new CurrentUser(other, "x@y.z", Set.of(Roles.ARTIST));
        HarmoniaException ex = assertThrows(HarmoniaException.class, () -> service.publish(track.getId(), user));
        assertEquals(403, ex.getStatus());
    }

    @Test
    void publishEmitsTrackPublished() {
        UUID owner = UUID.randomUUID();
        Artist artist = Artist.create("Luna", "bio", null, owner, Set.of());
        Track track = Track.create(UUID.randomUUID(), artist, null, "Song", 1000, "k", "audio/mpeg", 1, false, 1, Set.of());
        when(tracks.findById(track.getId())).thenReturn(Optional.of(track));
        CurrentUser user = new CurrentUser(owner, "a@b.c", Set.of(Roles.ARTIST));
        service.publish(track.getId(), user);
        assertEquals(TrackStatus.PUBLISHED, track.getStatus());
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(events).publish(eq(Topics.CATALOG), captor.capture());
        assertEquals(EventType.TRACK_PUBLISHED.name(), captor.getValue().eventType());
    }
}
