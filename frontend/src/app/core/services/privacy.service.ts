import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { ErasureRequest } from '../models/privacy.model';
import { API_ENDPOINTS } from '../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class PrivacyService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  exportPersonalData(): Observable<Blob> {
    return this.api.postBlob('/api/v1/users/me/data-export', {});
  }

  requestErasure(): Observable<ErasureRequest> {
    return this.api.post<ErasureRequest>('/api/v1/users/me/erasure-request', {});
  }

  getErasureRequest(): Observable<ErasureRequest | null> {
    return this.api.getOptional<ErasureRequest>('/api/v1/users/me/erasure-request');
  }

  listPendingErasureRequests(): Observable<ErasureRequest[]> {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('No organization selected');
    }
    return this.api.get<ErasureRequest[]>(
      `${API_ENDPOINTS.ORGANIZATIONS(orgId)}/privacy/erasure-requests`
    );
  }

  approveErasureRequest(requestId: string, adminNotes?: string): Observable<ErasureRequest> {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('No organization selected');
    }
    return this.api.post<ErasureRequest>(
      `${API_ENDPOINTS.ORGANIZATIONS(orgId)}/privacy/erasure-requests/${requestId}/approve`,
      { adminNotes: adminNotes ?? null }
    );
  }

  rejectErasureRequest(requestId: string, adminNotes?: string): Observable<ErasureRequest> {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('No organization selected');
    }
    return this.api.post<ErasureRequest>(
      `${API_ENDPOINTS.ORGANIZATIONS(orgId)}/privacy/erasure-requests/${requestId}/reject`,
      { adminNotes: adminNotes ?? null }
    );
  }
}
