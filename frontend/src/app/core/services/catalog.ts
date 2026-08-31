import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { API_BASE, PageResponse, asList, asPage, emptyPage } from '../models/api';
import { Album, Artist, Genre, StreamUrlResponse, Track } from '../models/catalog';

@Service()
export class CatalogService {
  private readonly http = inject(HttpClient);

  listArtists(page = 0, size = 20): Observable<PageResponse<Artist>> {
    return this.http
      .get<Artist[] | PageResponse<Artist>>(`${API_BASE}/artists`, { params: pageParams(page, size) })
      .pipe(
        map(asPage),
        catchError(() => of(emptyPage<Artist>(size))),
      );
  }

  getArtist(id: string): Observable<Artist | null> {
    return this.http.get<Artist>(`${API_BASE}/artists/${id}`).pipe(catchError(() => of(null)));
  }

  artistAlbums(id: string): Observable<Album[]> {
    return this.http
      .get<Album[] | PageResponse<Album>>(`${API_BASE}/artists/${id}/albums`)
      .pipe(map(asList), catchError(() => of([] as Album[])));
  }

  artistTracks(id: string): Observable<Track[]> {
    return this.http
      .get<Track[] | PageResponse<Track>>(`${API_BASE}/artists/${id}/tracks`)
      .pipe(map(asList), catchError(() => of([] as Track[])));
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
        map(asPage),
        catchError(() => of(emptyPage<Album>(size))),
      );
  }

  getAlbum(id: string): Observable<Album | null> {
    return this.http.get<Album>(`${API_BASE}/albums/${id}`).pipe(catchError(() => of(null)));
  }

  albumTracks(id: string): Observable<Track[]> {
    return this.http
      .get<Track[] | PageResponse<Track>>(`${API_BASE}/albums/${id}/tracks`)
      .pipe(map(asList), catchError(() => of([] as Track[])));
  }

  getTrack(id: string): Observable<Track | null> {
    return this.http.get<Track>(`${API_BASE}/tracks/${id}`).pipe(catchError(() => of(null)));
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
