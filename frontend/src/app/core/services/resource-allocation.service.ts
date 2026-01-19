import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { ResourceAllocation } from '../models/resource-allocation.model';
import { API_ENDPOINTS } from '../utils/constants';

/**
 * Resource allocation service - handles resource allocation and workload analysis
 */
@Injectable({
  providedIn: 'root'
})
export class ResourceAllocationService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found. Please login again.');
    }
    return orgId;
  }

  /**
   * Get complete resource allocation view
   * Includes employee workload, project staffing, and allocation metrics
   */
  getResourceAllocation(): Observable<ResourceAllocation> {
    const orgId = this.getOrgId();
    return this.api.get<ResourceAllocation>(API_ENDPOINTS.RESOURCE_ALLOCATION(orgId));
  }
}
