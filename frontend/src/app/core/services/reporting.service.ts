import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { Dashboard } from '../models/dashboard.model';
import { LeaveBalance, LeaveReport, TimeReport } from '../models/report.model';
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

  private dashboard$?: Observable<Dashboard>;
  private cachedOrgId?: string;

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found. Please login again.');
    }
    return orgId;
  }

  private reportsBase(): string {
    return API_ENDPOINTS.REPORTS(this.getOrgId());
  }

  private dateParams(startDate?: string, endDate?: string): string {
    const params = new URLSearchParams();
    if (startDate) {
      params.set('startDate', startDate);
    }
    if (endDate) {
      params.set('endDate', endDate);
    }
    const query = params.toString();
    return query ? `?${query}` : '';
  }

  /**
   * Get dashboard data with statistics and alerts.
   * Shared across dashboard page and sidebar indicators.
   */
  getDashboard(): Observable<Dashboard> {
    const orgId = this.getOrgId();
    if (!this.dashboard$ || this.cachedOrgId !== orgId) {
      this.cachedOrgId = orgId;
      this.dashboard$ = this.api
        .get<Dashboard>(`${this.reportsBase()}/dashboard`)
        .pipe(shareReplay({ bufferSize: 1, refCount: true }));
    }
    return this.dashboard$;
  }

  getTimeReportByProject(startDate?: string, endDate?: string): Observable<TimeReport[]> {
    return this.api.get<TimeReport[]>(
      `${this.reportsBase()}/time/by-project${this.dateParams(startDate, endDate)}`
    );
  }

  getTimeReportByEmployee(startDate?: string, endDate?: string): Observable<TimeReport[]> {
    return this.api.get<TimeReport[]>(
      `${this.reportsBase()}/time/by-employee${this.dateParams(startDate, endDate)}`
    );
  }

  getTimeSummary(startDate?: string, endDate?: string): Observable<TimeReport> {
    return this.api.get<TimeReport>(
      `${this.reportsBase()}/time/summary${this.dateParams(startDate, endDate)}`
    );
  }

  getLeaveBalance(): Observable<LeaveBalance[]> {
    return this.api.get<LeaveBalance[]>(`${this.reportsBase()}/leave/balance`);
  }

  getUpcomingLeave(): Observable<LeaveReport[]> {
    return this.api.get<LeaveReport[]>(`${this.reportsBase()}/leave/upcoming`);
  }

  invalidateDashboard(): void {
    this.dashboard$ = undefined;
    this.cachedOrgId = undefined;
  }
}
