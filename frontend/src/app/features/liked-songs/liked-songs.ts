import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { UserService } from '../../core/services/user';
import { ToastService } from '../../core/services/toast';
import { PlayerStore } from '../../player/player-store';
import { TrackRow } from '../../shared/components/track-row';
import { EmptyState } from '../../shared/components/empty-state';
import { Track } from '../../core/models/catalog';
import { httpMessage } from '../../shared/utils/format';

@Component({
  selector: 'app-liked-songs',
  imports: [TrackRow, EmptyState],
  templateUrl: './liked-songs.html',
  styleUrl: './liked-songs.scss',
})
export class LikedSongs {
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);
  readonly player = inject(PlayerStore);
  readonly tracks = rxResource({
    stream: () => this.users.likedSongs(),
    defaultValue: [] as Track[],
  });

  unlike(track: Track): void {
    this.users.unlikeTrack(track.id).subscribe({
      next: () => {
        this.toast.success(`Removed ${track.title}`);
        this.tracks.reload();
      },
      error: (err) => this.toast.error(httpMessage(err, 'Could not unlike track')),
    });
  }
}
