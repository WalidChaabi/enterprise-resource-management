import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EntitlementService } from './entitlement.service';
import { SettingsService } from './settings.service';

describe('EntitlementService', () => {
  let service: EntitlementService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EntitlementService, SettingsService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EntitlementService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('loads entitlements into the signal', () => {
    service.load();
    const request = httpMock.expectOne('http://localhost:8080/api/settings/entitlements');
    request.flush({
      planCode: 'STARTER',
      planName: 'Starter',
      entitlements: ['reports.read'],
      seatCount: 1,
      upgradeRequired: false,
    });

    expect(service.entitlements()?.entitlements).toEqual(['reports.read']);
    expect(service.has('reports.read')).toBeTrue();
    expect(service.has('billing.manage')).toBeFalse();
  });

  it('clears entitlements', () => {
    service.entitlements.set({
      planCode: 'STARTER',
      planName: 'Starter',
      entitlements: ['reports.read'],
      seatCount: 1,
      upgradeRequired: false,
    });
    service.clear();
    expect(service.entitlements()).toBeNull();
  });
});
