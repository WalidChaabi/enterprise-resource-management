import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  MfaBackupCodesResponse,
  MfaEnrollConfirm,
  MfaSetupResponse,
  SecuritySettings,
  SecuritySettingsUpdate,
} from '../models/security.model';

@Injectable({ providedIn: 'root' })
export class SecurityService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = environment.apiUrl;

  getSecuritySettings(): Observable<SecuritySettings> {
    return this.http.get<SecuritySettings>(`${this.apiBase}/api/settings/security`);
  }

  updateSecuritySettings(update: SecuritySettingsUpdate): Observable<SecuritySettings> {
    return this.http.put<SecuritySettings>(`${this.apiBase}/api/settings/security`, update);
  }

  beginMfaSetup(): Observable<MfaSetupResponse> {
    return this.http.post<MfaSetupResponse>(`${this.apiBase}/api/settings/security/mfa/setup`, {});
  }

  confirmMfaSetup(payload: MfaEnrollConfirm): Observable<MfaBackupCodesResponse> {
    return this.http.post<MfaBackupCodesResponse>(`${this.apiBase}/api/settings/security/mfa/confirm`, payload);
  }

  disableMfa(): Observable<void> {
    return this.http.post<void>(`${this.apiBase}/api/settings/security/mfa/disable`, {});
  }

  regenerateBackupCodes(): Observable<MfaBackupCodesResponse> {
    return this.http.post<MfaBackupCodesResponse>(`${this.apiBase}/api/settings/security/mfa/backup-codes`, {});
  }
}
