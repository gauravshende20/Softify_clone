import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { PlaybackApi } from '../core/services/playback-api';
import { Track } from '../core/models/catalog';
import { PlayerStore } from './player-store';

class FakeAudio {
  src = '';
  currentTime = 0;
  duration = 180;
  volume = 1;
  muted = false;
  paused = true;
  preload = '';
  play = vi.fn().mockResolvedValue(undefined);
  pause = vi.fn(function (this: FakeAudio) {
    this.paused = true;
  });
  load = vi.fn();
  addEventListener = vi.fn();
  removeEventListener = vi.fn();
}

const track = (id: string, title = id): Track => ({
  id,
  title,
  artistName: 'Nova Lane',
  durationSec: 200,
  coverUrl: '',
});

describe('PlayerStore', () => {
  let store: PlayerStore;
  let playback: {
    streamUrl: ReturnType<typeof vi.fn>;
    play: ReturnType<typeof vi.fn>;
    pause: ReturnType<typeof vi.fn>;
    complete: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    vi.stubGlobal('Audio', FakeAudio);
    sessionStorage.clear();
    playback = {
      streamUrl: vi.fn(() => of({ url: 'https://cdn.harmonia.test/a.mp3' })),
      play: vi.fn(() => of(undefined)),
      pause: vi.fn(() => of(undefined)),
      complete: vi.fn(() => of(undefined)),
    };
    TestBed.configureTestingModule({
      providers: [{ provide: PlaybackApi, useValue: playback }],
    });
    store = TestBed.inject(PlayerStore);
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    sessionStorage.clear();
  });

  it('playFromList sets the queue, index, and current track', () => {
    const list = [track('1', 'Dawn'), track('2', 'Dusk'), track('3', 'Night')];
    store.playFromList(list, 1);
    expect(store.queue().map((t) => t.id)).toEqual(['1', '2', '3']);
    expect(store.index()).toBe(1);
    expect(store.currentTrack()?.title).toBe('Dusk');
    expect(playback.streamUrl).toHaveBeenCalledWith('2');
  });

  it('next and prev move through the queue', () => {
    store.playFromList([track('a'), track('b'), track('c')], 1);
    store.next();
    expect(store.currentTrack()?.id).toBe('c');
    store.prev();
    expect(store.currentTrack()?.id).toBe('b');
  });

  it('cycleRepeat walks off → all → one → off', () => {
    expect(store.repeat()).toBe('off');
    store.cycleRepeat();
    expect(store.repeat()).toBe('all');
    store.cycleRepeat();
    expect(store.repeat()).toBe('one');
    store.cycleRepeat();
    expect(store.repeat()).toBe('off');
  });

  it('toggleShuffle, toggleMute, and setVolume update signals', () => {
    expect(store.shuffle()).toBe(false);
    store.toggleShuffle();
    expect(store.shuffle()).toBe(true);

    store.setVolume(0.4);
    expect(store.volume()).toBe(0.4);
    expect(sessionStorage.getItem('harmonia.volume')).toBe('0.4');

    store.toggleMute();
    expect(store.muted()).toBe(true);
    store.toggleMute();
    expect(store.muted()).toBe(false);
  });

  it('pause reports playing=false', async () => {
    store.playFromList([track('a')], 0);
    store.play();
    await Promise.resolve();
    store.pause();
    expect(store.playing()).toBe(false);
    expect(playback.pause).toHaveBeenCalled();
  });
});
