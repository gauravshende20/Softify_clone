export interface Artist {
  id: string;
  name: string;
  bio?: string;
  imageUrl?: string;
  genres?: string[];
  followers?: number;
  followed?: boolean;
  monthlyListeners?: number;
  verified?: boolean;
}

export interface Album {
  id: string;
  title: string;
  artistId?: string;
  artistName?: string;
  artist?: Artist;
  coverUrl?: string;
  releaseDate?: string;
  trackCount?: number;
  durationSec?: number;
  genre?: string;
  year?: number;
  tracks?: Track[];
}

export interface Track {
  id: string;
  title: string;
  artistId?: string;
  artistName?: string;
  artist?: Artist;
  albumId?: string;
  albumTitle?: string;
  album?: Album;
  coverUrl?: string;
  durationSec: number;
  explicit?: boolean;
  playCount?: number;
  liked?: boolean;
  trackNumber?: number;
  genre?: string;
  audioUrl?: string;
}

export interface Genre {
  id: string;
  name: string;
  imageUrl?: string;
  color?: string;
  description?: string;
}

export interface StreamUrlResponse {
  url: string;
  expiresAt?: string;
}

export interface TrackUploadMeta {
  title: string;
  albumId?: string;
  genreId?: string;
  explicit?: boolean;
}
