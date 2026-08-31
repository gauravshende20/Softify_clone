import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { UserService } from '../../core/services/user';
import { PlayerStore } from '../../player/player-store';
import { TrackRow } from '../../shared/components/track-row';
import { EmptyState } from '../../shared/components/empty-state';
import { Track } from '../../core/models/catalog';

@Component({
  selector: 'app-recently-played',
  imports: [TrackRow, EmptyState],
  template: `
    <div class="page">
      <header>
        <h1>Recently played</h1>
        <p>A short history of what this room last heard.</p>
      </header>
      @for (track of tracks.value(); track track.id; let i = $index) {
        <app-track-row
          [track]="track"
          [index]="i + 1"
          [active]="player.currentTrack()?.id === track.id"
          (play)="player.playFromList(tracks.value(), i)"
        />
      } @empty {
        <app-empty-state title="Nothing played yet" message="Start a song and it will land here." />
      }
    </div>
  `,
  styles: `
    h1 { margin: 0; font-family: var(--font-display); }
    p { color: var(--muted); }
    header { margin-bottom: 1rem; }
  `,
})
export class RecentlyPlayed {
  private readonly users = inject(UserService);
  readonly player = inject(PlayerStore);
  readonly tracks = rxResource({
    stream: () => this.users.recentlyPlayed(),
    defaultValue: [] as Track[],
  });
}
