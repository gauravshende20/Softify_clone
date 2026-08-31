import { Track } from './catalog';

export type RepeatMode = 'off' | 'all' | 'one';

export interface PlaybackEventRequest {
  trackId: string;
  positionSec?: number;
  contextType?: 'album' | 'playlist' | 'artist' | 'queue' | 'liked' | 'search' | 'home';
  contextId?: string;
}

export interface QueueState {
  tracks: Track[];
  index: number;
}
