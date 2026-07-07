import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { TimeEntry, TimeEntryCreateDTO, TimeEntryStatus, TimeEntryUpdateDTO } from '../models/time-entry.model';
import { API_ENDPOINTS } from '../utils/constants';

function formatTime(value: unknown): string | undefined {
  if (value == null || value === '') return undefined;
  return String(value).slice(0, 5);
}

function mapTimeEntry(raw: Record<string, unknown>): TimeEntry {
  const entryDate = String(raw['entryDate'] ?? raw['date'] ?? '');
  const hours = Number(raw['durationHours'] ?? raw['hours'] ?? 0);

  return {
    id: String(raw['id'] ?? ''),
    projectId: raw['projectId'] != null ? String(raw['projectId']) : '',
    projectName: raw['projectName'] as string | undefined,
    taskId: raw['taskId'] != null ? String(raw['taskId']) : undefined,
    taskTitle: raw['taskTitle'] as string | undefined,
    employeeId: String(raw['employeeId'] ?? ''),
    employeeName: raw['employeeName'] as string | undefined,
    date: entryDate,
    entryDate,
    startTime: formatTime(raw['startTime']),
    endTime: formatTime(raw['endTime']),
    hours,
    durationHours: hours,
    description: raw['description'] as string | undefined,
    status: raw['status'] as TimeEntryStatus,
    approvedById: raw['approvedById'] != null ? String(raw['approvedById']) : undefined,
    approvedByName: raw['approvedByName'] as string | undefined,
    rejectedReason: (raw['rejectionReason'] ?? raw['rejectedReason']) as string | undefined,
    createdAt: raw['createdAt'] as string | undefined,
    updatedAt: raw['updatedAt'] as string | undefined,
  };
}

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

  getTimeEntries(params?: {
    projectId?: string;
    employeeId?: string;
    status?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
  }): Observable<TimeEntry[]> {
    const orgId = this.getOrgId();
    let httpParams = new HttpParams();

    if (params?.projectId) httpParams = httpParams.set('projectId', params.projectId);
    if (params?.employeeId) httpParams = httpParams.set('employeeId', params.employeeId);
    if (params?.status) httpParams = httpParams.set('status', params.status);
    if (params?.startDate) httpParams = httpParams.set('startDate', params.startDate);
    if (params?.endDate) httpParams = httpParams.set('endDate', params.endDate);
    if (params?.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size !== undefined) httpParams = httpParams.set('size', params.size.toString());

    return this.api.get<unknown>(API_ENDPOINTS.TIME_ENTRIES(orgId), httpParams).pipe(
      map((response) => {
        const rows = Array.isArray(response)
          ? response
          : ((response as { content?: unknown[] })?.content ?? []);
        return rows.map((row) => mapTimeEntry(row as Record<string, unknown>));
      })
    );
  }

  getTimeEntry(timeEntryId: string): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api
      .get<Record<string, unknown>>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}`)
      .pipe(map(mapTimeEntry));
  }

  createTimeEntry(dto: TimeEntryCreateDTO): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api
      .post<Record<string, unknown>>(API_ENDPOINTS.TIME_ENTRIES(orgId), dto)
      .pipe(map(mapTimeEntry));
  }

  updateTimeEntry(timeEntryId: string, dto: TimeEntryUpdateDTO): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api
      .put<Record<string, unknown>>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}`, dto)
      .pipe(map(mapTimeEntry));
  }

  submitTimeEntry(timeEntryId: string): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api
      .post<Record<string, unknown>>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}/submit`, {})
      .pipe(map(mapTimeEntry));
  }

  approveTimeEntry(timeEntryId: string): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api
      .post<Record<string, unknown>>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}/approve`, {})
      .pipe(map(mapTimeEntry));
  }

  rejectTimeEntry(timeEntryId: string, reason?: string): Observable<TimeEntry> {
    const orgId = this.getOrgId();
    return this.api
      .post<Record<string, unknown>>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}/reject`, { reason })
      .pipe(map(mapTimeEntry));
  }

  deleteTimeEntry(timeEntryId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.TIME_ENTRIES(orgId)}/${timeEntryId}`);
  }
}
