import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { Department, DepartmentCreateDTO, DepartmentUpdateDTO } from '../models/department.model';
import { API_ENDPOINTS } from '../utils/constants';

/**
 * Department service - handles all department-related API calls
 */
@Injectable({
  providedIn: 'root'
})
export class DepartmentService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found. Please login again.');
    }
    return orgId;
  }

  getDepartments(): Observable<Department[]> {
    const orgId = this.getOrgId();
    return this.api.get<Department[]>(API_ENDPOINTS.DEPARTMENTS(orgId));
  }

  getDepartment(departmentId: string): Observable<Department> {
    const orgId = this.getOrgId();
    return this.api.get<Department>(`${API_ENDPOINTS.DEPARTMENTS(orgId)}/${departmentId}`);
  }

  createDepartment(dto: DepartmentCreateDTO): Observable<Department> {
    const orgId = this.getOrgId();
    return this.api.post<Department>(API_ENDPOINTS.DEPARTMENTS(orgId), dto);
  }

  updateDepartment(departmentId: string, dto: DepartmentUpdateDTO): Observable<Department> {
    const orgId = this.getOrgId();
    return this.api.put<Department>(`${API_ENDPOINTS.DEPARTMENTS(orgId)}/${departmentId}`, dto);
  }

  deleteDepartment(departmentId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.DEPARTMENTS(orgId)}/${departmentId}`);
  }
}
