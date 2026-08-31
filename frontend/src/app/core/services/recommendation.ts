import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { API_BASE, PageResponse, asList } from '../models/api';
import { Album, Artist, Track } from '../models/catalog';
import { Playlist } from '../models/playlist';

@Service()
export class RecommendationService {
  private readonly http = inject(HttpClient);

  madeForYou(): Observable<Playlist[]> {
    return this.getList<Playlist>('made-for-you');
  }

  trending(): Observable<Track[]> {
    return this.getList<Track>('trending');
  }

  popularArtists(): Observable<Artist[]> {
    return this.getList<Artist>('popular-artists');
  }

  newReleases(): Observable<Album[]> {
    return this.getList<Album>('new-releases');
  }

  forTrack(trackId: string): Observable<Track[]> {
    return this.http
      .get<Track[] | PageResponse<Track>>(`${API_BASE}/recommendations/tracks/${trackId}`)
      .pipe(map(asList), catchError(() => of([] as Track[])));
  }

  private getList<T>(path: string): Observable<T[]> {
    return this.http
      .get<T[] | PageResponse<T>>(`${API_BASE}/recommendations/${path}`)
      .pipe(map(asList), catchError(() => of([] as T[])));
  }
}
