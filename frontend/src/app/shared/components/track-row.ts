import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Track } from '../../core/models/catalog';
import { DurationPipe } from '../pipes/duration-pipe';
import { CoverArt } from './cover-art';
import { Icon } from './icon';

@Component({
  selector: 'app-track-row',
  imports: [RouterLink, DurationPipe, CoverArt, Icon],
  template: `
    <div class="row" [class.active]="active()">
      <span class="index">{{ index() }}</span>
      <button type="button" class="main" [attr.aria-label]="'Play ' + track().title" (click)="play.emit()">
        <app-cover-art
          class="thumb"
          [src]="track().coverUrl || track().album?.coverUrl"
          [alt]="track().title"
          [seed]="track().title"
        />
        <span class="meta">
          <strong>{{ track().title }}</strong>
          <a [routerLink]="['/artists', track().artistId || track().artist?.id]" (click)="$event.stopPropagation()">
            {{ track().artistName || track().artist?.name || 'Unknown artist' }}
          </a>
        </span>
      </button>
      <span class="album hide-sm">{{ track().albumTitle || track().album?.title || '' }}</span>
      <button
        type="button"
        class="icon-btn"
        [attr.aria-label]="track().liked ? 'Unlike' : 'Like'"
        [class.on]="track().liked"
        (click)="like.emit()"
      >
        <app-icon name="heart" />
      </button>
      <span class="time">{{ track().durationSec | duration }}</span>
    </div>
  `,
  styles: `
    .row {
      display: grid;
      grid-template-columns: 2rem minmax(0, 2.2fr) minmax(0, 1.4fr) 2.4rem 3.4rem;
      align-items: center;
      gap: 0.75rem;
      padding: 0.4rem 0.6rem;
      border-radius: 0.5rem;
    }
    .row:hover,
    .row.active {
      background: var(--surface-2);
    }
    .index {
      color: var(--muted);
      text-align: center;
      font-variant-numeric: tabular-nums;
    }
    .main {
      display: grid;
      grid-template-columns: 2.5rem minmax(0, 1fr);
      gap: 0.75rem;
      align-items: center;
      background: none;
      border: 0;
      color: inherit;
      text-align: left;
      min-width: 0;
    }
    .thumb {
      width: 2.5rem;
      height: 2.5rem;
      border-radius: 0.3rem;
    }
    .meta {
      display: grid;
      min-width: 0;
    }
    strong,
    a,
    .album {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    a,
    .album,
    .time {
      color: var(--muted);
      font-size: 0.85rem;
    }
    .time {
      text-align: right;
      font-variant-numeric: tabular-nums;
    }
    .icon-btn {
      background: none;
      border: 0;
      color: var(--muted);
    }
    .icon-btn.on {
      color: var(--accent);
    }
    @media (max-width: 720px) {
      .row {
        grid-template-columns: 1.6rem minmax(0, 1fr) 2.4rem 3.2rem;
      }
      .hide-sm {
        display: none;
      }
    }
  `,
})
export class TrackRow {
  readonly track = input.required<Track>();
  readonly index = input(1);
  readonly active = input(false);
  readonly play = output<void>();
  readonly like = output<void>();
}
