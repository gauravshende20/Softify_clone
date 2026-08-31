import { DestroyRef, Service, computed, inject, signal } from '@angular/core';
import { Track } from '../core/models/catalog';
import { RepeatMode } from '../core/models/playback';
import { PlaybackApi } from '../core/services/playback-api';

const VOLUME_KEY = 'harmonia.volume';

@Service()
export class PlayerStore {
  private readonly playbackApi = inject(PlaybackApi);
  private readonly destroyRef = inject(DestroyRef);
  private audio: HTMLAudioElement | null = null;
  private shuffleBag: number[] = [];
  private readonly onDestroy = this.destroyRef.onDestroy(() => this.teardown());

  readonly currentTrack = signal<Track | null>(null);
  readonly queue = signal<Track[]>([]);
  readonly index = signal(0);
  readonly playing = signal(false);
  readonly position = signal(0);
  readonly duration = signal(0);
  readonly volume = signal(readVolume());
  readonly muted = signal(false);
  readonly shuffle = signal(false);
  readonly repeat = signal<RepeatMode>('off');
  readonly queueOpen = signal(false);
  readonly loading = signal(false);

  readonly hasTrack = computed(() => !!this.currentTrack());
  readonly canPrev = computed(() => this.index() > 0 || this.repeat() !== 'off');
  readonly canNext = computed(
    () => this.queue().length > 1 || this.repeat() === 'all' || this.repeat() === 'one',
  );

  play(): void {
    const audio = this.ensureAudio();
    const track = this.currentTrack();
    void audio.play().then(
      () => {
        this.playing.set(true);
        if (track) {
          this.playbackApi
            .play({ trackId: track.id, positionSec: this.position() })
            .subscribe();
        }
      },
      () => this.playing.set(false),
    );
  }

  pause(): void {
    this.ensureAudio().pause();
    this.playing.set(false);
    const track = this.currentTrack();
    if (track) {
      this.playbackApi.pause({ trackId: track.id, positionSec: this.position() }).subscribe();
    }
  }

  togglePlay(): void {
    if (this.playing()) {
      this.pause();
    } else {
      this.play();
    }
  }

  next(): void {
    const tracks = this.queue();
    if (!tracks.length) {
      return;
    }
    if (this.repeat() === 'one') {
      this.seek(0);
      this.play();
      return;
    }
    const nextIndex = this.resolveNextIndex();
    if (nextIndex == null) {
      this.pause();
      return;
    }
    this.loadAt(nextIndex, true);
  }

  prev(): void {
    if (this.position() > 3) {
      this.seek(0);
      return;
    }
    const tracks = this.queue();
    if (!tracks.length) {
      return;
    }
    const current = this.index();
    const nextIndex = current === 0 ? (this.repeat() === 'all' ? tracks.length - 1 : 0) : current - 1;
    this.loadAt(nextIndex, true);
  }

  seek(seconds: number): void {
    const audio = this.ensureAudio();
    const duration = this.duration() || audio.duration || 0;
    const next = Math.min(Math.max(0, seconds), duration || seconds);
    audio.currentTime = next;
    this.position.set(next);
  }

  seekRatio(ratio: number): void {
    this.seek(ratio * (this.duration() || 0));
  }

  setVolume(value: number): void {
    const volume = Math.min(1, Math.max(0, value));
    this.volume.set(volume);
    this.ensureAudio().volume = volume;
    if (volume > 0 && this.muted()) {
      this.muted.set(false);
      this.ensureAudio().muted = false;
    }
    sessionStorage.setItem(VOLUME_KEY, String(volume));
  }

  toggleMute(): void {
    const next = !this.muted();
    this.muted.set(next);
    this.ensureAudio().muted = next;
  }

  toggleShuffle(): void {
    const enabled = !this.shuffle();
    this.shuffle.set(enabled);
    this.shuffleBag = enabled ? this.buildShuffleBag(this.index()) : [];
  }

  cycleRepeat(): void {
    const order: RepeatMode[] = ['off', 'all', 'one'];
    const next = order[(order.indexOf(this.repeat()) + 1) % order.length];
    this.repeat.set(next);
  }

