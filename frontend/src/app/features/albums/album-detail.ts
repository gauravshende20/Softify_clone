import { Component, inject, input } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin, of, switchMap } from 'rxjs';
import { CatalogService } from '../../core/services/catalog';
import { UserService } from '../../core/services/user';
import { ToastService } from '../../core/services/toast';
import { PlayerStore } from '../../player/player-store';
import { CoverArt } from '../../shared/components/cover-art';
import { TrackRow } from '../../shared/components/track-row';
import { EmptyState } from '../../shared/components/empty-state';
import { DurationPipe } from '../../shared/pipes/duration-pipe';
import { httpMessage } from '../../shared/utils/format';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-album-detail',
  imports: [CoverArt, TrackRow, EmptyState, DurationPipe, RouterLink],
  templateUrl: './album-detail.html',
  styleUrl: './album-detail.scss',
})
export class AlbumDetail {
  private readonly catalog = inject(CatalogService);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);
  readonly player = inject(PlayerStore);
  readonly id = input.required<string>();

  readonly data = rxResource({
    params: () => this.id(),
    stream: ({ params: request }) =>
      this.catalog.getAlbum(request).pipe(
        switchMap((album) => {
          if (!album) {
            return of({ album: null, tracks: [] });
          }
          return forkJoin({ album: of(album), tracks: this.catalog.albumTracks(request) });
        }),
      ),
    defaultValue: { album: null, tracks: [] },
  });

  play(index = 0): void {
    this.player.playFromList(this.data.value().tracks, index, { contextType: 'album', contextId: this.id() });
  }

  save(): void {
    this.users.saveAlbum(this.id()).subscribe({
      next: () => this.toast.success('Saved to library'),
      error: (err) => this.toast.error(httpMessage(err, 'Could not save album')),
    });
  }

  totalDuration(): number {
    return this.data.value().tracks.reduce((sum, t) => sum + (t.durationSec || 0), 0);
  }
}
