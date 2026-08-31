import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import {
  AdminArtistRow,
  AdminPlaylistRow,
  AdminTrackRow,
  AdminUserRow,
  AnalyticsOverview,
} from '../models/admin';
import { API_BASE, PageResponse, asPage, emptyPage } from '../models/api';

@Service()
export class AdminService {
  private readonly http = inject(HttpClient);

  overview(): Observable<AnalyticsOverview> {
    return this.http
      .get<AnalyticsOverview>(`${API_BASE}/analytics/overview`)
      .pipe(catchError(() => of({} as AnalyticsOverview)));
  }

  users(page = 0, size = 20): Observable<PageResponse<AdminUserRow>> {
    return this.page<AdminUserRow>(`${API_BASE}/admin/analytics/users`, page, size);
  }

  artists(page = 0, size = 20): Observable<PageResponse<AdminArtistRow>> {
    return this.page<AdminArtistRow>(`${API_BASE}/admin/analytics/artists`, page, size);
  }

  tracks(page = 0, size = 20): Observable<PageResponse<AdminTrackRow>> {
    return this.page<AdminTrackRow>(`${API_BASE}/admin/analytics/tracks`, page, size);
  }

  playlists(page = 0, size = 20): Observable<PageResponse<AdminPlaylistRow>> {
    return this.page<AdminPlaylistRow>(`${API_BASE}/admin/analytics/playlists`, page, size);
  }

  private page<T>(url: string, page: number, size: number): Observable<PageResponse<T>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<T[] | PageResponse<T>>(url, { params }).pipe(
      map(asPage),
      catchError(() => of(emptyPage<T>(size))),
    );
  }
}
