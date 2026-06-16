import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './services/auth.service';

export const authGuard: CanActivateFn = (): UrlTree | boolean => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.authenticated() || router.createUrlTree(['/login']);
};