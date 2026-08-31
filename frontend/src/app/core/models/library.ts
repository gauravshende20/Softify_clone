import { Album, Artist, Track } from './catalog';
import { Playlist } from './playlist';

export interface LibrarySnapshot {
  playlists: Playlist[];
  albums: Album[];
  artists: Artist[];
  likedTracks: Track[];
}
