import { Album, Artist, Genre, Track } from './catalog';
import { Playlist } from './playlist';

export interface SearchResults {
  query: string;
  tracks: Track[];
  artists: Artist[];
  albums: Album[];
  playlists: Playlist[];
  genres: Genre[];
}

export interface AutocompleteItem {
  id: string;
  type: 'track' | 'artist' | 'album' | 'playlist' | 'genre';
  title: string;
  subtitle?: string;
  imageUrl?: string;
}

export interface AutocompleteResponse {
  items: AutocompleteItem[];
}
