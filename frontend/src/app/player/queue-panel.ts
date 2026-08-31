import { Component, inject } from '@angular/core';
import { TrackRow } from '../shared/components/track-row';
import { Icon } from '../shared/components/icon';
import { EmptyState } from '../shared/components/empty-state';
import { PlayerStore } from './player-store';

@Component({
  selector: 'app-queue-panel',
  imports: [TrackRow, Icon, EmptyState],
  template: `
    @if (player.queueOpen()) {
      <aside class="panel" aria-label="Play queue">
        <header>
          <h2>Queue</h2>
          <button type="button" class="icon-btn" aria-label="Close queue" (click)="player.toggleQueue()">
            <app-icon name="close" />
          </button>
        </header>
        @if (player.queue().length) {
          <div class="list">
            @for (track of player.queue(); track track.id; let i = $index) {
              <app-track-row
                [track]="track"
                [index]="i + 1"
                [active]="i === player.index()"
                (play)="player.playFromList(player.queue(), i)"
              />
            }
          </div>
        } @else {
          <app-empty-state title="Queue is empty" message="Play an album or playlist to fill the queue." />
        }
      </aside>
    }
  `,
  styles: `
    .panel {
      position: fixed;
      top: 4.2rem;
      right: 0.8rem;
      bottom: 6.4rem;
      width: min(26rem, calc(100vw - 1.6rem));
      background: #17171f;
      border: 1px solid var(--border);
      border-radius: 1rem;
      z-index: 25;
      display: grid;
      grid-template-rows: auto 1fr;
      overflow: hidden;
    }
    header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.9rem 1rem;
      border-bottom: 1px solid var(--border);
    }
    h2 {
      margin: 0;
      font-size: 1rem;
    }
    .list {
      overflow: auto;
      padding: 0.4rem;
    }
  `,
})
export class QueuePanel {
  readonly player = inject(PlayerStore);
}
