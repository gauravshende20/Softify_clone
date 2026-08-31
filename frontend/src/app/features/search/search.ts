import { Component, inject, input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { SearchService } from '../../core/services/search';
import { SearchResults } from '../../core/models/search';
import { PlayerStore } from '../../player/player-store';
import { EmptyState } from '../../shared/components/empty-state';
import { MediaCard } from '../../shared/components/media-card';
import { SectionRow } from '../../shared/components/section-row';
import { TrackRow } from '../../shared/components/track-row';
import { UserService } from '../../core/services/user';
import { ToastService } from '../../core/services/toast';

const EMPTY: SearchResults = {
  query: '',
  tracks: [],
  artists: [],
  albums: [],
  playlists: [],
  genres: [],
};

@Component({
  selector: 'app-search',
  imports: [ReactiveFormsModule, EmptyState, MediaCard, SectionRow, TrackRow],
  templateUrl: './search.html',
  styleUrl: './search.scss',
})
export class Search {
  private readonly api = inject(SearchService);
  private readonly router = inject(Router);
  private readonly users = inject(UserService);
  private readonly toast = inject(ToastService);
  readonly player = inject(PlayerStore);
  readonly q = input('');
  readonly query = new FormControl('', { nonNullable: true });

  readonly results = rxResource({
    params: () => this.q()?.trim() ?? '',
    stream: ({ params }) => (params ? this.api.search(params) : of(EMPTY)),
    defaultValue: EMPTY,
  });

  submit(): void {
    const q = this.query.value.trim() || this.q().trim();
    void this.router.navigate(['/search'], { queryParams: q ? { q } : {} });
  }

  toggleLike(trackId: string, liked?: boolean): void {
    const req = liked ? this.users.unlikeTrack(trackId) : this.users.likeTrack(trackId);
    req.subscribe({
      next: () => this.toast.success(liked ? 'Removed from Liked songs' : 'Added to Liked songs'),
      error: () => this.toast.error('Could not update liked songs'),
    });
  }
}
