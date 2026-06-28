import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('sends credentials on API requests', () => {
    http.get('/api/v1/organizations/1/employees').subscribe();

    const request = httpMock.expectOne('/api/v1/organizations/1/employees');
    expect(request.request.withCredentials).toBeTrue();
    expect(request.request.headers.has('X-Request-Id')).toBeTrue();
    request.flush({});
  });
});
