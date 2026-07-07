import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { guestGuard } from './guest.guard';
import { AuthService } from '../services/auth.service';

describe('guestGuard', () => {
  let authService: jasmine.SpyObj<Pick<AuthService, 'isAuthenticated'>>;
  let router: jasmine.SpyObj<Pick<Router, 'createUrlTree'>>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['isAuthenticated']);
    router = jasmine.createSpyObj('Router', ['createUrlTree']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });
  });

  function runGuard() {
    return TestBed.runInInjectionContext(() => guestGuard({} as never, {} as never));
  }

  it('allows guests when not authenticated', () => {
    authService.isAuthenticated.and.returnValue(false);
    expect(runGuard()).toBeTrue();
  });

  it('redirects authenticated users to dashboard', () => {
    authService.isAuthenticated.and.returnValue(true);
    const tree = {} as UrlTree;
    router.createUrlTree.and.returnValue(tree);

    expect(runGuard()).toBe(tree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/dashboard']);
  });
});
