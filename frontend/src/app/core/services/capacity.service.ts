import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { CapacityDashboard } from '../models/capacity.model';
import { API_ENDPOINTS } from '../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class CapacityService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  getCapacityDashboard(weekStart?: string): Observable<CapacityDashboard> {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('No organization selected');
    }
    const query = weekStart ? `?weekStart=${weekStart}` : '';
    return this.api.get<CapacityDashboard>(`${API_ENDPOINTS.REPORTS(orgId)}/capacity${query}`);
  }
}
