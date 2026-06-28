import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { Project, ProjectCreateDTO, ProjectUpdateDTO } from '../models/project.model';
import { API_ENDPOINTS } from '../utils/constants';

/**
 * Project service - handles all project-related API calls
 */
@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found. Please login again.');
    }
    return orgId;
  }

  getProjects(): Observable<Project[]> {
    const orgId = this.getOrgId();
    return this.api.get<Project[]>(API_ENDPOINTS.PROJECTS(orgId));
  }

  getProject(projectId: string): Observable<Project> {
    const orgId = this.getOrgId();
    return this.api.get<Project>(`${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}`);
  }

  createProject(dto: ProjectCreateDTO): Observable<Project> {
    const orgId = this.getOrgId();
    return this.api.post<Project>(API_ENDPOINTS.PROJECTS(orgId), dto);
  }

  updateProject(projectId: string, dto: ProjectUpdateDTO): Observable<Project> {
    const orgId = this.getOrgId();
    return this.api.put<Project>(`${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}`, dto);
  }

  archiveProject(projectId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}`);
  }

  deleteProject(projectId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}/hard`);
  }

  addMember(projectId: string, employeeId: string): Observable<Project> {
    const orgId = this.getOrgId();
    return this.api.post<Project>(
      `${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}/members/${employeeId}`,
      {}
    );
  }

  removeMember(projectId: string, employeeId: string): Observable<Project> {
    const orgId = this.getOrgId();
    return this.api.delete<Project>(
      `${API_ENDPOINTS.PROJECTS(orgId)}/${projectId}/members/${employeeId}`
    );
  }
}
