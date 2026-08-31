package com.harmonia.catalog.bootstrap;

import com.harmonia.catalog.domain.Album;
import com.harmonia.catalog.domain.AlbumType;
import com.harmonia.catalog.domain.Artist;
import com.harmonia.catalog.domain.Genre;
import com.harmonia.catalog.domain.Track;
import com.harmonia.catalog.repo.AlbumRepository;
import com.harmonia.catalog.repo.ArtistRepository;
import com.harmonia.catalog.repo.GenreRepository;
import com.harmonia.catalog.repo.TrackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "harmonia.catalog.seed", havingValue = "true", matchIfMissing = true)
public class CatalogSeed implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogSeed.class);
    static final UUID SEED_USER = UUID.fromString("00000000-0000-0000-0000-000000000001");

    static final UUID GENRE_POP = UUID.fromString("a1000000-0000-4000-8000-000000000001");
    static final UUID GENRE_ROCK = UUID.fromString("a1000000-0000-4000-8000-000000000002");
    static final UUID GENRE_HIPHOP = UUID.fromString("a1000000-0000-4000-8000-000000000003");
    static final UUID GENRE_ELECTRONIC = UUID.fromString("a1000000-0000-4000-8000-000000000004");
    static final UUID GENRE_JAZZ = UUID.fromString("a1000000-0000-4000-8000-000000000005");
    static final UUID GENRE_INDIE = UUID.fromString("a1000000-0000-4000-8000-000000000006");

    static final UUID ARTIST_LUNA = UUID.fromString("a2000000-0000-4000-8000-000000000001");
    static final UUID ARTIST_NEON = UUID.fromString("a2000000-0000-4000-8000-000000000002");
    static final UUID ARTIST_ORCHARD = UUID.fromString("a2000000-0000-4000-8000-000000000003");

    static final UUID ALBUM_TIDAL = UUID.fromString("a3000000-0000-4000-8000-000000000001");
    static final UUID ALBUM_GRID = UUID.fromString("a3000000-0000-4000-8000-000000000002");
    static final UUID ALBUM_HUSHED = UUID.fromString("a3000000-0000-4000-8000-000000000003");

    private final GenreRepository genres;
    private final ArtistRepository artists;
    private final AlbumRepository albums;
    private final TrackRepository tracks;

    public CatalogSeed(GenreRepository genres,
                       ArtistRepository artists,
                       AlbumRepository albums,
                       TrackRepository tracks) {
        this.genres = genres;
        this.artists = artists;
        this.albums = albums;
        this.tracks = tracks;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (genres.count() > 0) {
            return;
        }
        Genre pop = genres.save(Genre.create(GENRE_POP, "Pop"));
        Genre rock = genres.save(Genre.create(GENRE_ROCK, "Rock"));
        Genre hipHop = genres.save(Genre.create(GENRE_HIPHOP, "Hip-Hop"));
        Genre electronic = genres.save(Genre.create(GENRE_ELECTRONIC, "Electronic"));
        Genre jazz = genres.save(Genre.create(GENRE_JAZZ, "Jazz"));
        Genre indie = genres.save(Genre.create(GENRE_INDIE, "Indie"));

        Artist luna = artists.save(Artist.seed(ARTIST_LUNA, "Luna Waves",
                "Dream-pop producer from Lisbon blending analog synths and field recordings.",
                "artwork/artists/luna-waves.jpg", SEED_USER, Set.of(electronic, indie), true));
        Artist neon = artists.save(Artist.seed(ARTIST_NEON, "Neon District",
                "Night-drive hip-hop duo from Detroit.",
                "artwork/artists/neon-district.jpg", SEED_USER, Set.of(hipHop, electronic), true));
        Artist orchard = artists.save(Artist.seed(ARTIST_ORCHARD, "The Midnight Orchard",
                "Jazz-leaning indie collective from Montreal.",
                "artwork/artists/midnight-orchard.jpg", SEED_USER, Set.of(jazz, indie, rock), true));

        Album tidal = albums.save(Album.seed(ALBUM_TIDAL, luna, "Tidal Bloom", AlbumType.ALBUM,
                LocalDate.of(2024, 3, 15), "artwork/albums/tidal-bloom.jpg"));
        Album grid = albums.save(Album.seed(ALBUM_GRID, neon, "Gridlines", AlbumType.ALBUM,
                LocalDate.of(2024, 6, 7), "artwork/albums/gridlines.jpg"));
        Album hushed = albums.save(Album.seed(ALBUM_HUSHED, orchard, "Hushed Frequency", AlbumType.ALBUM,
                LocalDate.of(2023, 11, 2), "artwork/albums/hushed-frequency.jpg"));

        seedAlbumTracks(luna, tidal, Set.of(electronic, indie), List.of(
                track(1, "Sea Glass"), track(2, "Low Tide"), track(3, "Phosphor"), track(4, "Harbor Lights")));
        seedAlbumTracks(neon, grid, Set.of(hipHop, electronic), List.of(
                track(5, "Aftercurfew"), track(6, "Sodium Vapor"), track(7, "Meter Drop"), track(8, "Skyway")));
        seedAlbumTracks(orchard, hushed, Set.of(jazz, indie), List.of(
                track(9, "Moss Choir"), track(10, "Blue Porch"), track(11, "Paper Lanterns"), track(12, "Last Call")));

        log.info("Seeded {} genres, {} artists, {} albums, {} tracks",
                genres.count(), artists.count(), albums.count(), tracks.count());
    }

    private void seedAlbumTracks(Artist artist, Album album, Set<Genre> genres, List<SeedTrack> titles) {
        for (int i = 0; i < titles.size(); i++) {
            SeedTrack item = titles.get(i);
            String key = "demo/audio/" + Genre.slugify(artist.getName()) + "/" + Genre.slugify(item.title()) + ".wav";
            tracks.save(Track.seed(item.id(), artist, album, item.title(), 28_000, key, i + 1, genres));
        }
    }

    private static SeedTrack track(int n, String title) {
        return new SeedTrack(UUID.fromString("a4000000-0000-4000-8000-%012d".formatted(n)), title);
    }

    private record SeedTrack(UUID id, String title) {
    }
}
