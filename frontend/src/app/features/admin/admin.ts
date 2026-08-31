import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { AdminService } from '../../core/services/admin';
import { formatCount } from '../../shared/utils/format';
import { EmptyState } from '../../shared/components/empty-state';

@Component({
  selector: 'app-admin',
  imports: [EmptyState],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin {
  private readonly admin = inject(AdminService);
  readonly formatCount = formatCount;
  readonly tab = signal<'users' | 'artists' | 'tracks' | 'playlists'>('users');

  readonly overview = rxResource({
    stream: () => this.admin.overview(),
    defaultValue: {},
  });

  readonly tables = rxResource({
    stream: () =>
      forkJoin({
        users: this.admin.users(),
        artists: this.admin.artists(),
        tracks: this.admin.tracks(),
        playlists: this.admin.playlists(),
      }),
    defaultValue: {
      users: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true },
      artists: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true },
      tracks: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true },
      playlists: { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0, first: true, last: true },
    },
  });

  kpi(key: 'users' | 'artists' | 'tracks' | 'playlists' | 'plays' | 'likes'): number {
    const o = this.overview.value();
    const map = {
      users: o.totalUsers ?? o.users ?? 0,
      artists: o.totalArtists ?? o.artists ?? 0,
      tracks: o.totalTracks ?? o.tracks ?? 0,
      playlists: o.totalPlaylists ?? o.playlists ?? 0,
      plays: o.playsLast24h ?? 0,
      likes: o.likesLast24h ?? 0,
    };
    return map[key] ?? 0;
  }
}
