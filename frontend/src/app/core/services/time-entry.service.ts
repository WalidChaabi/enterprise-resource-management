import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { TimeEntry, TimeEntryCreateDTO, TimeEntryUpdateDTO } from '../models/time-entry.model';
import { API_ENDPOINTS } from '../utils/constants';

/**
 * Time Entry service - handles all time entry-related API calls
 */
@Injectable({
  providedIn: 'root'
})
export class TimeEntryService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found. Please login again.');
    }
    return orgId;
  }

  getTimeEntries(params?: { projectId?: string; employeeId?: string; status?: string; startDate?: string; endDate?: string; page?: number; size?: number }): Observable<any> {
    const orgId = this.getOrgId();
    let httpParams = new HttpParams();
    
    if (params?.projectId) {
      httpParams = httpParams.set('projectId', params.projectId);
    }
    if (params?.employeeId) {
      httpParams = httpParams.set('employeeId', params.employeeId);
    }
    if (params?.status) {
      httpParams = httpParams.set('status', params.status);
    }
    if (params?.startDate) {
      httpParams = httpParams.set('startDate', params.startDate);
    }
    if (params?.endDate) {
      httpParams = httpParams.set('endDate', params.endDate);
    }
    if (params?.page !== undefined) {
      httpParams = httpParams.set('page', params.page.toString());
    }
    if (params?.size !== undefined) {
      httpParams = httpParams.set('size', params.size.toString());
    }

    return this.api.get<TimeEntry[]>(API_ENDPOINTS.TIME_ENTRIES(orgId), httpParams);
  }

  getTimeEntry(timeEntryId: string): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api.get<TimeEntry>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}`);
  }

  createTimeEntry(dto: TimeEntryCreateDTO): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api.post<TimeEntry>(API_ENDPOINTS.TIME_ENTRIES(orgId), dto);
  }

  updateTimeEntry(timeEntryId: string, dto: TimeEntryUpdateDTO): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api.put<TimeEntry>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}`, dto);
  }

  submitTimeEntry(timeEntryId: string): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api.post<TimeEntry>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}/submit`, {});
  }

  approveTimeEntry(timeEntryId: string): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api.post<TimeEntry>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}/approve`, {});
  }

  rejectTimeEntry(timeEntryId: string, reason?: string): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api.post<TimeEntry>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}/reject`, { reason });
  }

  deleteTimeEntry(timeEntryId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}`);
  }
}
