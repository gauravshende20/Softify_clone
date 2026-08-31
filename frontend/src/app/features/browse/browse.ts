import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { map } from 'rxjs';
import { CatalogService } from '../../core/services/catalog';
import { coverGradient } from '../../shared/utils/format';
import { MediaCard } from '../../shared/components/media-card';
import { SectionRow } from '../../shared/components/section-row';
import { EmptyState } from '../../shared/components/empty-state';
import { TrackRow } from '../../shared/components/track-row';
import { PlayerStore } from '../../player/player-store';

@Component({
  selector: 'app-browse',
  imports: [RouterLink, MediaCard, SectionRow, EmptyState, TrackRow],
  templateUrl: './browse.html',
  styleUrl: './browse.scss',
})
export class Browse {
  private readonly catalog = inject(CatalogService);
  readonly player = inject(PlayerStore);
  readonly gradient = coverGradient;
  readonly genres = rxResource({
    stream: () => this.catalog.listGenres(),
    defaultValue: [],
  });
  readonly albums = rxResource({
    stream: () => this.catalog.listAlbums(0, 12).pipe(map((page) => page.content)),
    defaultValue: [],
  });
  readonly artists = rxResource({
    stream: () => this.catalog.listArtists(0, 12).pipe(map((page) => page.content)),
    defaultValue: [],
  });
  readonly tracks = rxResource({
    stream: () => this.catalog.listTracks(0, 12),
    defaultValue: [],
  });
}
