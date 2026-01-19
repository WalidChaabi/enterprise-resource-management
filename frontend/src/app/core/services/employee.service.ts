import { Injectable, inject } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { Employee, EmployeeCreateDTO, EmployeeUpdateDTO, Page } from '../models/employee.model';
import { API_ENDPOINTS } from '../utils/constants';

/**
 * Employee service - handles all employee-related API calls
 */
@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found. Please login again.');
    }
    return orgId;
  }

  getEmployees(params?: { departmentId?: string; active?: boolean; page?: number; size?: number }): Observable<Page<Employee>> {
    const orgId = this.getOrgId();
    let httpParams = new HttpParams();
    
    if (params?.departmentId) {
      httpParams = httpParams.set('departmentId', params.departmentId);
    }
    if (params?.active !== undefined) {
      httpParams = httpParams.set('active', params.active.toString());
    }
    if (params?.page !== undefined) {
      httpParams = httpParams.set('page', params.page.toString());
    }
    if (params?.size !== undefined) {
      httpParams = httpParams.set('size', params.size.toString());
    }

    return this.api.get<Page<Employee>>(API_ENDPOINTS.EMPLOYEES(orgId), httpParams);
  }

  getEmployee(employeeId: string): Observable<Employee> {
    const orgId = this.getOrgId();
    return this.api.get<Employee>(`${API_ENDPOINTS.EMPLOYEES(orgId)}/${employeeId}`);
  }

  createEmployee(dto: EmployeeCreateDTO): Observable<Employee> {
    const orgId = this.getOrgId();
    return this.api.post<Employee>(API_ENDPOINTS.EMPLOYEES(orgId), dto);
  }

  updateEmployee(employeeId: string, dto: EmployeeUpdateDTO): Observable<Employee> {
    const orgId = this.getOrgId();
    return this.api.put<Employee>(`${API_ENDPOINTS.EMPLOYEES(orgId)}/${employeeId}`, dto);
  }

  deactivateEmployee(employeeId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.EMPLOYEES(orgId)}/${employeeId}`);
  }
}
