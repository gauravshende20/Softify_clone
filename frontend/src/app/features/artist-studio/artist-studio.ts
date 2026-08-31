import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { rxResource } from '@angular/core/rxjs-interop';
import { forkJoin } from 'rxjs';
import { CatalogService } from '../../core/services/catalog';
import { ToastService } from '../../core/services/toast';
import { TrackRow } from '../../shared/components/track-row';
import { MediaCard } from '../../shared/components/media-card';
import { EmptyState } from '../../shared/components/empty-state';
import { PlayerStore } from '../../player/player-store';
import { httpMessage } from '../../shared/utils/format';

@Component({
  selector: 'app-artist-studio',
  imports: [ReactiveFormsModule, TrackRow, MediaCard, EmptyState],
  templateUrl: './artist-studio.html',
  styleUrl: './artist-studio.scss',
})
export class ArtistStudio {
  private readonly catalog = inject(CatalogService);
  private readonly toast = inject(ToastService);
  private readonly fb = inject(FormBuilder);
  readonly player = inject(PlayerStore);
  readonly file = signal<File | null>(null);
  readonly uploading = signal(false);

  readonly form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    albumId: [''],
    genreId: [''],
    explicit: [false],
  });

  readonly catalogData = rxResource({
    stream: () =>
      forkJoin({
        tracks: this.catalog.myTracks(),
        albums: this.catalog.myAlbums(),
        genres: this.catalog.listGenres(),
      }),
    defaultValue: { tracks: [], albums: [], genres: [] },
  });

  onFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.file.set(input.files?.[0] ?? null);
  }

  upload(): void {
    const file = this.file();
    if (!file || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.uploading.set(true);
    const { title, albumId, genreId, explicit } = this.form.getRawValue();
    this.catalog
      .uploadTrack(file, {
        title,
        albumId: albumId || undefined,
        genreId: genreId || undefined,
        explicit,
      })
      .subscribe({
        next: () => {
          this.uploading.set(false);
          this.file.set(null);
          this.form.reset({ title: '', albumId: '', genreId: '', explicit: false });
          this.catalogData.reload();
          this.toast.success('Track uploaded');
        },
        error: (err) => {
          this.uploading.set(false);
          this.toast.error(httpMessage(err, 'Upload failed'));
        },
      });
  }
}
