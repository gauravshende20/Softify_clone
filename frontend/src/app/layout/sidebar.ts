import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { AuthService } from '../core/auth/auth';
import { PlaylistService } from '../core/services/playlist';
import { ToastService } from '../core/services/toast';
import { Icon } from '../shared/components/icon';
import { httpMessage } from '../shared/utils/format';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, Icon, ReactiveFormsModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})
export class Sidebar {
  private readonly playlistsApi = inject(PlaylistService);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);
  readonly creating = signal(false);
  readonly name = new FormControl('', { nonNullable: true });

  readonly playlists = rxResource({
    stream: () => this.playlistsApi.listMine(),
    defaultValue: [],
  });

  createPlaylist(): void {
    const name = this.name.value.trim() || 'New playlist';
    this.playlistsApi.create({ name, public: false }).subscribe({
      next: () => {
        this.name.setValue('');
        this.creating.set(false);
        this.playlists.reload();
        this.toast.success('Playlist created');
      },
      error: (err) => this.toast.error(httpMessage(err, 'Could not create playlist')),
    });
  }
}
