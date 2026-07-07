import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let authService: jasmine.SpyObj<Pick<AuthService, 'isAuthenticated'>>;
  let router: jasmine.SpyObj<Pick<Router, 'navigate'>>;
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/employees' } as RouterStateSnapshot;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['isAuthenticated']);
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });
  });

  function runGuard() {
    return TestBed.runInInjectionContext(() => authGuard(route, state));
  }

  it('allows authenticated users', () => {
    authService.isAuthenticated.and.returnValue(true);
    expect(runGuard()).toBeTrue();
  });

  it('redirects unauthenticated users to login with return URL', () => {
    authService.isAuthenticated.and.returnValue(false);
    expect(runGuard()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/employees' },
    });
  });
});
