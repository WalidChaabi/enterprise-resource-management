import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../utils/constants';

/**
 * Route guard factory for role-based access control
 * Usage: canActivate: [roleGuard(ROLES.ORG_ADMIN, ROLES.HR_ADMIN)]
 */
export const roleGuard = (...allowedRoles: Role[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login'], { 
        queryParams: { returnUrl: state.url } 
      });
      return false;
    }

    if (authService.hasAnyRole(...allowedRoles)) {
      return true;
    }

    router.navigate(['/dashboard']);
    return false;
  };
};
