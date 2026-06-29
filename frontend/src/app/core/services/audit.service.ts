import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { AuditEventPage } from '../models/audit.model';
import { API_ENDPOINTS } from '../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  listEvents(page = 0, size = 20, action?: string, entityType?: string): Observable<AuditEventPage> {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('No organization selected');
    }
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (action) params.set('action', action);
    if (entityType) params.set('entityType', entityType);
    return this.api.get<AuditEventPage>(
      `${API_ENDPOINTS.ORGANIZATIONS(orgId)}/audit-events?${params.toString()}`
    );
  }

  exportCsv(action?: string, entityType?: string): Observable<Blob> {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('No organization selected');
    }
    const params = new URLSearchParams();
    if (action) params.set('action', action);
    if (entityType) params.set('entityType', entityType);
    const query = params.toString();
    const suffix = query ? `?${query}` : '';
    return this.api.getBlob(`${API_ENDPOINTS.ORGANIZATIONS(orgId)}/audit-events/export${suffix}`);
  }
}
