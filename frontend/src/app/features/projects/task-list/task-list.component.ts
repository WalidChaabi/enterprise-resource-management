import { Component, Input, OnInit, signal, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TaskService } from '../../../core/services/task.service';
import { Task, TaskStatus, TaskPriority } from '../../../core/models/task.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { BadgeComponent, BadgeVariant } from '../../../shared/components/ui/badge/badge.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { TaskFormComponent } from '../task-form/task-form.component';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslatePipe, ButtonComponent, BadgeComponent, AlertComponent, DatePipe, DialogComponent, TaskFormComponent],
  templateUrl: './task-list.component.html'
})
export class TaskListComponent implements OnInit {
  private readonly taskService = inject(TaskService);

  @Input({ required: true }) projectId!: string;

  readonly tasks = signal<Task[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);
  readonly showEditModal = signal<boolean>(false);
  readonly selectedTaskId = signal<string | null>(null);

  ngOnInit(): void {
    if (this.projectId) {
      this.loadTasks();
    }
  }

  loadTasks(): void {
    this.loading.set(true);
    this.error.set('');

    this.taskService.getTasks(this.projectId).subscribe({
      next: (tasks) => {
        this.tasks.set(tasks);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading tasks:', err);
        let errorMessage = 'Failed to load tasks';
        
        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
        } else if (typeof err === 'string') {
          errorMessage = err;
        } else if (err?.error?.message) {
          errorMessage = err.error.message;
        } else if (err?.error?.details && Array.isArray(err.error.details) && err.error.details.length > 0) {
          errorMessage = err.error.details[0];
        }
        
        this.error.set(errorMessage);
        this.loading.set(false);
      }
    });
  }

  getStatusVariant(status: TaskStatus): BadgeVariant {
    switch (status) {
      case TaskStatus.DONE:
        return 'default';
      case TaskStatus.IN_PROGRESS:
        return 'secondary';
      case TaskStatus.CANCELLED:
        return 'destructive';
      default:
        return 'outline';
    }
  }

  getPriorityVariant(priority?: TaskPriority): BadgeVariant {
    switch (priority) {
      case TaskPriority.URGENT:
      case TaskPriority.HIGH:
        return 'destructive';
      case TaskPriority.MEDIUM:
        return 'secondary';
      default:
        return 'outline';
    }
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  onTaskCreated(): void {
    this.closeCreateModal();
    this.loadTasks();
  }

  openEditModal(taskId: string): void {
    this.selectedTaskId.set(taskId);
    this.showEditModal.set(true);
  }

  closeEditModal(): void {
    this.showEditModal.set(false);
    this.selectedTaskId.set(null);
  }

  onTaskUpdated(): void {
    this.closeEditModal();
    this.loadTasks();
  }
}
