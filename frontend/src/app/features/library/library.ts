import { Component, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { UserService } from '../../core/services/user';
import { PlaylistService } from '../../core/services/playlist';
import { MediaCard } from '../../shared/components/media-card';
import { EmptyState } from '../../shared/components/empty-state';
import { RouterLink } from '@angular/router';

type LibraryTab = 'playlists' | 'liked' | 'albums' | 'artists';

@Component({
  selector: 'app-library',
  imports: [MediaCard, EmptyState, RouterLink],
  templateUrl: './library.html',
  styleUrl: './library.scss',
})
export class Library {
  private readonly users = inject(UserService);
  private readonly playlistsApi = inject(PlaylistService);
  readonly tab = signal<LibraryTab>('playlists');

  readonly data = rxResource({
    stream: () =>
      forkJoin({
        playlists: this.playlistsApi.listMine(),
        liked: this.users.likedSongs(),
        albums: this.users.albums(),
        artists: this.users.artists(),
      }),
    defaultValue: { playlists: [], liked: [], albums: [], artists: [] },
  });
}
