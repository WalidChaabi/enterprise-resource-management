import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { UserRole, UserRoleAssignDTO, Role } from '../models/user-role.model';
import { API_ENDPOINTS } from '../utils/constants';

/**
 * User Role service - handles role assignment to users/employees
 */
@Injectable({
  providedIn: 'root'
})
export class UserRoleService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found. Please login again.');
    }
    return orgId;
  }

  getUserRoles(userId: string): Observable<UserRole[]> {
    const orgId = this.getOrgId();
    return this.api.get<UserRole[]>(`${API_ENDPOINTS.ORGANIZATIONS(orgId)}/users/${userId}/roles`);
  }

  getEmployeeRoles(employeeId: string): Observable<UserRole[]> {
    const orgId = this.getOrgId();
    return this.api.get<UserRole[]>(`${API_ENDPOINTS.EMPLOYEES(orgId)}/${employeeId}/roles`);
  }

  getAvailableRoles(): Observable<Role[]> {
    const orgId = this.getOrgId();
    return this.api.get<Role[]>(API_ENDPOINTS.ROLES(orgId));
  }

  assignRole(dto: UserRoleAssignDTO): Observable<UserRole> {
    const orgId = this.getOrgId();
    return this.api.post<UserRole>(`${API_ENDPOINTS.ORGANIZATIONS(orgId)}/users/${dto.userId}/roles`, { roleId: dto.roleId });
  }

  assignRoleToEmployee(employeeId: string, roleId: string): Observable<UserRole> {
    const orgId = this.getOrgId();
    return this.api.post<UserRole>(`${API_ENDPOINTS.EMPLOYEES(orgId)}/${employeeId}/roles`, { roleId });
  }

  removeRole(userId: string, roleId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.ORGANIZATIONS(orgId)}/users/${userId}/roles/${roleId}`);
  }

  removeRoleFromEmployee(employeeId: string, roleId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.EMPLOYEES(orgId)}/${employeeId}/roles/${roleId}`);
  }
}
