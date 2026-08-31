import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { API_BASE, PageResponse, asList } from '../models/api';
import { CreatePlaylistRequest, Playlist } from '../models/playlist';
import { Track } from '../models/catalog';

@Service()
export class PlaylistService {
  private readonly http = inject(HttpClient);

  listMine(): Observable<Playlist[]> {
    return this.http
      .get<Playlist[] | PageResponse<Playlist>>(`${API_BASE}/playlists/me`)
      .pipe(
        map(asList),
        catchError(() =>
          this.http
            .get<Playlist[] | PageResponse<Playlist>>(`${API_BASE}/playlists`)
            .pipe(map(asList), catchError(() => of([] as Playlist[]))),
        ),
      );
  }

  get(id: string): Observable<Playlist | null> {
    return this.http.get<Playlist>(`${API_BASE}/playlists/${id}`).pipe(catchError(() => of(null)));
  }

  tracks(id: string): Observable<Track[]> {
    return this.http
      .get<Track[] | PageResponse<Track>>(`${API_BASE}/playlists/${id}/tracks`)
      .pipe(map(asList), catchError(() => of([] as Track[])));
  }

  create(payload: CreatePlaylistRequest): Observable<Playlist> {
    return this.http.post<Playlist>(`${API_BASE}/playlists`, payload);
  }

  update(id: string, payload: Partial<CreatePlaylistRequest>): Observable<Playlist> {
    return this.http.put<Playlist>(`${API_BASE}/playlists/${id}`, payload);
  }

  remove(id: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/playlists/${id}`);
  }

  addTrack(playlistId: string, trackId: string): Observable<void> {
    return this.http.post<void>(`${API_BASE}/playlists/${playlistId}/tracks`, { trackId });
  }

  removeTrack(playlistId: string, trackId: string): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/playlists/${playlistId}/tracks/${trackId}`);
  }
}
