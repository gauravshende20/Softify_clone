import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, catchError, map, of, switchMap } from 'rxjs';
import { API_BASE, PageResponse, asList } from '../models/api';
import { Album, Artist, Track } from '../models/catalog';
import { Playlist } from '../models/playlist';
import { UpdateProfileRequest, UserProfile } from '../models/user';
import { LibrarySnapshot } from '../models/library';

@Service()
export class UserService {
  private readonly http = inject(HttpClient);

  me(): Observable<UserProfile | null> {
    return this.http.get<UserProfile>(`${API_BASE}/me`).pipe(catchError(() => of(null)));
  }

  updateMe(payload: UpdateProfileRequest): Observable<UserProfile> {
    return this.http.put<UserProfile>(`${API_BASE}/me`, payload);
  }

  library(): Observable<LibrarySnapshot> {
    return this.http.get<LibrarySnapshot>(`${API_BASE}/library`).pipe(
      catchError(() => of({ playlists: [], albums: [], artists: [], likedTracks: [] })),
    );
  }

  playlists(): Observable<Playlist[]> {
    return this.http
      .get<Playlist[] | PageResponse<Playlist>>(`${API_BASE}/library/playlists`)
      .pipe(map(asList), catchError(() => of([] as Playlist[])));
  }

  albums(): Observable<Album[]> {
    return this.http
      .get<Album[] | PageResponse<Album>>(`${API_BASE}/library/albums`)
      .pipe(map(asList), catchError(() => of([] as Album[])));
  }

  artists(): Observable<Artist[]> {
    return this.http
      .get<Artist[] | PageResponse<Artist>>(`${API_BASE}/library/artists`)
      .pipe(map(asList), catchError(() => of([] as Artist[])));
  }

  likedSongs(): Observable<Track[]> {
    return this.hydrateTracks(
      this.http.get<PageResponse<{ trackId: string }> | { trackId: string }[]>(
        `${API_BASE}/library/liked-songs`,
      ),
    );
  }

  likeTrack(trackId: string): Observable<void> {
    return this.http.post<void>(`${API_BASE}/library/liked-songs/${trackId}`, {});
  }

  unlikeTrack(trackId: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/library/liked-songs/${trackId}`);
  }

  recentlyPlayed(): Observable<Track[]> {
    return this.hydrateTracks(
      this.http.get<PageResponse<{ trackId: string }> | { trackId: string }[]>(
        `${API_BASE}/me/recently-played`,
      ),
    );
  }

  private hydrateTracks(
    source: Observable<PageResponse<{ trackId: string }> | { trackId: string }[]>,
  ): Observable<Track[]> {
    return source.pipe(
      switchMap((page) => {
        const ids = asList(page)
          .map((row) => row.trackId)
          .filter(Boolean);
        if (!ids.length) {
          return of([] as Track[]);
        }
        return this.http
          .get<Track[] | PageResponse<Track>>(`${API_BASE}/tracks`, {
            params: { ids: ids.join(',') },
          })
          .pipe(map((value) => asList(value).map(normalizeTrack)));
      }),
      catchError(() => of([] as Track[])),
    );
  }

  saveAlbum(albumId: string): Observable<void> {
    return this.http.post<void>(`${API_BASE}/library/albums/${albumId}`, {});
  }

  removeAlbum(albumId: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/library/albums/${albumId}`);
  }
}

function normalizeTrack(track: Track & { durationMs?: number; artworkKey?: string }): Track {
  const durationSec =
    track.durationSec || (track.durationMs ? Math.round(track.durationMs / 1000) : 0);
  return {
    ...track,
    id: track.id,
    title: track.title,
    durationSec,
    coverUrl: track.coverUrl || track.artworkKey,
    artistName: track.artistName || track.artist?.name,
    albumTitle: track.albumTitle || track.album?.title,
  };
}
