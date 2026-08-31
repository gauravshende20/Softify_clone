import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, catchError, map, of } from 'rxjs';
import { API_BASE, asList } from '../models/api';
import { AutocompleteItem, AutocompleteResponse, SearchResults } from '../models/search';
import { Album, Artist, Genre, Track } from '../models/catalog';
import { Playlist } from '../models/playlist';

@Service()
export class SearchService {
  private readonly http = inject(HttpClient);

  search(q: string): Observable<SearchResults> {
    const params = new HttpParams().set('q', q);
    return this.http.get<Partial<SearchResults>>(`${API_BASE}/search`, { params }).pipe(
      map((res) => normalizeSearch(q, res)),
      catchError(() => of(emptyResults(q))),
    );
  }

  autocomplete(q: string): Observable<AutocompleteItem[]> {
    const params = new HttpParams().set('q', q);
    return this.http
      .get<AutocompleteResponse | AutocompleteItem[]>(`${API_BASE}/search/autocomplete`, { params })
      .pipe(
        map((res) => (Array.isArray(res) ? res : (res.items ?? []))),
        catchError(() => of([] as AutocompleteItem[])),
      );
  }
}

function emptyResults(query: string): SearchResults {
  return { query, tracks: [], artists: [], albums: [], playlists: [], genres: [] };
}

function normalizeSearch(query: string, res: Partial<SearchResults> & Record<string, unknown>): SearchResults {
  return {
    query,
    tracks: asList((res['tracks'] as Track[] | undefined) ?? []),
    artists: asList((res['artists'] as Artist[] | undefined) ?? []),
    albums: asList((res['albums'] as Album[] | undefined) ?? []),
    playlists: asList((res['playlists'] as Playlist[] | undefined) ?? []),
    genres: asList((res['genres'] as Genre[] | undefined) ?? []),
  };
}
