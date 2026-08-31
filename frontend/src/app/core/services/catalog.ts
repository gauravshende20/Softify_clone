import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { API_BASE, PageResponse, asList, asPage, emptyPage } from '../models/api';
import { Album, Artist, Genre, StreamUrlResponse, Track } from '../models/catalog';

@Service()
export class CatalogService {
  private readonly http = inject(HttpClient);

  listTracks(page = 0, size = 20): Observable<Track[]> {
    return this.http
      .get<Track[] | PageResponse<Track>>(`${API_BASE}/tracks`, { params: pageParams(page, size) })
      .pipe(map((value) => asList(value).map(normalizeTrack)), catchError(() => of([] as Track[])));
  }

  listArtists(page = 0, size = 20): Observable<PageResponse<Artist>> {
    return this.http
      .get<Artist[] | PageResponse<Artist>>(`${API_BASE}/artists`, { params: pageParams(page, size) })
      .pipe(
        map((value) => {
          const page = asPage(value);
          return { ...page, content: page.content.map(normalizeArtist) };
        }),
        catchError(() => of(emptyPage<Artist>(size))),
      );
  }

  getArtist(id: string): Observable<Artist | null> {
    return this.http.get<Artist>(`${API_BASE}/artists/${id}`).pipe(
      map(normalizeArtist),
      catchError(() => of(null)),
    );
  }

  artistAlbums(id: string): Observable<Album[]> {
    return this.http
      .get<Album[] | PageResponse<Album>>(`${API_BASE}/artists/${id}/albums`)
      .pipe(map((value) => asList(value).map(normalizeAlbum)), catchError(() => of([] as Album[])));
  }

  artistTracks(id: string): Observable<Track[]> {
    return this.http
      .get<Track[] | PageResponse<Track>>(`${API_BASE}/artists/${id}/tracks`)
      .pipe(map((value) => asList(value).map(normalizeTrack)), catchError(() => of([] as Track[])));
  }

  followArtist(id: string): Observable<void> {
    return this.http.post<void>(`${API_BASE}/artists/${id}/follow`, {});
  }

  unfollowArtist(id: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/artists/${id}/follow`);
  }

  listAlbums(page = 0, size = 20): Observable<PageResponse<Album>> {
    return this.http
      .get<Album[] | PageResponse<Album>>(`${API_BASE}/albums`, { params: pageParams(page, size) })
      .pipe(
        map((value) => {
          const page = asPage(value);
          return { ...page, content: page.content.map(normalizeAlbum) };
        }),
        catchError(() => of(emptyPage<Album>(size))),
      );
  }

  getAlbum(id: string): Observable<Album | null> {
    return this.http.get<Album & { tracks?: Track[] }>(`${API_BASE}/albums/${id}`).pipe(
      map((album) => {
        const mapped = normalizeAlbum(album);
        mapped.tracks = (album.tracks ?? []).map(normalizeTrack);
        return mapped;
      }),
      catchError(() => of(null)),
    );
  }

  albumTracks(id: string): Observable<Track[]> {
    return this.http.get<Track[] | PageResponse<Track> | { tracks?: Track[] }>(`${API_BASE}/albums/${id}/tracks`).pipe(
      map((value) => {
        if (value && !Array.isArray(value) && 'tracks' in value) {
          return (value.tracks ?? []).map(normalizeTrack);
        }
        return asList(value as Track[] | PageResponse<Track>).map(normalizeTrack);
      }),
      catchError(() =>
        this.getAlbum(id).pipe(map((album) => album?.tracks ?? [])),
      ),
    );
  }

  getTrack(id: string): Observable<Track | null> {
    return this.http.get<Track>(`${API_BASE}/tracks/${id}`).pipe(
      map(normalizeTrack),
      catchError(() => of(null)),
    );
  }

  streamUrl(trackId: string): Observable<StreamUrlResponse> {
    return this.http
      .get<StreamUrlResponse>(`${API_BASE}/tracks/${trackId}/stream`)
      .pipe(catchError(() => of({ url: '' })));
  }

  listGenres(): Observable<Genre[]> {
    return this.http
      .get<Genre[] | PageResponse<Genre>>(`${API_BASE}/genres`)
      .pipe(map(asList), catchError(() => of([] as Genre[])));
  }

  getGenre(id: string): Observable<Genre | null> {
    return this.http.get<Genre>(`${API_BASE}/genres/${id}`).pipe(catchError(() => of(null)));
  }

  myTracks(): Observable<Track[]> {
    return this.http
      .get<Track[] | PageResponse<Track>>(`${API_BASE}/artists/me/tracks`)
      .pipe(map(asList), catchError(() => of([] as Track[])));
  }

  myAlbums(): Observable<Album[]> {
    return this.http
      .get<Album[] | PageResponse<Album>>(`${API_BASE}/artists/me/albums`)
      .pipe(map(asList), catchError(() => of([] as Album[])));
  }

  uploadTrack(file: File, meta: { title: string; albumId?: string; genreId?: string; explicit?: boolean }): Observable<Track> {
    const body = new FormData();
    body.append('file', file);
    body.append('title', meta.title);
    if (meta.albumId) {
      body.append('albumId', meta.albumId);
    }
    if (meta.genreId) {
      body.append('genreId', meta.genreId);
    }
    if (meta.explicit != null) {
      body.append('explicit', String(meta.explicit));
    }
    return this.http.post<Track>(`${API_BASE}/tracks`, body);
  }

  createAlbum(payload: { title: string; genreId?: string; releaseDate?: string }): Observable<Album> {
    return this.http.post<Album>(`${API_BASE}/albums`, payload);
  }
}

function pageParams(page: number, size: number): HttpParams {
  return new HttpParams().set('page', String(page)).set('size', String(size));
}

type RawTrack = Track & {
  durationMs?: number;
  artworkKey?: string;
  artist?: Artist & { imageKey?: string };
  album?: Album & { artworkKey?: string };
};

type RawAlbum = Album & { artworkKey?: string; artist?: Artist & { name?: string } };
type RawArtist = Artist & { imageKey?: string };

export function normalizeTrack(raw: RawTrack): Track {
  const durationSec =
    raw.durationSec || (raw.durationMs != null ? Math.round(raw.durationMs / 1000) : 0);
  return {
    ...raw,
    durationSec,
    artistId: raw.artistId || raw.artist?.id,
    artistName: raw.artistName || raw.artist?.name,
    albumId: raw.albumId || raw.album?.id,
    albumTitle: raw.albumTitle || raw.album?.title,
    coverUrl: raw.coverUrl || raw.artworkKey || raw.album?.artworkKey || raw.album?.coverUrl,
  };
}

export function normalizeAlbum(raw: RawAlbum): Album {
  return {
    ...raw,
    artistName: raw.artistName || raw.artist?.name,
    artistId: raw.artistId || raw.artist?.id,
    coverUrl: raw.coverUrl || raw.artworkKey,
  };
}

export function normalizeArtist(raw: RawArtist): Artist {
  return {
    ...raw,
    imageUrl: raw.imageUrl || raw.imageKey,
  };
}
