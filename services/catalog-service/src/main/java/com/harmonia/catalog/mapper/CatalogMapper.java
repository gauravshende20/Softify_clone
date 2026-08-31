package com.harmonia.catalog.mapper;

import com.harmonia.catalog.domain.Album;
import com.harmonia.catalog.domain.Artist;
import com.harmonia.catalog.domain.Genre;
import com.harmonia.catalog.domain.Track;
import com.harmonia.catalog.dto.AlbumResponse;
import com.harmonia.catalog.dto.AlbumSummary;
import com.harmonia.catalog.dto.ArtistResponse;
import com.harmonia.catalog.dto.ArtistSummary;
import com.harmonia.catalog.dto.GenreResponse;
import com.harmonia.catalog.dto.TrackResponse;
import com.harmonia.catalog.dto.TrackSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    GenreResponse toGenre(Genre genre);

    ArtistSummary toArtistSummary(Artist artist);

    AlbumSummary toAlbumSummary(Album album);

    @Mapping(target = "artistId", source = "artist.id")
    @Mapping(target = "artistName", source = "artist.name")
    @Mapping(target = "albumId", source = "album.id")
    @Mapping(target = "albumTitle", source = "album.title")
    @Mapping(target = "artworkKey", source = "album.artworkKey")
    TrackSummary toTrackSummary(Track track);

    @Mapping(target = "genres", source = "artist.genres")
    @Mapping(target = "albums", source = "albums")
    ArtistResponse toArtist(Artist artist, List<AlbumSummary> albums);

    @Mapping(target = "id", source = "album.id")
    @Mapping(target = "title", source = "album.title")
    @Mapping(target = "albumType", source = "album.albumType")
    @Mapping(target = "releaseDate", source = "album.releaseDate")
    @Mapping(target = "artworkKey", source = "album.artworkKey")
    @Mapping(target = "status", source = "album.status")
    @Mapping(target = "artist", source = "artist")
    @Mapping(target = "tracks", source = "tracks")
    AlbumResponse toAlbum(Album album, ArtistSummary artist, List<TrackSummary> tracks);

    @Mapping(target = "artistId", source = "artist.id")
    @Mapping(target = "albumId", source = "album.id")
    TrackResponse toTrack(Track track);
}
