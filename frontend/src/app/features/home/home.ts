import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { RecommendationService } from '../../core/services/recommendation';
import { UserService } from '../../core/services/user';
import { PlayerStore } from '../../player/player-store';
import { EmptyState } from '../../shared/components/empty-state';
import { MediaCard } from '../../shared/components/media-card';
import { SectionRow } from '../../shared/components/section-row';
import { TrackRow } from '../../shared/components/track-row';
import { AuthService } from '../../core/auth/auth';

@Component({
  selector: 'app-home',
  imports: [SectionRow, MediaCard, TrackRow, EmptyState],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private readonly recs = inject(RecommendationService);
  private readonly users = inject(UserService);
  readonly auth = inject(AuthService);
  readonly player = inject(PlayerStore);

  readonly feed = rxResource({
    stream: () =>
      forkJoin({
        recent: this.users.recentlyPlayed(),
        madeForYou: this.recs.madeForYou(),
        trending: this.recs.trending(),
        artists: this.recs.popularArtists(),
        releases: this.recs.newReleases(),
      }),
    defaultValue: { recent: [], madeForYou: [], trending: [], artists: [], releases: [] },
  });

  like(trackId: string): void {
    this.users.likeTrack(trackId).subscribe();
  }
}
