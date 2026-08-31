import { Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CoverArt } from './cover-art';
import { Icon } from './icon';

@Component({
  selector: 'app-media-card',
  imports: [RouterLink, CoverArt, Icon],
  template: `
    <a class="card" [routerLink]="link()">
      <div class="art" [class.round]="round()">
        <app-cover-art [src]="image()" [alt]="title()" [seed]="title()" />
        <button
          type="button"
          class="play"
          [attr.aria-label]="'Play ' + title()"
          (click)="onPlay($event)"
        >
          <app-icon name="play" />
        </button>
      </div>
      <h3>{{ title() }}</h3>
      <p>{{ subtitle() }}</p>
    </a>
  `,
  styles: `
    .card {
      display: grid;
      gap: 0.7rem;
      padding: 0.85rem;
      border-radius: 0.9rem;
      color: inherit;
      text-decoration: none;
      background: transparent;
      transition: background 0.15s ease;
    }
    .card:hover,
    .card:focus-visible {
      background: var(--surface-2);
    }
    .art {
      position: relative;
      border-radius: 0.6rem;
      overflow: hidden;
      box-shadow: 0 12px 30px rgba(0, 0, 0, 0.35);
    }
    .art.round {
      border-radius: 999px;
    }
    h3,
    p {
      margin: 0;
    }
    h3 {
      font-size: 0.95rem;
      font-weight: 650;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    p {
      color: var(--muted);
      font-size: 0.82rem;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .play {
      position: absolute;
      right: 0.6rem;
      bottom: 0.6rem;
      width: 2.6rem;
      height: 2.6rem;
      border: 0;
      border-radius: 999px;
      background: var(--accent);
      color: #1a140c;
      display: grid;
      place-items: center;
      opacity: 0;
      transform: translateY(8px);
      transition:
        opacity 0.15s ease,
        transform 0.15s ease;
    }
    .card:hover .play,
    .card:focus-within .play {
      opacity: 1;
      transform: none;
    }
  `,
})
export class MediaCard {
  readonly title = input.required<string>();
  readonly subtitle = input('');
  readonly image = input<string | undefined | null>();
  readonly link = input<string | unknown[]>('/');
  readonly round = input(false);
  readonly play = output<void>();

  onPlay(event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.play.emit();
  }
}
