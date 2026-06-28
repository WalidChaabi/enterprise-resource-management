import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { AuthResponse } from '../models/auth.model';
import { ROLES, STORAGE_KEYS } from '../utils/constants';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: jasmine.SpyObj<Pick<Router, 'navigate'>>;

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

  beforeEach(() => {
    sessionStorage.clear();
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: router },
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
      expect(response).toEqual(authResponse);
    });

    const request = httpMock.expectOne('http://localhost:8080/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBeTrue();
    request.flush(authResponse);

    expect(service.getToken()).toBeNull();
    expect(service.getOrganizationId()).toBe('org-1');
    expect(service.isAuthenticated()).toBeTrue();
    expect(service.getCurrentUser()?.email).toBe('admin@example.com');
    expect(sessionStorage.getItem(STORAGE_KEYS.USER)).toContain('admin@example.com');
  });

  it('clears session data and navigates to login on logout', () => {
    service.login({ email: 'admin@example.com', password: 'password123' }).subscribe();
    httpMock.expectOne('http://localhost:8080/auth/login').flush(authResponse);

    service.logout();
    httpMock.expectOne('http://localhost:8080/auth/logout').flush(null);

    expect(sessionStorage.getItem(STORAGE_KEYS.USER)).toBeNull();
    expect(service.isAuthenticated()).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('checks role membership with hasRole and hasAnyRole', () => {
    service.login({ email: 'admin@example.com', password: 'password123' }).subscribe();
    httpMock.expectOne('http://localhost:8080/auth/login').flush(authResponse);

    expect(service.hasRole(ROLES.HR_ADMIN)).toBeTrue();
    expect(service.hasRole(ROLES.EMPLOYEE)).toBeFalse();
    expect(service.hasAnyRole(ROLES.EMPLOYEE, ROLES.ORG_ADMIN)).toBeTrue();
    expect(service.hasAnyRole(ROLES.EMPLOYEE, ROLES.PROJECT_MANAGER)).toBeFalse();
  });
});
