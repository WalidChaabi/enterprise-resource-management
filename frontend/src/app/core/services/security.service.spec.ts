import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SecurityService } from './security.service';

describe('SecurityService', () => {
  let service: SecurityService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SecurityService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SecurityService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads security settings', () => {
    service.getSecuritySettings().subscribe((settings) => {
      expect(settings.requireMfa).toBeFalse();
    });

    const request = httpMock.expectOne('http://localhost:8080/api/settings/security');
    expect(request.request.method).toBe('GET');
    request.flush({
      ssoEnabled: false,
      requireMfa: false,
      userMfaEnabled: false,
      unusedBackupCodes: 0,
      availableSsoProviders: [],
    });
  });

  it('starts MFA enrollment', () => {
    service.beginMfaSetup().subscribe((response) => {
      expect(response.secret).toBe('SECRET');
    });

    const request = httpMock.expectOne('http://localhost:8080/api/settings/security/mfa/setup');
    expect(request.request.method).toBe('POST');
    request.flush({
      secret: 'SECRET',
      qrCodeUri: 'otpauth://totp/test',
      issuer: 'ERM',
      accountName: 'user@example.com',
    });
  });
});
