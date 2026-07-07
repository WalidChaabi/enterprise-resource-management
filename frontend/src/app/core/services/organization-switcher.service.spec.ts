import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { OrganizationSwitcherService } from './organization-switcher.service';
import { ApiService } from './api.service';

describe('OrganizationSwitcherService', () => {
  let service: OrganizationSwitcherService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [OrganizationSwitcherService, ApiService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(OrganizationSwitcherService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists organizations for the current user', () => {
    service.listOrganizations().subscribe((orgs) => {
      expect(orgs.length).toBe(1);
      expect(orgs[0].name).toBe('Acme');
    });

    const request = httpMock.expectOne('http://localhost:8080/api/organizations');
    request.flush([{ id: 'org-1', name: 'Acme' }]);
  });

  it('switches active organization', () => {
    service.switchOrganization('org-2').subscribe((response) => {
      expect(response.activeOrgId).toBe('org-2');
    });

    const request = httpMock.expectOne('http://localhost:8080/api/organizations/switch');
    expect(request.request.body).toEqual({ orgId: 'org-2' });
    request.flush({ activeOrgId: 'org-2', message: 'Switched' });
  });
});
