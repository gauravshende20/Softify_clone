import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { RecommendationService } from '../../core/services/recommendation';
import { PlayerStore } from '../../player/player-store';
import { MediaCard } from '../../shared/components/media-card';
import { SectionRow } from '../../shared/components/section-row';
import { TrackRow } from '../../shared/components/track-row';
import { EmptyState } from '../../shared/components/empty-state';

@Component({
  selector: 'app-recommendations',
  imports: [MediaCard, SectionRow, TrackRow, EmptyState],
  template: `
    <div class="page">
      <header>
        <h1>Made for you</h1>
        <p>Personal mixes, trending rooms, and new releases from Harmonia.</p>
      </header>
      <app-section-row title="Your mixes">
        @for (playlist of feed.value().madeForYou; track playlist.id) {
          <app-media-card [title]="playlist.name" [subtitle]="playlist.description || 'Mix'" [image]="playlist.coverUrl" [link]="['/playlists', playlist.id]" />
        } @empty {
          <app-empty-state title="No mixes yet" message="Listen a little and we’ll shape a room for you." />
        }
      </app-section-row>
      <section>
        <h2>Trending</h2>
        @for (track of feed.value().trending; track track.id; let i = $index) {
          <app-track-row [track]="track" [index]="i + 1" (play)="player.playFromList(feed.value().trending, i)" />
        }
      </section>
      <app-section-row title="New releases">
        @for (album of feed.value().releases; track album.id) {
          <app-media-card [title]="album.title" [subtitle]="album.artistName || ''" [image]="album.coverUrl" [link]="['/albums', album.id]" />
        }
      </app-section-row>
    </div>
  `,
  styles: `
    .page { display: grid; gap: 1.6rem; }
    h1 { margin: 0; font-family: var(--font-display); }
    p { color: var(--muted); }
  `,
})
export class Recommendations {
  private readonly recs = inject(RecommendationService);
  readonly player = inject(PlayerStore);
  readonly feed = rxResource({
    stream: () =>
      forkJoin({
        madeForYou: this.recs.madeForYou(),
        trending: this.recs.trending(),
        releases: this.recs.newReleases(),
      }),
    defaultValue: { madeForYou: [], trending: [], releases: [] },
  });
}
