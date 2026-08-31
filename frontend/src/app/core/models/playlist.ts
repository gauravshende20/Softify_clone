import { Track } from './catalog';

export interface Playlist {
  id: string;
  name: string;
  description?: string;
  coverUrl?: string;
  ownerId?: string;
  ownerName?: string;
  public?: boolean;
  collaborative?: boolean;
  trackCount?: number;
  durationSec?: number;
  tracks?: Track[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CreatePlaylistRequest {
  name: string;
  description?: string;
  public?: boolean;
}

export interface AddTrackRequest {
  trackId: string;
  position?: number;
}
