import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  ProfileSettings,
  ProfileSettingsUpdate,
  OrganizationSettings,
  OrganizationSettingsUpdate,
  NotificationPreferences,
  NotificationPreferencesUpdate,
  BillingSettings,
  BillingPortalResponse,
  ShortcutDefinition
} from '../models/settings.model';
import { Role } from '../models/user-role.model';
import { API_ENDPOINTS } from '../utils/constants';

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private readonly api = inject(ApiService);

  getProfile(): Observable<ProfileSettings> {
    return this.api.get<ProfileSettings>(`${API_ENDPOINTS.SETTINGS}/profile`);
  }

  updateProfile(update: ProfileSettingsUpdate): Observable<ProfileSettings> {
    return this.api.put<ProfileSettings>(`${API_ENDPOINTS.SETTINGS}/profile`, update);
  }

  getOrganization(): Observable<OrganizationSettings> {
    return this.api.get<OrganizationSettings>(`${API_ENDPOINTS.SETTINGS}/organization`);
  }

  updateOrganization(update: OrganizationSettingsUpdate): Observable<OrganizationSettings> {
    return this.api.put<OrganizationSettings>(`${API_ENDPOINTS.SETTINGS}/organization`, update);
  }

  getRoles(): Observable<Role[]> {
    return this.api.get<Role[]>(`${API_ENDPOINTS.SETTINGS}/roles`);
  }

  getBilling(): Observable<BillingSettings> {
    return this.api.get<BillingSettings>(`${API_ENDPOINTS.SETTINGS}/billing`);
  }

  createBillingPortalSession(): Observable<BillingPortalResponse> {
    return this.api.post<BillingPortalResponse>(`${API_ENDPOINTS.SETTINGS}/billing/portal`, {});
  }

  createBillingCheckoutSession(): Observable<BillingPortalResponse> {
    return this.api.post<BillingPortalResponse>(`${API_ENDPOINTS.SETTINGS}/billing/checkout`, {});
  }

  getNotificationPreferences(): Observable<NotificationPreferences> {
    return this.api.get<NotificationPreferences>(`${API_ENDPOINTS.SETTINGS}/notification-preferences`);
  }

  updateNotificationPreferences(update: NotificationPreferencesUpdate): Observable<NotificationPreferences> {
    return this.api.put<NotificationPreferences>(`${API_ENDPOINTS.SETTINGS}/notification-preferences`, update);
  }

  getShortcuts(): Observable<ShortcutDefinition[]> {
    return this.api.get<ShortcutDefinition[]>(`${API_ENDPOINTS.SETTINGS}/shortcuts`);
  }
}
