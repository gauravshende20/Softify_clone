package com.harmonia.playlist.mapper;

import com.harmonia.playlist.domain.Playlist;
import com.harmonia.playlist.domain.PlaylistTrack;
import com.harmonia.playlist.dto.PlaylistResponse;
import com.harmonia.playlist.dto.PlaylistSummary;
import com.harmonia.playlist.dto.PlaylistTrackResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlaylistMapper {

    PlaylistSummary toSummary(Playlist playlist);

    PlaylistTrackResponse toTrack(PlaylistTrack track);

    @Mapping(target = "tracks", source = "tracks")
    PlaylistResponse toResponse(Playlist playlist, List<PlaylistTrackResponse> tracks);
}
