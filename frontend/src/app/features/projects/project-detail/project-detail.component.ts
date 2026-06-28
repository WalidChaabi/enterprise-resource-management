import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ProjectService } from '../../../core/services/project.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { Project, ProjectStatus } from '../../../core/models/project.model';
import { Employee } from '../../../core/models/employee.model';
import { TranslationService } from '../../../core/services/translation.service';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { BadgeComponent, BadgeVariant } from '../../../shared/components/ui/badge/badge.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { TaskListComponent } from '../task-list/task-list.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { ProjectFormComponent } from '../project-form/project-form.component';

@Component({
    selector: 'app-project-detail',
    imports: [CommonModule, RouterModule, TranslatePipe, ButtonComponent, CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent, BadgeComponent, AlertComponent, TaskListComponent, DialogComponent, ProjectFormComponent],
    templateUrl: './project-detail.component.html'
})
export class ProjectDetailComponent implements OnInit {
  private readonly projectService = inject(ProjectService);
  private readonly employeeService = inject(EmployeeService);
  private readonly route = inject(ActivatedRoute);
  private readonly translationService = inject(TranslationService);
  private readonly toastService = inject(ToastService);

  readonly project = signal<Project | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly showAddMemberModal = signal<boolean>(false);
  readonly showEditModal = signal<boolean>(false);
  readonly employees = signal<Employee[]>([]);
  readonly loadingEmployees = signal<boolean>(false);
  readonly selectedEmployeeId = signal<string>('');
  readonly addingMember = signal<boolean>(false);

  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('id');
    if (projectId) {
      this.loadProject(projectId);
    }
  }

  loadProject(projectId: string): void {
    this.loading.set(true);
    this.error.set('');

    this.projectService.getProject(projectId).subscribe({
      next: (project) => {
        this.project.set(project);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to load project');
        this.loading.set(false);
      }
    });
  }

  formatBudget(budget: number | undefined): string {
    if (budget != null) {
      return '$' + budget.toLocaleString();
    }
    return this.translationService.translate('common.notSet');
  }

  getStatusVariant(status: ProjectStatus): BadgeVariant {
    switch (status) {
      case ProjectStatus.ACTIVE:
        return 'default';
      case ProjectStatus.COMPLETED:
        return 'secondary';
      case ProjectStatus.CANCELLED:
      case ProjectStatus.ON_HOLD:
        return 'destructive';
      default:
        return 'outline';
    }
  }

  openAddMemberModal(): void {
    this.showAddMemberModal.set(true);
    this.loadEmployees();
  }

  closeAddMemberModal(): void {
    this.showAddMemberModal.set(false);
    this.selectedEmployeeId.set('');
  }

  loadEmployees(): void {
    this.loadingEmployees.set(true);
    this.employeeService.getEmployees({ active: true, size: 1000 }).subscribe({
      next: (response) => {
        const employees = Array.isArray(response) ? response : (response.content || []);
        this.employees.set(employees);
        this.loadingEmployees.set(false);
      },
      error: (err) => {
        console.error('Error loading employees:', err);
        this.toastService.error('Failed to load employees', 'Could not load the list of employees.');
        this.loadingEmployees.set(false);
      }
    });
  }

  onAddMember(): void {
    const employeeId = this.selectedEmployeeId();
    const projectId = this.project()?.id;

    if (!employeeId || !projectId) {
      return;
    }

    this.addingMember.set(true);
    const extractErrorMessage = (err: unknown): string => {
      if (err && typeof err === 'object') {
        const error = err as { details?: string[]; message?: string; error?: { details?: string[]; message?: string } };
        if (error.details && Array.isArray(error.details) && error.details.length > 0) {
          return error.details[0];
        }
        if (error.error?.details && Array.isArray(error.error.details) && error.error.details.length > 0) {
          return error.error.details[0];
        }
        if (error.message) {
          return error.message;
        }
        if (error.error?.message) {
          return error.error.message;
        }
      }
      return 'Failed to add member';
    };

    this.projectService.addMember(projectId, employeeId).subscribe({
      next: () => {
        this.toastService.success('Member added', 'The employee has been successfully added to the project.');
        this.addingMember.set(false);
        this.closeAddMemberModal();
        this.loadProject(projectId);
      },
      error: (err) => {
        console.error('Error adding member:', err);
        const errorMessage = extractErrorMessage(err);
        this.toastService.error('Failed to add member', errorMessage);
        this.addingMember.set(false);
      }
    });
  }

  openEditModal(): void {
    this.showEditModal.set(true);
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
  }

  onProjectUpdated(): void {
    this.closeEditModal();
    const projectId = this.project()?.id;
    if (projectId) {
      this.loadProject(projectId);
    }
  }
}
