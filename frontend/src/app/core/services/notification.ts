import { HttpClient } from '@angular/common/http';
import { Service, inject, signal } from '@angular/core';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { API_BASE, PageResponse, asList } from '../models/api';
import { AppNotification } from '../models/notification';

@Service()
export class NotificationService {
  private readonly http = inject(HttpClient);
  readonly items = signal<AppNotification[]>([]);
  readonly unreadCount = signal(0);

  refresh(): Observable<AppNotification[]> {
    return this.http
      .get<AppNotification[] | PageResponse<AppNotification>>(`${API_BASE}/notifications`)
      .pipe(
        map(asList),
        tap((items) => {
          this.items.set(items);
          this.unreadCount.set(items.filter((n) => !n.read).length);
        }),
        catchError(() => {
          this.items.set([]);
          this.unreadCount.set(0);
          return of([] as AppNotification[]);
        }),
      );
  }

  markRead(id: string): Observable<void> {
    return this.http.post<void>(`${API_BASE}/notifications/${id}/read`, {}).pipe(
      tap(() => {
        this.items.update((items) => items.map((n) => (n.id === id ? { ...n, read: true } : n)));
        this.unreadCount.set(this.items().filter((n) => !n.read).length);
      }),
      catchError(() => of(undefined)),
    );
  }
}
