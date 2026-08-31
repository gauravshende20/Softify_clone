import { Component, inject, input, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin, of, switchMap } from 'rxjs';
import { CatalogService } from '../../core/services/catalog';
import { PlayerStore } from '../../player/player-store';
import { ToastService } from '../../core/services/toast';
import { CoverArt } from '../../shared/components/cover-art';
import { MediaCard } from '../../shared/components/media-card';
import { SectionRow } from '../../shared/components/section-row';
import { TrackRow } from '../../shared/components/track-row';
import { EmptyState } from '../../shared/components/empty-state';
import { formatCount, httpMessage } from '../../shared/utils/format';

@Component({
  selector: 'app-artist-detail',
  imports: [CoverArt, MediaCard, SectionRow, TrackRow, EmptyState],
  templateUrl: './artist-detail.html',
  styleUrl: './artist-detail.scss',
})
export class ArtistDetail {
  private readonly catalog = inject(CatalogService);
  private readonly toast = inject(ToastService);
  readonly player = inject(PlayerStore);
  readonly id = input.required<string>();
  readonly formatCount = formatCount;
  readonly following = signal(false);

  readonly data = rxResource({
    params: () => this.id(),
    stream: ({ params: request }) =>
      this.catalog.getArtist(request).pipe(
        switchMap((artist) => {
          if (!artist) {
            return of({ artist: null, tracks: [], albums: [] });
          }
          this.following.set(!!artist.followed);
          return forkJoin({
            artist: of(artist),
            tracks: this.catalog.artistTracks(request),
            albums: this.catalog.artistAlbums(request),
          });
        }),
      ),
    defaultValue: { artist: null, tracks: [], albums: [] },
  });

  playAll(): void {
    this.player.playFromList(this.data.value().tracks, 0, { contextType: 'artist', contextId: this.id() });
  }

  toggleFollow(): void {
    const id = this.id();
    const req = this.following() ? this.catalog.unfollowArtist(id) : this.catalog.followArtist(id);
    req.subscribe({
      next: () => {
        this.following.update((v) => !v);
        this.toast.success(this.following() ? 'Following' : 'Unfollowed');
      },
      error: (err) => this.toast.error(httpMessage(err, 'Could not update follow')),
    });
  }
}
