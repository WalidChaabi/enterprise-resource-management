import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { Dashboard } from '../models/dashboard.model';
import { API_ENDPOINTS } from '../utils/constants';

/**
 * Reporting service - handles dashboard and reporting API calls
 */
@Injectable({
  providedIn: 'root'
})
export class ReportingService {
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
   * Get dashboard data with statistics and alerts
   */
  getDashboard(): Observable<Dashboard> {
    const orgId = this.getOrgId();
    return this.api.get<Dashboard>(`${API_ENDPOINTS.REPORTS(orgId)}/dashboard`);
  }
}
