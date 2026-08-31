export interface AnalyticsOverview {
  totalUsers?: number;
  totalArtists?: number;
  totalTracks?: number;
  totalPlaylists?: number;
  playsLast24h?: number;
  likesLast24h?: number;
  activeListeners?: number;
  newUsersLast7d?: number;
  users?: number;
  artists?: number;
  tracks?: number;
  playlists?: number;
}

export interface AdminUserRow {
  id: string;
  email: string;
  displayName?: string;
  roles?: string[];
  enabled?: boolean;
  createdAt?: string;
}

export interface AdminArtistRow {
  id: string;
  name: string;
  email?: string;
  followers?: number;
  trackCount?: number;
  verified?: boolean;
}

export interface AdminTrackRow {
  id: string;
  title: string;
  artistName?: string;
  playCount?: number;
  published?: boolean;
  createdAt?: string;
}

export interface AdminPlaylistRow {
  id: string;
  name: string;
  ownerName?: string;
  trackCount?: number;
  public?: boolean;
}
