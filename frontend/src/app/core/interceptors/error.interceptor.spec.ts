import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { throwError } from 'rxjs';
import { errorInterceptor } from './error.interceptor';
import { AuthService } from '../services/auth.service';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authService: jasmine.SpyObj<Pick<AuthService, 'refreshSession' | 'clearLocalSession'>>;
  let router: jasmine.SpyObj<Pick<Router, 'navigate'>> & { url: string };

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['refreshSession', 'clearLocalSession']);
    authService.refreshSession.and.returnValue(throwError(() => new Error('refresh failed')));
    router = jasmine.createSpyObj('Router', ['navigate']);
    router.url = '/dashboard';

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authService },
        { provide: Router, useValue: router },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('attempts refresh then clears session on 401 when refresh fails', () => {
    let caughtError: HttpErrorResponse | undefined;

    http.get('/api/v1/organizations/1/employees').subscribe({
      error: (error) => {
        caughtError = error;
      },
    });

    httpMock
      .expectOne('/api/v1/organizations/1/employees')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(authService.refreshSession).toHaveBeenCalled();
    expect(authService.clearLocalSession).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/dashboard' },
    });
    expect(caughtError?.status).toBe(401);
  });

  it('rethrows non-401 errors for component handling', () => {
    let caughtError: HttpErrorResponse | undefined;

    http.get('/api/v1/organizations/1/projects').subscribe({
      error: (error) => {
        caughtError = error;
      },
    });

    httpMock
      .expectOne('/api/v1/organizations/1/projects')
      .flush(null, { status: 500, statusText: 'Server Error' });

    expect(authService.refreshSession).not.toHaveBeenCalled();
    expect(authService.clearLocalSession).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
    expect(caughtError?.status).toBe(500);
  });
});
