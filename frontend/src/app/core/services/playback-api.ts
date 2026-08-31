import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, catchError, of } from 'rxjs';
import { API_BASE } from '../models/api';
import { PlaybackEventRequest } from '../models/playback';
import { StreamUrlResponse } from '../models/catalog';

@Service()
export class PlaybackApi {
  private readonly http = inject(HttpClient);

  streamUrl(trackId: string): Observable<StreamUrlResponse> {
    const params = new HttpParams().set('trackId', trackId);
    return this.http.get<StreamUrlResponse>(`${API_BASE}/playback/stream-url`, { params }).pipe(
      catchError(() =>
        this.http.get<StreamUrlResponse>(`${API_BASE}/tracks/${trackId}/stream`).pipe(
          catchError(() => of({ url: '' })),
        ),
      ),
    );
  }

  play(body: PlaybackEventRequest): Observable<void> {
    return this.http.post<void>(`${API_BASE}/playback/play`, body).pipe(catchError(() => of(undefined)));
  }

  pause(body: PlaybackEventRequest): Observable<void> {
    return this.http.post<void>(`${API_BASE}/playback/pause`, body).pipe(catchError(() => of(undefined)));
  }

  complete(body: PlaybackEventRequest): Observable<void> {
    return this.http
      .post<void>(`${API_BASE}/playback/complete`, body)
      .pipe(catchError(() => of(undefined)));
  }
}
