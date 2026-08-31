import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../auth/auth';

const ANON_PATHS = [
  '/api/v1/auth/login',
  '/api/v1/auth/register',
  '/api/v1/auth/refresh',
  '/api/v1/auth/forgot-password',
  '/api/v1/auth/reset-password',
  '/api/v1/auth/verify-email',
];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const skipAuth = ANON_PATHS.some((path) => req.url.includes(path));
  const token = auth.accessToken();
  const authReq =
    !skipAuth && token
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || skipAuth || req.url.includes('/api/v1/auth/refresh')) {
        return throwError(() => error);
      }
      return auth.refreshAccessToken().pipe(
        switchMap((accessToken) =>
          next(
            req.clone({
              setHeaders: { Authorization: `Bearer ${accessToken}` },
            }),
          ),
        ),
        catchError((refreshError) => {
          auth.forceLogout();
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};