  toggleQueue(): void {
    this.queueOpen.update((open) => !open);
  }

  playFromList(
    tracks: Track[],
    startIndex = 0,
    context?: { contextType?: PlaybackContext; contextId?: string },
  ): void {
    if (!tracks.length) {
      return;
    }
    const safeIndex = Math.min(Math.max(0, startIndex), tracks.length - 1);
    this.queue.set([...tracks]);
    this.shuffleBag = this.shuffle() ? this.buildShuffleBag(safeIndex) : [];
    this.loadAt(safeIndex, true, context);
  }

  playTrack(track: Track, extraQueue: Track[] = []): void {
    const queue = extraQueue.length ? extraQueue : [track, ...this.queue().filter((t) => t.id !== track.id)];
    const index = Math.max(
      0,
      queue.findIndex((item) => item.id === track.id),
    );
    this.playFromList(queue, index);
  }

  private loadAt(
    index: number,
    autoplay: boolean,
    context?: { contextType?: PlaybackContext; contextId?: string },
  ): void {
    const track = this.queue()[index];
    if (!track) {
      return;
    }
    this.index.set(index);
    this.currentTrack.set(track);
    this.position.set(0);
    this.duration.set(track.durationSec || 0);
    this.loading.set(true);
    this.playbackApi.streamUrl(track.id).subscribe({
      next: (res) => {
        const audio = this.ensureAudio();
        audio.src = res.url || track.audioUrl || '';
        audio.load();
        this.loading.set(false);
        if (autoplay && audio.src) {
          this.play();
        }
        if (autoplay) {
          this.playbackApi
            .play({
              trackId: track.id,
              positionSec: 0,
              contextType: context?.contextType,
              contextId: context?.contextId,
            })
            .subscribe();
        }
      },
      error: () => {
        this.loading.set(false);
        const audio = this.ensureAudio();
        if (track.audioUrl) {
          audio.src = track.audioUrl;
          audio.load();
          if (autoplay) {
            this.play();
          }
        }
      },
    });
  }

  private resolveNextIndex(): number | null {
    const tracks = this.queue();
    const current = this.index();
    if (this.shuffle() && tracks.length > 1) {
      if (!this.shuffleBag.length) {
        this.shuffleBag = this.buildShuffleBag(current);
      }
      return this.shuffleBag.shift() ?? current;
    }
    if (current < tracks.length - 1) {
      return current + 1;
    }
    if (this.repeat() === 'all') {
      return 0;
    }
    return null;
  }

  private buildShuffleBag(exclude: number): number[] {
    const bag = this.queue()
      .map((_, i) => i)
      .filter((i) => i !== exclude);
    for (let i = bag.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [bag[i], bag[j]] = [bag[j], bag[i]];
    }
    return bag;
  }

  private ensureAudio(): HTMLAudioElement {
    if (this.audio) {
      return this.audio;
    }
    const audio = new Audio();
    audio.preload = 'metadata';
    audio.volume = this.volume();
    audio.muted = this.muted();
    audio.addEventListener('timeupdate', () => this.position.set(audio.currentTime || 0));
    audio.addEventListener('durationchange', () => this.duration.set(audio.duration || 0));
    audio.addEventListener('ended', () => this.next());
    audio.addEventListener('play', () => this.playing.set(true));
    audio.addEventListener('pause', () => this.playing.set(false));
    this.audio = audio;
    return audio;
  }

  private teardown(): void {
    if (!this.audio) {
      return;
    }
    this.audio.pause();
    this.audio.src = '';
    this.audio = null;
  }
}

type PlaybackContext = 'album' | 'playlist' | 'artist' | 'queue' | 'liked' | 'search' | 'home';

function readVolume(): number {
  const raw = sessionStorage.getItem(VOLUME_KEY);
  const parsed = raw ? Number(raw) : 0.8;
  return Number.isFinite(parsed) ? Math.min(1, Math.max(0, parsed)) : 0.8;
}
