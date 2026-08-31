import { HttpClient } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import {
  Observable,
  catchError,
  finalize,
  map,
  of,
  shareReplay,
  switchMap,
  tap,
  throwError,
} from 'rxjs';
import { API_BASE } from '../models/api';
import { Account, LoginRequest, RegisterRequest, TokenResponse, UserRole } from '../models/user';

const REFRESH_STORAGE_KEY = 'harmonia.refreshToken';

@Service()
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly accessTokenSignal = signal<string | null>(null);
  private readonly currentUserSignal = signal<Account | null>(null);
  private refreshInFlight$: Observable<string> | null = null;

  readonly accessToken = this.accessTokenSignal.asReadonly();
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.accessTokenSignal() && !!this.currentUserSignal());
  readonly roles = computed(() => this.currentUserSignal()?.roles ?? []);
  readonly displayName = computed(() => {
    const user = this.currentUserSignal();
    if (!user) {
      return '';
    }
    return user.displayName || user.email.split('@')[0] || 'Listener';
  });
  readonly isAdmin = computed(() => this.hasRole('ADMIN'));
  readonly isArtist = computed(() => this.hasRole('ARTIST') || this.hasRole('ADMIN'));

  hasRole(role: string): boolean {
    const wanted = normalizeRole(role);
    return this.roles().some((r) => normalizeRole(r) === wanted);
  }

  login(request: LoginRequest): Observable<Account> {
    return this.http.post<TokenResponse>(`${API_BASE}/auth/login`, request).pipe(
      tap((tokens) => this.acceptTokens(tokens)),
      switchMap(() => this.loadMe()),
    );
  }

  register(request: RegisterRequest): Observable<Account> {
    return this.http.post<Account>(`${API_BASE}/auth/register`, request).pipe(
      switchMap(() => this.login({ email: request.email, password: request.password })),
    );
  }

  logout(): void {
    this.http.post(`${API_BASE}/auth/logout`, {}).pipe(catchError(() => of(null))).subscribe();
    this.clearSession();
    void this.router.navigate(['/auth/login']);
  }

  forceLogout(): void {
    this.clearSession();
    if (!this.router.url.startsWith('/auth/')) {
      void this.router.navigate(['/auth/login']);
    }
  }

  restoreSession(): Observable<Account | null> {
    const refreshToken = sessionStorage.getItem(REFRESH_STORAGE_KEY);
    if (!refreshToken) {
      return of(null);
    }
    return this.refreshAccessToken().pipe(
      switchMap(() => this.loadMe()),
      catchError(() => {
        this.clearSession();
        return of(null);
      }),
    );
  }

  refreshAccessToken(): Observable<string> {
    if (this.refreshInFlight$) {
      return this.refreshInFlight$;
    }
    const refreshToken = sessionStorage.getItem(REFRESH_STORAGE_KEY);
    if (!refreshToken) {
      return throwError(() => new Error('Missing refresh token'));
    }
    this.refreshInFlight$ = this.http
      .post<TokenResponse>(`${API_BASE}/auth/refresh`, { refreshToken })
      .pipe(
        tap((tokens) => this.acceptTokens(tokens)),
        map((tokens) => tokens.accessToken),
        finalize(() => {
          this.refreshInFlight$ = null;
        }),
        shareReplay({ bufferSize: 1, refCount: true }),
      );
    return this.refreshInFlight$;
  }

  patchCurrentUser(partial: Partial<Account>): void {
    const current = this.currentUserSignal();
    if (!current) {
      return;
    }
    this.currentUserSignal.set({ ...current, ...partial });
  }

  private loadMe(): Observable<Account> {
    return this.http.get<Account>(`${API_BASE}/auth/me`).pipe(
      tap((account) => this.currentUserSignal.set(account)),
    );
  }

  private acceptTokens(tokens: TokenResponse): void {
    this.accessTokenSignal.set(tokens.accessToken);
    if (tokens.refreshToken) {
      sessionStorage.setItem(REFRESH_STORAGE_KEY, tokens.refreshToken);
    }
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('harmonia.accessToken');
    }
  }

  private clearSession(): void {
    this.accessTokenSignal.set(null);
    this.currentUserSignal.set(null);
    sessionStorage.removeItem(REFRESH_STORAGE_KEY);
  }
}

export function normalizeRole(role: string): string {
  return role.replace(/^ROLE_/, '').toUpperCase();
}

export function hasAnyRole(userRoles: UserRole[] | undefined, required: string[]): boolean {
  if (!required.length) {
    return true;
  }
  const owned = new Set((userRoles ?? []).map(normalizeRole));
  return required.some((role) => owned.has(normalizeRole(role)));
}
