import { Component, inject, input } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { forkJoin, of, switchMap } from 'rxjs';
import { CatalogService } from '../../core/services/catalog';
import { RecommendationService } from '../../core/services/recommendation';
import { UserService } from '../../core/services/user';
import { ToastService } from '../../core/services/toast';
import { PlayerStore } from '../../player/player-store';
import { CoverArt } from '../../shared/components/cover-art';
import { TrackRow } from '../../shared/components/track-row';
import { EmptyState } from '../../shared/components/empty-state';
import { DurationPipe } from '../../shared/pipes/duration-pipe';
import { httpMessage } from '../../shared/utils/format';

@Component({
  selector: 'app-track-detail',
  imports: [RouterLink, CoverArt, TrackRow, EmptyState, DurationPipe],
  templateUrl: './track-detail.html',
  styleUrl: './track-detail.scss',
})
export class TrackDetail {
  private readonly catalog = inject(CatalogService);
  private readonly recs = inject(RecommendationService);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);
  readonly player = inject(PlayerStore);
  readonly id = input.required<string>();

  readonly data = rxResource({
    params: () => this.id(),
    stream: ({ params: request }) =>
      this.catalog.getTrack(request).pipe(
        switchMap((track) => {
          if (!track) {
            return of({ track: null, related: [] });
          }
          return forkJoin({ track: of(track), related: this.recs.forTrack(request) });
        }),
      ),
    defaultValue: { track: null, related: [] },
  });

  play(): void {
    const track = this.data.value().track;
    if (track) {
      this.player.playFromList([track, ...this.data.value().related], 0);
    }
  }

  like(): void {
    const track = this.data.value().track;
    if (!track) {
      return;
    }
    const req = track.liked ? this.users.unlikeTrack(track.id) : this.users.likeTrack(track.id);
    req.subscribe({
      next: () => this.toast.success(track.liked ? 'Removed from Liked songs' : 'Added to Liked songs'),
      error: (err) => this.toast.error(httpMessage(err, 'Could not update liked songs')),
    });
  }
}
