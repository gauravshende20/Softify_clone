import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, hasAnyRole } from '../auth/auth';

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/auth/login']);
  }
  const required = (route.data['roles'] as string[] | undefined) ?? [];
  if (hasAnyRole(auth.roles(), required)) {
    return true;
  }
  return router.createUrlTree(['/']);
};
