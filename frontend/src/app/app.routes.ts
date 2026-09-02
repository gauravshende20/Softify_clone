import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { guestGuard } from './core/guards/guest-guard';
import { roleGuard } from './core/guards/role-guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadComponent: () => import('./layout/auth-shell').then((m) => m.AuthShell),
    canActivate: [guestGuard],
    children: [
      {
        path: 'login',
        title: 'Sign in · Harmonia',
        loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
      },
      {
        path: 'register',
        title: 'Create account · Harmonia',
        loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
      },
      { path: '', pathMatch: 'full', redirectTo: 'login' },
    ],
  },
  {
    path: '',
    loadComponent: () => import('./layout/shell').then((m) => m.Shell),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        title: 'Home · Harmonia',
        loadComponent: () => import('./features/home/home').then((m) => m.Home),
      },
      {
        path: 'search',
        title: 'Search · Harmonia',
        loadComponent: () => import('./features/search/search').then((m) => m.Search),
      },
      {
        path: 'browse',
        title: 'Browse · Harmonia',
        loadComponent: () => import('./features/browse/browse').then((m) => m.Browse),
      },
      {
        path: 'browse/genre/:id',
        title: 'Genre · Harmonia',
        loadComponent: () => import('./features/browse/genre-detail').then((m) => m.GenreDetail),
      },
      {
        path: 'artists/:id',
        title: 'Artist · Harmonia',
        loadComponent: () => import('./features/artists/artist-detail').then((m) => m.ArtistDetail),
      },
      {
        path: 'albums/:id',
        title: 'Album · Harmonia',
        loadComponent: () => import('./features/albums/album-detail').then((m) => m.AlbumDetail),
      },
      {
        path: 'tracks/:id',
        title: 'Track · Harmonia',
        loadComponent: () => import('./features/tracks/track-detail').then((m) => m.TrackDetail),
      },
      {
        path: 'playlists/:id',
        title: 'Playlist · Harmonia',
        loadComponent: () => import('./features/playlists/playlist-detail').then((m) => m.PlaylistDetail),
      },
      {
        path: 'library',
        title: 'Library · Harmonia',
        loadComponent: () => import('./features/library/library').then((m) => m.Library),
      },
      {
        path: 'liked-songs',
        title: 'Liked songs · Harmonia',
        loadComponent: () => import('./features/liked-songs/liked-songs').then((m) => m.LikedSongs),
      },
      {
        path: 'recently-played',
        title: 'Recently played · Harmonia',
        loadComponent: () => import('./features/recently-played/recently-played').then((m) => m.RecentlyPlayed),
      },
      {
        path: 'recommendations',
        title: 'Made for you · Harmonia',
        loadComponent: () => import('./features/recommendations/recommendations').then((m) => m.Recommendations),
      },
      {
        path: 'profile',
        title: 'Profile · Harmonia',
        loadComponent: () => import('./features/profile/profile').then((m) => m.Profile),
      },
      {
        path: 'subscription',
        title: 'Premium · Harmonia',
        loadComponent: () => import('./features/subscription/subscription').then((m) => m.SubscriptionPage),
      },
      {
        path: 'subscription/success',
        title: 'Payment received · Harmonia',
        loadComponent: () => import('./features/subscription/subscription').then((m) => m.SubscriptionPage),
      },
      {
        path: 'subscription/cancelled',
        title: 'Checkout cancelled · Harmonia',
        loadComponent: () => import('./features/subscription/subscription').then((m) => m.SubscriptionPage),
      },
      {
        path: 'studio',
        title: 'Artist studio · Harmonia',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ARTIST', 'ARTIST', 'ROLE_ADMIN', 'ADMIN'] },
        loadComponent: () => import('./features/artist-studio/artist-studio').then((m) => m.ArtistStudio),
      },
      {
        path: 'admin',
        title: 'Admin · Harmonia',
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN', 'ADMIN'] },
        loadComponent: () => import('./features/admin/admin').then((m) => m.Admin),
      },
    ],
  },
  { path: '**', redirectTo: '' },
];
