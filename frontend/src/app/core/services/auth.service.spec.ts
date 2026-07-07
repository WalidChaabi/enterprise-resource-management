import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { AuthResponse, LoginResponse } from '../models/auth.model';
import { EntitlementService } from './entitlement.service';
import { ROLES, STORAGE_KEYS } from '../utils/constants';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: jasmine.SpyObj<Pick<Router, 'navigate'>>;
  let entitlementService: jasmine.SpyObj<Pick<EntitlementService, 'load' | 'clear'>>;

  const authResponse: AuthResponse = {
    accessToken: 'access-token',
    tokenType: 'Bearer',
    userId: 'user-1',
    email: 'admin@example.com',
    firstName: 'Admin',
    lastName: 'User',
    organizationId: 'org-1',
    roles: [ROLES.HR_ADMIN, ROLES.ORG_ADMIN],
  };

  const loginComplete: LoginResponse = {
    mfaRequired: false,
    session: authResponse,
  };

  const loginMfaPending: LoginResponse = {
    mfaRequired: true,
    mfaToken: 'mfa-pending-token',
  };

  beforeEach(() => {
    sessionStorage.clear();
    router = jasmine.createSpyObj('Router', ['navigate']);
    entitlementService = jasmine.createSpyObj('EntitlementService', ['load', 'clear']);

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: router },
        { provide: EntitlementService, useValue: entitlementService },
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('logs in with credentials and stores user profile in sessionStorage', () => {
    service.login({ email: 'admin@example.com', password: 'password123' }).subscribe((response) => {
      expect(response.mfaRequired).toBeFalse();
      expect(response.session).toEqual(authResponse);
    });

    const request = httpMock.expectOne('http://localhost:8080/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBeTrue();
    request.flush(loginComplete);

    expect(service.getToken()).toBeNull();
    expect(service.getOrganizationId()).toBe('org-1');
    expect(service.isAuthenticated()).toBeTrue();
    expect(service.getCurrentUser()?.email).toBe('admin@example.com');
    expect(sessionStorage.getItem(STORAGE_KEYS.USER)).toContain('admin@example.com');
    expect(entitlementService.load).toHaveBeenCalled();
  });

  it('returns MFA pending response without storing session', () => {
    service.login({ email: 'admin@example.com', password: 'password123' }).subscribe((response) => {
      expect(response.mfaRequired).toBeTrue();
      expect(response.mfaToken).toBe('mfa-pending-token');
      expect(response.session).toBeUndefined();
    });

    httpMock.expectOne('http://localhost:8080/auth/login').flush(loginMfaPending);

    expect(service.isAuthenticated()).toBeFalse();
    expect(sessionStorage.getItem(STORAGE_KEYS.USER)).toBeNull();
    expect(entitlementService.load).not.toHaveBeenCalled();
  });

  it('verifyMfa completes login and stores session', () => {
    service.verifyMfa({ mfaToken: 'mfa-pending-token', code: '123456' }).subscribe((response) => {
      expect(response).toEqual(authResponse);
    });

    const request = httpMock.expectOne('http://localhost:8080/auth/mfa/verify');
    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBeTrue();
    request.flush(authResponse);

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.getCurrentUser()?.email).toBe('admin@example.com');
  });

  it('clears session data and navigates to login on logout', () => {
    service.login({ email: 'admin@example.com', password: 'password123' }).subscribe();
    httpMock.expectOne('http://localhost:8080/auth/login').flush(loginComplete);

    service.logout();
    httpMock.expectOne('http://localhost:8080/auth/logout').flush(null);

    expect(sessionStorage.getItem(STORAGE_KEYS.USER)).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
    expect(entitlementService.clear).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('checks role membership with hasRole and hasAnyRole', () => {
    service.login({ email: 'admin@example.com', password: 'password123' }).subscribe();
    httpMock.expectOne('http://localhost:8080/auth/login').flush(loginComplete);

    expect(service.hasRole(ROLES.HR_ADMIN)).toBeTrue();
    expect(service.hasRole(ROLES.EMPLOYEE)).toBeFalse();
    expect(service.hasAnyRole(ROLES.EMPLOYEE, ROLES.ORG_ADMIN)).toBeTrue();
    expect(service.hasAnyRole(ROLES.EMPLOYEE, ROLES.PROJECT_MANAGER)).toBeFalse();
  });
});
