package com.harmonia.playlist.service;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.common.kafka.EventType;
import com.harmonia.playlist.domain.Playlist;
import com.harmonia.playlist.domain.PlaylistTrack;
import com.harmonia.playlist.domain.Visibility;
import com.harmonia.playlist.dto.AddTrackRequest;
import com.harmonia.playlist.dto.CreatePlaylistRequest;
import com.harmonia.playlist.dto.ReorderTracksRequest;
import com.harmonia.playlist.dto.UpdatePlaylistRequest;
import com.harmonia.playlist.mapper.PlaylistMapper;
import com.harmonia.playlist.repo.PlaylistCollaboratorRepository;
import com.harmonia.playlist.repo.PlaylistRepository;
import com.harmonia.playlist.repo.PlaylistTrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceTest {

    @Mock PlaylistRepository playlists;
    @Mock PlaylistTrackRepository playlistTracks;
    @Mock PlaylistCollaboratorRepository collaborators;
    @Mock DomainEventPublisher events;

    PlaylistService service;

    @BeforeEach
    void setUp() {
        service = new PlaylistService(playlists, playlistTracks, collaborators,
                Mappers.getMapper(PlaylistMapper.class), events);
    }

    @Test
    void createPublishesCreated() {
        UUID owner = UUID.randomUUID();
        when(playlists.save(any(Playlist.class))).thenAnswer(inv -> inv.getArgument(0));
        when(playlistTracks.findByPlaylistIdOrderByPositionAsc(any())).thenReturn(List.of());
        service.create(owner, new CreatePlaylistRequest("Chill", "desc", null, Visibility.PRIVATE, false));
        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(events).publish(any(), captor.capture());
        assertEquals(EventType.PLAYLIST_CREATED.name(), captor.getValue().eventType());
    }

    @Test
    void nonOwnerCannotUpdate() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        Playlist playlist = Playlist.create(owner, "Mine", null, null, Visibility.PRIVATE, false);
        when(playlists.findById(playlist.getId())).thenReturn(Optional.of(playlist));
        HarmoniaException ex = assertThrows(HarmoniaException.class, () -> service.update(
                playlist.getId(), other, new UpdatePlaylistRequest("Stolen", null, null, Visibility.PUBLIC, false)));
        assertEquals(ErrorCode.PLAYLIST_NOT_OWNED, ex.getCode());
        assertEquals(403, ex.getStatus());
    }

    @Test
    void privatePlaylistHiddenFromOthers() {
        UUID owner = UUID.randomUUID();
        Playlist playlist = Playlist.create(owner, "Secret", null, null, Visibility.PRIVATE, false);
        when(playlists.findById(playlist.getId())).thenReturn(Optional.of(playlist));
        HarmoniaException ex = assertThrows(HarmoniaException.class,
                () -> service.get(playlist.getId(), UUID.randomUUID()));
        assertEquals(403, ex.getStatus());
    }

    @Test
    void addTrackAppendsAndResequences() {
        UUID owner = UUID.randomUUID();
        Playlist playlist = Playlist.create(owner, "Mix", null, null, Visibility.PRIVATE, false);
        UUID existing = UUID.randomUUID();
        UUID incoming = UUID.randomUUID();
        PlaylistTrack first = PlaylistTrack.create(playlist.getId(), existing, 0, owner);
        when(playlists.findById(playlist.getId())).thenReturn(Optional.of(playlist));
        when(playlistTracks.existsByPlaylistIdAndTrackId(playlist.getId(), incoming)).thenReturn(false);
        when(playlistTracks.findByPlaylistIdOrderByPositionAsc(playlist.getId()))
                .thenReturn(new ArrayList<>(List.of(first)), List.of(first, PlaylistTrack.create(playlist.getId(), incoming, 1, owner)));
        when(playlistTracks.saveAndFlush(any(PlaylistTrack.class))).thenAnswer(inv -> inv.getArgument(0));

        service.addTrack(playlist.getId(), owner, new AddTrackRequest(incoming, null));
        assertEquals(0, first.getPosition());
    }

    @Test
    void addDuplicateTrackConflicts() {
        UUID owner = UUID.randomUUID();
        Playlist playlist = Playlist.create(owner, "Mix", null, null, Visibility.PRIVATE, false);
        UUID trackId = UUID.randomUUID();
        when(playlists.findById(playlist.getId())).thenReturn(Optional.of(playlist));
        when(playlistTracks.existsByPlaylistIdAndTrackId(playlist.getId(), trackId)).thenReturn(true);
        HarmoniaException ex = assertThrows(HarmoniaException.class,
                () -> service.addTrack(playlist.getId(), owner, new AddTrackRequest(trackId, null)));
        assertEquals(ErrorCode.TRACK_ALREADY_IN_PLAYLIST, ex.getCode());
    }

    @Test
    void removeTrackCompactsPositions() {
        UUID owner = UUID.randomUUID();
        Playlist playlist = Playlist.create(owner, "Mix", null, null, Visibility.PRIVATE, false);
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        PlaylistTrack t0 = PlaylistTrack.create(playlist.getId(), a, 0, owner);
        PlaylistTrack t1 = PlaylistTrack.create(playlist.getId(), b, 1, owner);
        PlaylistTrack t2 = PlaylistTrack.create(playlist.getId(), c, 2, owner);
        when(playlists.findById(playlist.getId())).thenReturn(Optional.of(playlist));
        when(playlistTracks.existsByPlaylistIdAndTrackId(playlist.getId(), b)).thenReturn(true);
        when(playlistTracks.findByPlaylistIdOrderByPositionAsc(playlist.getId()))
                .thenReturn(List.of(t0, t2), List.of(t0, t2));

        service.removeTrack(playlist.getId(), owner, b);
        verify(playlistTracks).deleteByPlaylistIdAndTrackId(playlist.getId(), b);
        assertEquals(0, t0.getPosition());
        assertEquals(1, t2.getPosition());
    }

    @Test
    void reorderAssignsContiguousPositions() {
        UUID owner = UUID.randomUUID();
        Playlist playlist = Playlist.create(owner, "Mix", null, null, Visibility.PRIVATE, false);
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        PlaylistTrack t0 = PlaylistTrack.create(playlist.getId(), a, 0, owner);
        PlaylistTrack t1 = PlaylistTrack.create(playlist.getId(), b, 1, owner);
        PlaylistTrack t2 = PlaylistTrack.create(playlist.getId(), c, 2, owner);
        when(playlists.findById(playlist.getId())).thenReturn(Optional.of(playlist));
        when(playlistTracks.findByPlaylistIdOrderByPositionAsc(playlist.getId())).thenReturn(List.of(t0, t1, t2));

        service.reorder(playlist.getId(), owner, new ReorderTracksRequest(List.of(c, a, b)));
        assertEquals(0, t2.getPosition());
        assertEquals(1, t0.getPosition());
        assertEquals(2, t1.getPosition());
    }

    @Test
    void reorderRejectsMismatchedIds() {
        UUID owner = UUID.randomUUID();
        Playlist playlist = Playlist.create(owner, "Mix", null, null, Visibility.PRIVATE, false);
        UUID a = UUID.randomUUID();
        PlaylistTrack t0 = PlaylistTrack.create(playlist.getId(), a, 0, owner);
        when(playlists.findById(playlist.getId())).thenReturn(Optional.of(playlist));
        when(playlistTracks.findByPlaylistIdOrderByPositionAsc(playlist.getId())).thenReturn(List.of(t0));
        HarmoniaException ex = assertThrows(HarmoniaException.class,
                () -> service.reorder(playlist.getId(), owner, new ReorderTracksRequest(List.of(UUID.randomUUID()))));
        assertEquals(400, ex.getStatus());
        verify(events, never()).publish(any(), any());
    }

    @Test
    void nonOwnerCannotReorder() {
        UUID owner = UUID.randomUUID();
        Playlist playlist = Playlist.create(owner, "Mix", null, null, Visibility.PRIVATE, false);
        when(playlists.findById(playlist.getId())).thenReturn(Optional.of(playlist));
        HarmoniaException ex = assertThrows(HarmoniaException.class, () -> service.reorder(
                playlist.getId(), UUID.randomUUID(), new ReorderTracksRequest(List.of(UUID.randomUUID()))));
        assertEquals(ErrorCode.PLAYLIST_NOT_OWNED, ex.getCode());
    }
}
