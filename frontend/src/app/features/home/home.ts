import { Component, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin, map } from 'rxjs';
import { CatalogService } from '../../core/services/catalog';
import { UserService } from '../../core/services/user';
import { PlayerStore } from '../../player/player-store';
import { EmptyState } from '../../shared/components/empty-state';
import { MediaCard } from '../../shared/components/media-card';
import { SectionRow } from '../../shared/components/section-row';
import { TrackRow } from '../../shared/components/track-row';
import { AuthService } from '../../core/auth/auth';
import { Track } from '../../core/models/catalog';

@Component({
  selector: 'app-home',
  imports: [SectionRow, MediaCard, TrackRow, EmptyState],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home {
  private readonly catalog = inject(CatalogService);
  private readonly users = inject(UserService);
  readonly auth = inject(AuthService);
  readonly player = inject(PlayerStore);

  readonly feed = rxResource({
    stream: () =>
      forkJoin({
        recent: this.users.recentlyPlayed(),
        tracks: this.catalog.listTracks(0, 20),
        artists: this.catalog.listArtists(0, 12).pipe(map((page) => page.content)),
        albums: this.catalog.listAlbums(0, 12).pipe(map((page) => page.content)),
      }),
    defaultValue: { recent: [], tracks: [], artists: [], albums: [] },
  });

  demoTrack(): Track | undefined {
    const tracks = this.feed.value().tracks;
    return tracks.find((track) => track.title === 'Sea Glass') ?? tracks[0];
  }

  playDemo(): void {
    const tracks = this.feed.value().tracks;
    const demo = this.demoTrack();
    if (!demo) {
      return;
    }
    const index = Math.max(0, tracks.findIndex((track) => track.id === demo.id));
    this.player.playFromList(tracks, index, { contextType: 'home' });
  }

  like(trackId: string): void {
    this.users.likeTrack(trackId).subscribe();
  }
}
