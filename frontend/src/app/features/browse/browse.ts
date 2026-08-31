import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { CatalogService } from '../../core/services/catalog';
import { RecommendationService } from '../../core/services/recommendation';
import { coverGradient } from '../../shared/utils/format';
import { MediaCard } from '../../shared/components/media-card';
import { SectionRow } from '../../shared/components/section-row';
import { EmptyState } from '../../shared/components/empty-state';

@Component({
  selector: 'app-browse',
  imports: [RouterLink, MediaCard, SectionRow, EmptyState],
  templateUrl: './browse.html',
  styleUrl: './browse.scss',
})
export class Browse {
  private readonly catalog = inject(CatalogService);
  private readonly recs = inject(RecommendationService);
  readonly gradient = coverGradient;
  readonly genres = rxResource({
    stream: () => this.catalog.listGenres(),
    defaultValue: [],
  });
  readonly releases = rxResource({
    stream: () => this.recs.newReleases(),
    defaultValue: [],
  });
}
