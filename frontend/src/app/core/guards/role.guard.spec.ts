import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';
import { ROLES } from '../utils/constants';

describe('roleGuard', () => {
  let authService: jasmine.SpyObj<Pick<AuthService, 'hasAnyRole'>> & {
    isAuthenticated: jasmine.Spy<() => boolean>;
  };
  let router: jasmine.SpyObj<Pick<Router, 'navigate'>>;
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/protected' } as RouterStateSnapshot;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['hasAnyRole']);
    authService.isAuthenticated = jasmine.createSpy('isAuthenticated');
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });
  });

  function runGuard(...roles: Parameters<typeof roleGuard>) {
    const guard = roleGuard(...roles);
    return TestBed.runInInjectionContext(() => guard(route, state));
  }

  it('redirects unauthenticated users to login', () => {
    authService.isAuthenticated.and.returnValue(false);

    expect(runGuard(ROLES.HR_ADMIN)).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/protected' },
    });
  });

  it('allows access when the user has an allowed role', () => {
    authService.isAuthenticated.and.returnValue(true);
    authService.hasAnyRole.and.returnValue(true);

    expect(runGuard(ROLES.HR_ADMIN, ROLES.ORG_ADMIN)).toBeTrue();
    expect(authService.hasAnyRole).toHaveBeenCalledWith(ROLES.HR_ADMIN, ROLES.ORG_ADMIN);
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('redirects authenticated users without permission to dashboard', () => {
    authService.isAuthenticated.and.returnValue(true);
    authService.hasAnyRole.and.returnValue(false);

    expect(runGuard(ROLES.ORG_ADMIN)).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });
});
