import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApiService } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ApiService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('performs GET requests against the API base URL', () => {
    service.get<{ ok: boolean }>('/api/settings/profile').subscribe((response) => {
      expect(response.ok).toBeTrue();
    });

    const request = httpMock.expectOne('http://localhost:8080/api/settings/profile');
    expect(request.request.method).toBe('GET');
    request.flush({ ok: true });
  });

  it('maps API errors to ErrorResponse shape', () => {
    let capturedCode = '';
    service.get('/api/settings/profile').subscribe({
      error: (error) => {
        capturedCode = error.code;
      },
    });

    httpMock.expectOne('http://localhost:8080/api/settings/profile').flush(
      { code: 'AUTH_ERROR', details: ['Unauthorized'] },
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(capturedCode).toBe('AUTH_ERROR');
  });

  it('wraps client-side errors', () => {
    let capturedCode = '';
    service.get('/api/settings/profile').subscribe({
      error: (error) => {
        capturedCode = error.code;
      },
    });

    const request = httpMock.expectOne('http://localhost:8080/api/settings/profile');
    request.error(new ErrorEvent('error', { message: 'Network down' }));

    expect(capturedCode).toBe('CLIENT_ERROR');
  });
});
