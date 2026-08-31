import { Component, inject, input } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { CatalogService } from '../../core/services/catalog';
import { SearchService } from '../../core/services/search';
import { MediaCard } from '../../shared/components/media-card';
import { SectionRow } from '../../shared/components/section-row';
import { TrackRow } from '../../shared/components/track-row';
import { PlayerStore } from '../../player/player-store';
import { EmptyState } from '../../shared/components/empty-state';

@Component({
  selector: 'app-genre-detail',
  imports: [MediaCard, SectionRow, TrackRow, EmptyState],
  template: `
    <div class="page">
      <header>
        <p class="eyebrow">Genre</p>
        <h1>{{ genre.value()?.name || 'Genre' }}</h1>
        <p>{{ genre.value()?.description || 'A lane through the catalog.' }}</p>
      </header>
      @if (!results.value()?.tracks.length && !results.value()?.albums.length) {
        <app-empty-state title="Quiet on this shelf" message="No titles tagged to this genre yet." />
      }
      <section>
        @for (track of results.value()?.tracks ?? []; track track.id; let i = $index) {
          <app-track-row [track]="track" [index]="i + 1" (play)="player.playFromList(results.value()?.tracks ?? [], i)" />
        }
      </section>
      <app-section-row title="Albums">
        @for (album of results.value()?.albums ?? []; track album.id) {
          <app-media-card [title]="album.title" [subtitle]="album.artistName || ''" [image]="album.coverUrl" [link]="['/albums', album.id]" />
        }
      </app-section-row>
    </div>
  `,
  styles: `
    .page { display: grid; gap: 1.4rem; }
    h1, p { margin: 0; }
    p { color: var(--muted); }
    .eyebrow { color: var(--accent); letter-spacing: 0.12em; text-transform: uppercase; font-size: 0.75rem; }
  `,
})
export class GenreDetail {
  private readonly catalog = inject(CatalogService);
  private readonly search = inject(SearchService);
  readonly player = inject(PlayerStore);
  readonly id = input.required<string>();
  readonly genre = rxResource({
    params: () => this.id(),
    stream: ({ params: request }) => this.catalog.getGenre(request),
  });
  readonly results = rxResource({
    params: () => this.genre.value()?.name ?? '',
    stream: ({ params: request }) => this.search.search(request).pipe(map((res) => res)),
  });
}
