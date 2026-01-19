import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { TaskService } from '../../../core/services/task.service';
import { Task, TaskStatus, TaskPriority } from '../../../core/models/task.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { BadgeComponent, BadgeVariant } from '../../../shared/components/ui/badge/badge.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslatePipe, ButtonComponent, CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent, BadgeComponent, AlertComponent, DatePipe],
  templateUrl: './task-detail.component.html'
})
export class TaskDetailComponent implements OnInit {
  private readonly taskService = inject(TaskService);
  private readonly route = inject(ActivatedRoute);

  readonly task = signal<Task | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');

  readonly projectId = computed(() => {
    const projectId = this.route.snapshot.paramMap.get('projectId');
    return projectId || this.task()?.projectId || null;
  });

  ngOnInit(): void {
    const projectId = this.route.snapshot.paramMap.get('projectId');
    const taskId = this.route.snapshot.paramMap.get('id');
    
    if (projectId && taskId) {
      this.loadTask(projectId, taskId);
    }
  }

  loadTask(projectId: string, taskId: string): void {
    this.loading.set(true);
    this.error.set('');

    this.taskService.getTask(projectId, taskId).subscribe({
      next: (task) => {
        this.task.set(task);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading task:', err);
        let errorMessage = 'Failed to load task';
        
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
}
