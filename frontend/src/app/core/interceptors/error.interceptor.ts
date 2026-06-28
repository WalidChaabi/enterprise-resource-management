import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Handles 401 responses by attempting a cookie-based refresh once.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const isAuthEndpoint = req.url.includes('/auth/login')
        || req.url.includes('/auth/refresh')
        || req.url.includes('/auth/logout');

      if (error.status === 401 && !isAuthEndpoint && !req.headers.has('X-Auth-Retry')) {
        return authService.refreshSession().pipe(
          switchMap(() => next(req.clone({
            withCredentials: true,
            setHeaders: { 'X-Auth-Retry': '1' }
          }))),
          catchError(() => {
            authService.clearLocalSession();
            router.navigate(['/login'], {
              queryParams: { returnUrl: router.url }
            });
            return throwError(() => error);
          })
        );
      }

      if (error.status === 401 && !isAuthEndpoint) {
        authService.clearLocalSession();
        router.navigate(['/login'], {
          queryParams: { returnUrl: router.url }
        });
      }

      return throwError(() => error);
    })
  );
};
