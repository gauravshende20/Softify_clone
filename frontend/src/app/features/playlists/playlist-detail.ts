import { Component, computed, inject, input, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin, of, switchMap } from 'rxjs';
import { AuthService } from '../../core/auth/auth';
import { PlaylistService } from '../../core/services/playlist';
import { SearchService } from '../../core/services/search';
import { ToastService } from '../../core/services/toast';
import { PlayerStore } from '../../player/player-store';
import { CoverArt } from '../../shared/components/cover-art';
import { TrackRow } from '../../shared/components/track-row';
import { EmptyState } from '../../shared/components/empty-state';
import { DurationPipe } from '../../shared/pipes/duration-pipe';
import { httpMessage } from '../../shared/utils/format';
import { Track } from '../../core/models/catalog';

@Component({
  selector: 'app-playlist-detail',
  imports: [CoverArt, TrackRow, EmptyState, DurationPipe, ReactiveFormsModule],
  templateUrl: './playlist-detail.html',
  styleUrl: './playlist-detail.scss',
})
export class PlaylistDetail {
  private readonly api = inject(PlaylistService);
  private readonly search = inject(SearchService);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);
  readonly player = inject(PlayerStore);
  readonly id = input.required<string>();
  readonly query = new FormControl('', { nonNullable: true });
  readonly suggestions = signal<Track[]>([]);

  readonly data = rxResource({
    params: () => this.id(),
    stream: ({ params: request }) =>
      this.api.get(request).pipe(
        switchMap((playlist) => {
          if (!playlist) {
            return of({ playlist: null, tracks: [] as Track[] });
          }
          if (playlist.tracks?.length) {
            return of({ playlist, tracks: playlist.tracks });
          }
          return forkJoin({ playlist: of(playlist), tracks: this.api.tracks(request) });
        }),
      ),
    defaultValue: { playlist: null, tracks: [] as Track[] },
  });

  readonly isOwner = computed(() => {
    const playlist = this.data.value().playlist;
    const user = this.auth.currentUser();
    return !!playlist && !!user && playlist.ownerId === user.id;
  });

  play(index = 0): void {
    this.player.playFromList(this.data.value().tracks, index, {
      contextType: 'playlist',
      contextId: this.id(),
    });
  }

  shuffle(): void {
    const tracks = [...this.data.value().tracks];
    for (let i = tracks.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [tracks[i], tracks[j]] = [tracks[j], tracks[i]];
    }
    this.player.playFromList(tracks, 0, { contextType: 'playlist', contextId: this.id() });
    if (!this.player.shuffle()) {
      this.player.toggleShuffle();
    }
  }

  lookup(): void {
    const q = this.query.value.trim();
    if (q.length < 2) {
      this.suggestions.set([]);
      return;
    }
    this.search.search(q).subscribe((res) => this.suggestions.set(res.tracks.slice(0, 6)));
  }

  add(track: Track): void {
    this.api.addTrack(this.id(), track.id).subscribe({
      next: () => {
        this.toast.success(`Added ${track.title}`);
        this.data.reload();
        this.suggestions.set([]);
        this.query.setValue('');
      },
      error: (err) => this.toast.error(httpMessage(err, 'Could not add track')),
    });
  }

  remove(track: Track): void {
    this.api.removeTrack(this.id(), track.id).subscribe({
      next: () => {
        this.toast.success(`Removed ${track.title}`);
        this.data.reload();
      },
      error: (err) => this.toast.error(httpMessage(err, 'Could not remove track')),
    });
  }

  totalDuration(): number {
    return this.data.value().tracks.reduce((sum, t) => sum + (t.durationSec || 0), 0);
  }
}
