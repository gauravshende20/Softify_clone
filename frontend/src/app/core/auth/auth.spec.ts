import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth';
import { Account, TokenResponse } from '../models/user';

const tokens = (over: Partial<TokenResponse> = {}): TokenResponse => ({
  accessToken: 'access-1',
  refreshToken: 'refresh-1',
  tokenType: 'Bearer',
  expiresIn: 900,
  userId: 'user-1',
  email: 'ada@harmonia.test',
  ...over,
});

const account: Account = {
  id: 'user-1',
  email: 'ada@harmonia.test',
  enabled: true,
  emailVerified: true,
  roles: ['LISTENER'],
  displayName: 'Ada',
};

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
    localStorage.clear();
  });

  it('stores the access token in memory and the refresh token in sessionStorage', () => {
    service.login({ email: account.email, password: 'Secret12345' }).subscribe();

    const login = http.expectOne('/api/v1/auth/login');
    login.flush(tokens());
    http.expectOne('/api/v1/auth/me').flush(account);

    expect(service.accessToken()).toBe('access-1');
    expect(sessionStorage.getItem('harmonia.refreshToken')).toBe('refresh-1');
    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('harmonia.accessToken')).toBeNull();
    expect(service.currentUser()?.email).toBe(account.email);
    expect(service.isAuthenticated()).toBe(true);
  });

  it('never writes the access token to localStorage on refresh', () => {
    sessionStorage.setItem('harmonia.refreshToken', 'refresh-1');
    service.refreshAccessToken().subscribe();
    http.expectOne('/api/v1/auth/refresh').flush(tokens({ accessToken: 'access-2', refreshToken: 'refresh-2' }));
    expect(service.accessToken()).toBe('access-2');
    expect(sessionStorage.getItem('harmonia.refreshToken')).toBe('refresh-2');
    expect(localStorage.getItem('accessToken')).toBeNull();
  });

  it('shares a single in-flight refresh across queued callers', () => {
    sessionStorage.setItem('harmonia.refreshToken', 'refresh-1');
    const seen: string[] = [];
    service.refreshAccessToken().subscribe((token) => seen.push(token));
    service.refreshAccessToken().subscribe((token) => seen.push(token));
    const reqs = http.match('/api/v1/auth/refresh');
    expect(reqs.length).toBe(1);
    reqs[0].flush(tokens({ accessToken: 'queued-access' }));
    expect(seen).toEqual(['queued-access', 'queued-access']);
  });

  it('treats ROLE_ADMIN and ADMIN as the same role', () => {
    service.login({ email: account.email, password: 'Secret12345' }).subscribe();
    http.expectOne('/api/v1/auth/login').flush(tokens());
    http.expectOne('/api/v1/auth/me').flush({ ...account, roles: ['ROLE_ADMIN'] });
    expect(service.hasRole('ADMIN')).toBe(true);
    expect(service.isAdmin()).toBe(true);
  });
});
