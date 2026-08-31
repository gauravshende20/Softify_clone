package com.harmonia.playlist.service;

import com.harmonia.common.api.error.ErrorCode;
import com.harmonia.common.api.error.HarmoniaException;
import com.harmonia.common.api.paging.PageResponse;
import com.harmonia.common.kafka.DomainEvent;
import com.harmonia.common.kafka.DomainEventPublisher;
import com.harmonia.common.kafka.EventType;
import com.harmonia.common.kafka.Topics;
import com.harmonia.playlist.domain.CollaboratorRole;
import com.harmonia.playlist.domain.Playlist;
import com.harmonia.playlist.domain.PlaylistTrack;
import com.harmonia.playlist.domain.Visibility;
import com.harmonia.playlist.dto.AddTrackRequest;
import com.harmonia.playlist.dto.CreatePlaylistRequest;
import com.harmonia.playlist.dto.PlaylistResponse;
import com.harmonia.playlist.dto.PlaylistSummary;
import com.harmonia.playlist.dto.ReorderTracksRequest;
import com.harmonia.playlist.dto.UpdatePlaylistRequest;
import com.harmonia.playlist.mapper.PlaylistMapper;
import com.harmonia.playlist.repo.PlaylistCollaboratorRepository;
import com.harmonia.playlist.repo.PlaylistRepository;
import com.harmonia.playlist.repo.PlaylistTrackRepository;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private static final String PRODUCER = "playlist-service";

    private final PlaylistRepository playlists;
    private final PlaylistTrackRepository playlistTracks;
    private final PlaylistCollaboratorRepository collaborators;
    private final PlaylistMapper mapper;
    private final DomainEventPublisher events;

    public PlaylistService(PlaylistRepository playlists,
                           PlaylistTrackRepository playlistTracks,
                           PlaylistCollaboratorRepository collaborators,
                           PlaylistMapper mapper,
                           DomainEventPublisher events) {
        this.playlists = playlists;
        this.playlistTracks = playlistTracks;
        this.collaborators = collaborators;
        this.mapper = mapper;
        this.events = events;
    }

    @Transactional
    public PlaylistResponse create(UUID userId, CreatePlaylistRequest request) {
        Playlist playlist = Playlist.create(
                userId,
                request.name(),
                request.description(),
                request.coverKey(),
                request.visibility(),
                Boolean.TRUE.equals(request.collaborative()));
        playlists.save(playlist);
        publish(EventType.PLAYLIST_CREATED, playlist, userId, Map.of("name", playlist.getName()));
        return toResponse(playlist);
    }

    @Transactional(readOnly = true)
    public List<PlaylistSummary> mine(UUID userId) {
        return playlists.findByOwnerIdOrderByUpdatedAtDesc(userId).stream().map(mapper::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<PlaylistSummary> publicPlaylists(Pageable pageable) {
        Page<Playlist> page = playlists.findByVisibilityOrderByUpdatedAtDesc(Visibility.PUBLIC, pageable);
        return PageResponse.of(
                page.getContent().stream().map(mapper::toSummary).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PlaylistResponse get(UUID playlistId, UUID userId) {
        return toResponse(requireView(playlistId, userId));
    }

    @Transactional
    public PlaylistResponse update(UUID playlistId, UUID userId, UpdatePlaylistRequest request) {
        Playlist playlist = requireOwner(playlistId, userId);
        playlist.update(request.name(), request.description(), request.coverKey(), request.visibility(), request.collaborative());
        publish(EventType.PLAYLIST_UPDATED, playlist, userId, Map.of("name", playlist.getName()));
        return toResponse(playlist);
    }

    @Transactional
    public void delete(UUID playlistId, UUID userId) {
        Playlist playlist = requireOwner(playlistId, userId);
        playlistTracks.deleteByPlaylistId(playlistId);
        playlists.delete(playlist);
        publish(EventType.PLAYLIST_DELETED, playlist, userId, Map.of("name", playlist.getName()));
    }

    @Transactional
    public PlaylistResponse addTrack(UUID playlistId, UUID userId, AddTrackRequest request) {
        Playlist playlist = requireCanEdit(playlistId, userId);
        if (playlistTracks.existsByPlaylistIdAndTrackId(playlistId, request.trackId())) {
            throw HarmoniaException.conflict(ErrorCode.TRACK_ALREADY_IN_PLAYLIST, "Track is already in this playlist");
        }
        List<PlaylistTrack> current = new ArrayList<>(playlistTracks.findByPlaylistIdOrderByPositionAsc(playlistId));
        int insertAt = request.position() == null ? current.size() : request.position();
        if (insertAt < 0 || insertAt > current.size()) {
            throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "Position is out of range");
        }
        PlaylistTrack created = PlaylistTrack.create(playlistId, request.trackId(), -1, userId);
        playlistTracks.saveAndFlush(created);
        current.add(insertAt, created);
        resequence(current);
        playlist.touch();
        publish(EventType.PLAYLIST_UPDATED, playlist, userId, Map.of("action", "TRACK_ADDED", "trackId", request.trackId().toString()));
        return toResponse(playlist);
    }

    @Transactional
    public PlaylistResponse removeTrack(UUID playlistId, UUID userId, UUID trackId) {
        Playlist playlist = requireCanEdit(playlistId, userId);
        if (!playlistTracks.existsByPlaylistIdAndTrackId(playlistId, trackId)) {
            throw HarmoniaException.notFound(ErrorCode.TRACK_NOT_FOUND, "Track is not in this playlist");
        }
        playlistTracks.deleteByPlaylistIdAndTrackId(playlistId, trackId);
        playlistTracks.flush();
        resequence(playlistTracks.findByPlaylistIdOrderByPositionAsc(playlistId));
        playlist.touch();
        publish(EventType.PLAYLIST_UPDATED, playlist, userId, Map.of("action", "TRACK_REMOVED", "trackId", trackId.toString()));
        return toResponse(playlist);
    }

    @Transactional
    public PlaylistResponse reorder(UUID playlistId, UUID userId, ReorderTracksRequest request) {
        Playlist playlist = requireCanEdit(playlistId, userId);
        List<PlaylistTrack> current = playlistTracks.findByPlaylistIdOrderByPositionAsc(playlistId);
        List<UUID> requested = request.trackIds();
        Set<UUID> existingIds = current.stream().map(PlaylistTrack::getTrackId).collect(Collectors.toSet());
        Set<UUID> requestedIds = new HashSet<>(requested);
        if (requested.size() != current.size() || !existingIds.equals(requestedIds) || requestedIds.size() != requested.size()) {
            throw HarmoniaException.badRequest(ErrorCode.BAD_REQUEST, "trackIds must contain each playlist track exactly once");
        }
        Map<UUID, PlaylistTrack> byId = current.stream()
                .collect(Collectors.toMap(PlaylistTrack::getTrackId, Function.identity()));
        List<PlaylistTrack> reordered = requested.stream().map(byId::get).toList();
        resequence(reordered);
        playlist.touch();
        publish(EventType.PLAYLIST_UPDATED, playlist, userId, Map.of("action", "REORDERED"));
        return toResponse(playlist);
    }

    void resequence(List<PlaylistTrack> tracks) {
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).setPosition(-1 - i);
        }
        playlistTracks.flush();
        for (int i = 0; i < tracks.size(); i++) {
            tracks.get(i).setPosition(i);
        }
    }

    private Playlist requirePlaylist(UUID playlistId) {
        return playlists.findById(playlistId)
                .orElseThrow(() -> HarmoniaException.notFound(ErrorCode.PLAYLIST_NOT_FOUND, "Playlist not found"));
    }

    private Playlist requireOwner(UUID playlistId, UUID userId) {
        Playlist playlist = requirePlaylist(playlistId);
        if (!playlist.getOwnerId().equals(userId)) {
            throw HarmoniaException.forbidden(ErrorCode.PLAYLIST_NOT_OWNED, "You do not own this playlist");
        }
        return playlist;
    }

    private Playlist requireCanEdit(UUID playlistId, UUID userId) {
        Playlist playlist = requirePlaylist(playlistId);
        if (playlist.getOwnerId().equals(userId)) {
            return playlist;
        }
        if (playlist.isCollaborative()
                && collaborators.existsByPlaylistIdAndUserIdAndRole(playlistId, userId, CollaboratorRole.EDITOR)) {
            return playlist;
        }
        throw HarmoniaException.forbidden(ErrorCode.PLAYLIST_NOT_OWNED, "You cannot edit this playlist");
    }

    private Playlist requireView(UUID playlistId, UUID userId) {
        Playlist playlist = requirePlaylist(playlistId);
        if (playlist.getVisibility() == Visibility.PUBLIC) {
            return playlist;
        }
        if (userId != null && playlist.getOwnerId().equals(userId)) {
            return playlist;
        }
        if (userId != null && collaborators.existsByPlaylistIdAndUserId(playlistId, userId)) {
            return playlist;
        }
        throw HarmoniaException.forbidden(ErrorCode.FORBIDDEN, "Playlist is private");
    }

    private PlaylistResponse toResponse(Playlist playlist) {
        List<PlaylistTrack> tracks = playlistTracks.findByPlaylistIdOrderByPositionAsc(playlist.getId());
        return mapper.toResponse(playlist, tracks.stream().map(mapper::toTrack).toList());
    }

    private void publish(EventType type, Playlist playlist, UUID userId, Map<String, Object> payload) {
        events.publish(Topics.PLAYLIST, DomainEvent.of(
                type, "Playlist", playlist.getId().toString(),
                PRODUCER, MDC.get("traceId"), userId.toString(), payload));
    }
}
