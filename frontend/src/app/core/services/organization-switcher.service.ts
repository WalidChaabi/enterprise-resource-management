import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { OrganizationSummary, OrganizationSwitchRequest, OrganizationSwitchResponse } from '../models/organization.model';
import { API_ENDPOINTS } from '../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class OrganizationSwitcherService {
  private readonly api = inject(ApiService);

  listOrganizations(): Observable<OrganizationSummary[]> {
    return this.api.get<OrganizationSummary[]>(API_ENDPOINTS.ORGANIZATIONS_SWITCHER);
  }

  switchOrganization(orgId: string): Observable<OrganizationSwitchResponse> {
    const request: OrganizationSwitchRequest = { orgId };
    return this.api.post<OrganizationSwitchResponse>(`${API_ENDPOINTS.ORGANIZATIONS_SWITCHER}/switch`, request);
  }
}
