import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { LeaveRequest, LeaveRequestCreateDTO, LeaveRequestUpdateDTO } from '../models/leave-request.model';
import { API_ENDPOINTS } from '../utils/constants';

/**
 * Leave Request service - handles all leave request-related API calls
 */
@Injectable({
  providedIn: 'root'
})
export class LeaveRequestService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found. Please login again.');
    }
    return orgId;
  }

  getLeaveRequests(params?: { employeeId?: string; status?: string; startDate?: string; endDate?: string; page?: number; size?: number }): Observable<any> {
    const orgId = this.getOrgId();
    let httpParams = new HttpParams();
    
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

    return this.api.get<LeaveRequest[]>(API_ENDPOINTS.LEAVE_REQUESTS(orgId), httpParams);
  }

  getLeaveRequest(leaveRequestId: string): Observable<LeaveRequest> {
    const orgId = this.getOrgId();
    return this.api.get<LeaveRequest>(`${API_ENDPOINTS.LEAVE_REQUESTS(orgId)}/${leaveRequestId}`);
  }

  createLeaveRequest(dto: LeaveRequestCreateDTO): Observable<LeaveRequest> {
    const orgId = this.getOrgId();
    return this.api.post<LeaveRequest>(API_ENDPOINTS.LEAVE_REQUESTS(orgId), dto);
  }

  updateLeaveRequest(leaveRequestId: string, dto: LeaveRequestUpdateDTO): Observable<LeaveRequest> {
    const orgId = this.getOrgId();
    return this.api.put<LeaveRequest>(`${API_ENDPOINTS.LEAVE_REQUESTS(orgId)}/${leaveRequestId}`, dto);
  }

  approveLeaveRequest(leaveRequestId: string): Observable<LeaveRequest> {
    const orgId = this.getOrgId();
    return this.api.post<LeaveRequest>(`${API_ENDPOINTS.LEAVE_REQUESTS(orgId)}/${leaveRequestId}/approve`, {});
  }

  rejectLeaveRequest(leaveRequestId: string, reason?: string): Observable<LeaveRequest> {
    const orgId = this.getOrgId();
    return this.api.post<LeaveRequest>(`${API_ENDPOINTS.LEAVE_REQUESTS(orgId)}/${leaveRequestId}/reject`, { reason });
  }

  cancelLeaveRequest(leaveRequestId: string): Observable<LeaveRequest> {
    const orgId = this.getOrgId();
    return this.api.post<LeaveRequest>(`${API_ENDPOINTS.LEAVE_REQUESTS(orgId)}/${leaveRequestId}/cancel`, {});
  }

  deleteLeaveRequest(leaveRequestId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.LEAVE_REQUESTS(orgId)}/${leaveRequestId}`);
  }
}
