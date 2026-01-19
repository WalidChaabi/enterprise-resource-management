import { Component, OnInit, signal, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TaskService } from '../../../core/services/task.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { Task, TaskCreateDTO, TaskUpdateDTO, TaskStatus, TaskPriority } from '../../../core/models/task.model';
import { Employee } from '../../../core/models/employee.model';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { InputComponent } from '../../../shared/components/ui/input/input.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    TranslatePipe,
    ButtonComponent,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardContentComponent,
    InputComponent,
    AlertComponent
  ],
  templateUrl: './task-form.component.html'
})
export class TaskFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly taskService = inject(TaskService);
  private readonly employeeService = inject(EmployeeService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  // Modal mode support
  readonly modalMode = input<boolean>(false);
  readonly projectIdInput = input<string | null>(null);
  readonly taskIdInput = input<string | null>(null);
  readonly saved = output<void>();
  readonly cancelled = output<void>();

  readonly form: FormGroup;
  readonly employees = signal<Employee[]>([]);
  readonly loading = signal<boolean>(false);
  readonly saving = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly isEditMode = signal<boolean>(false);
  readonly taskId = signal<string | null>(null);
  readonly projectId = signal<string | null>(null);

  readonly taskStatuses = [
    { value: TaskStatus.TODO, label: 'tasks.status.todo' },
    { value: TaskStatus.IN_PROGRESS, label: 'tasks.status.in_progress' },
    { value: TaskStatus.DONE, label: 'tasks.status.done' },
    { value: TaskStatus.CANCELLED, label: 'tasks.status.cancelled' }
  ];

  readonly taskPriorities = [
    { value: TaskPriority.LOW, label: 'tasks.priority.low' },
    { value: TaskPriority.MEDIUM, label: 'tasks.priority.medium' },
    { value: TaskPriority.HIGH, label: 'tasks.priority.high' },
    { value: TaskPriority.URGENT, label: 'tasks.priority.urgent' }
  ];

  constructor() {
    this.form = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      status: [TaskStatus.TODO, [Validators.required]],
      priority: [''],
      assigneeId: [''],
      dueDate: [''],
      estimatedHours: [0, [Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    if (this.modalMode()) {
      // Modal mode: use input projectId, skip route params
      const projectId = this.projectIdInput();
      const taskId = this.taskIdInput();
      
      if (!projectId) {
        this.error.set('Project ID is required');
        return;
      }
      
      this.projectId.set(projectId);
      this.loadEmployees();
      
      if (taskId) {
        // Edit mode in modal
        this.isEditMode.set(true);
        this.taskId.set(taskId);
        this.loadTask(projectId, taskId);
      } else {
        // Create mode in modal
        this.form.reset({
          name: '',
          description: '',
          status: TaskStatus.TODO,
          priority: '',
          assigneeId: '',
          dueDate: '',
          estimatedHours: 0
        });
      }
    } else {
      // Page mode: use route params
      const projectId = this.route.snapshot.paramMap.get('projectId');
      const taskId = this.route.snapshot.paramMap.get('id');
      
      if (!projectId) {
        this.error.set('Project ID is required');
        return;
      }

      this.projectId.set(projectId);
      this.loadEmployees();

      if (taskId) {
        this.isEditMode.set(true);
        this.taskId.set(taskId);
        this.loadTask(projectId, taskId);
      }
    }
  }

  loadEmployees(): void {
    this.employeeService.getEmployees({ active: true, size: 1000 }).subscribe({
      next: (response) => {
        const employees = Array.isArray(response) ? response : (response.content || []);
        this.employees.set(employees);
      },
      error: (err) => {
        console.error('Error loading employees:', err);
        // Don't block form if employees fail to load
      }
    });
  }

  loadTask(projectId: string, taskId: string): void {
    this.loading.set(true);
    this.error.set('');

    this.taskService.getTask(projectId, taskId).subscribe({
      next: (task) => {
        this.form.patchValue({
          name: task.name,
          description: task.description || '',
          status: task.status,
          priority: task.priority || '',
          assigneeId: task.assigneeId || '',
          dueDate: task.dueDate ? task.dueDate.split('T')[0] : '',
          estimatedHours: task.estimatedHours || 0
        });
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading task:', err);
        let errorMessage = 'Failed to load task';
        
        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.error?.details && Array.isArray(err.error.details) && err.error.details.length > 0) {
          errorMessage = err.error.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
        } else if (err?.error?.message) {
          errorMessage = err.error.message;
        }
        
        this.error.set(errorMessage);
        this.loading.set(false);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set('');

    const formValue = this.form.value;
    const estimatedHoursValue = formValue.estimatedHours;
    const parsedEstimatedHours =
      estimatedHoursValue === '' || estimatedHoursValue === null || estimatedHoursValue === undefined
        ? undefined
        : Number(estimatedHoursValue);
    const dto: TaskCreateDTO | TaskUpdateDTO = {
      name: formValue.name,
      description: formValue.description || undefined,
      status: formValue.status,
      priority: formValue.priority || undefined,
      assigneeId: formValue.assigneeId || undefined,
      dueDate: formValue.dueDate || undefined,
      estimatedHours: Number.isFinite(parsedEstimatedHours) ? parsedEstimatedHours : undefined
    };

    const projectId = this.projectId();
    if (!projectId) {
      this.error.set('Project ID is required');
      this.saving.set(false);
      return;
    }

    if (this.isEditMode() && this.taskId()) {
      // Update existing task
      this.taskService.updateTask(projectId, this.taskId()!, dto as TaskUpdateDTO).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.toastService.success('Task updated', 'The task has been successfully updated.');
            this.saved.emit();
          } else {
            this.router.navigate(['/projects', projectId]);
          }
        },
        error: (err) => {
          console.error('Error updating task:', err);
          let errorMessage = 'Failed to update task';
          
          if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
            errorMessage = err.details[0];
          } else if (err?.error?.details && Array.isArray(err.error.details) && err.error.details.length > 0) {
            errorMessage = err.error.details[0];
          } else if (err?.message) {
            errorMessage = err.message;
          } else if (err?.error?.message) {
            errorMessage = err.error.message;
          }
          
          this.error.set(errorMessage);
          this.toastService.error('Failed to update task', errorMessage);
          this.saving.set(false);
        }
      });
    } else {
      // Create new task
      this.taskService.createTask(projectId, dto as TaskCreateDTO).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.toastService.success('Task created', 'The task has been successfully created.');
            this.saved.emit();
          } else {
            this.router.navigate(['/projects', projectId]);
          }
        },
        error: (err) => {
          console.error('Error creating task:', err);
          let errorMessage = 'Failed to create task';
          
          if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
            errorMessage = err.details[0];
          } else if (err?.error?.details && Array.isArray(err.error.details) && err.error.details.length > 0) {
            errorMessage = err.error.details[0];
          } else if (err?.message) {
            errorMessage = err.message;
          } else if (err?.error?.message) {
            errorMessage = err.error.message;
          }
          
          this.error.set(errorMessage);
          this.toastService.error('Failed to create task', errorMessage);
          this.saving.set(false);
        }
      });
    }
  }

  onCancel(): void {
    if (this.modalMode()) {
      this.cancelled.emit();
    } else {
      const projectId = this.projectId();
      if (projectId) {
        this.router.navigate(['/projects', projectId]);
      } else {
        this.router.navigate(['/projects']);
      }
    }
  }

  getFieldError(fieldName: string): string {
    const field = this.form.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName} is required`;
      }
      if (field.errors['min']) {
        return `${fieldName} must be at least ${field.errors['min'].min}`;
      }
    }
    return '';
  }
}
