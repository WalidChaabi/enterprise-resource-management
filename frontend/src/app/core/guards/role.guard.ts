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

    // For development: Allow all authenticated users to access pages
    // TODO: Remove this in production and uncomment the role check below
    const user = authService.getCurrentUser();
    if (user) {
      console.warn(`[DEV MODE] Role guard bypassed. User roles: ${user.roles?.join(', ') || 'none'}. Required: ${allowedRoles.join(', ')}`);
      return true;
    }

    // Production role check (currently disabled for development)
    // if (authService.hasAnyRole(...allowedRoles)) {
    //   return true;
    // }

    // If no user or role check fails, redirect to dashboard
    console.warn(`Access denied to ${state.url}. Required roles: ${allowedRoles.join(', ')}`);
    router.navigate(['/dashboard']);
    return false;
  };
};
