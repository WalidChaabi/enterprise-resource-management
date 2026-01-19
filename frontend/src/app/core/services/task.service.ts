import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AuthService } from './auth.service';
import { Task, TaskCreateDTO, TaskUpdateDTO } from '../models/task.model';
import { API_ENDPOINTS } from '../utils/constants';

/**
 * Task service - handles all task-related API calls
 */
@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private readonly api = inject(ApiService);
  private readonly authService = inject(AuthService);

  private getOrgId(): string {
    const orgId = this.authService.getOrganizationId();
    if (!orgId) {
      throw new Error('Organization ID not found. Please login again.');
    }
    return orgId;
  }

  getTasks(projectId: string): Observable<Task[]> {
    const orgId = this.getOrgId();
    return this.api.get<Task[]>(API_ENDPOINTS.TASKS(orgId, projectId));
  }

  getTask(projectId: string, taskId: string): Observable<Task> {
    const orgId = this.getOrgId();
    return this.api.get<Task>(`${API_ENDPOINTS.TASKS(orgId, projectId)}/${taskId}`);
  }

  createTask(projectId: string, dto: TaskCreateDTO): Observable<Task> {
    const orgId = this.getOrgId();
    return this.api.post<Task>(API_ENDPOINTS.TASKS(orgId, projectId), dto);
  }

  updateTask(projectId: string, taskId: string, dto: TaskUpdateDTO): Observable<Task> {
    const orgId = this.getOrgId();
    return this.api.put<Task>(`${API_ENDPOINTS.TASKS(orgId, projectId)}/${taskId}`, dto);
  }

  deleteTask(projectId: string, taskId: string): Observable<void> {
    const orgId = this.getOrgId();
    return this.api.delete<void>(`${API_ENDPOINTS.TASKS(orgId, projectId)}/${taskId}`);
  }
}
