import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import {
  WebhookDeliveryPage,
  WebhookEndpoint,
  WebhookEndpointCreated,
  WebhookEndpointRequest,
} from '../models/webhook.model';
import { API_ENDPOINTS } from '../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class WebhookService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private basePath(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('No organization selected');
    }
    return `${API_ENDPOINTS.ORGANIZATIONS(orgId)}/webhooks`;
  }

  listEndpoints(): Observable<WebhookEndpoint[]> {
    return this.api.get<WebhookEndpoint[]>(this.basePath());
  }

  createEndpoint(request: WebhookEndpointRequest): Observable<WebhookEndpointCreated> {
    return this.api.post<WebhookEndpointCreated>(this.basePath(), request);
  }

  updateEndpoint(id: string, request: WebhookEndpointRequest): Observable<WebhookEndpoint> {
    return this.api.put<WebhookEndpoint>(`${this.basePath()}/${id}`, request);
  }

  deleteEndpoint(id: string): Observable<void> {
    return this.api.delete<void>(`${this.basePath()}/${id}`);
  }

  listDeliveries(page = 0, size = 20, endpointId?: string): Observable<WebhookDeliveryPage> {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (endpointId) {
      params.set('endpointId', endpointId);
    }
    return this.api.get<WebhookDeliveryPage>(`${this.basePath()}/deliveries?${params.toString()}`);
  }
}
