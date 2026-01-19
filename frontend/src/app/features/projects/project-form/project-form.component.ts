import { Component, OnInit, signal, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProjectService } from '../../../core/services/project.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { Project, ProjectCreateDTO, ProjectUpdateDTO, ProjectStatus } from '../../../core/models/project.model';
import { Employee } from '../../../core/models/employee.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { InputComponent } from '../../../shared/components/ui/input/input.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';

@Component({
  selector: 'app-project-form',
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
  templateUrl: './project-form.component.html'
})
export class ProjectFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly projectService = inject(ProjectService);
  private readonly employeeService = inject(EmployeeService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  // Inputs for modal usage
  modalMode = input<boolean>(false);
  projectIdInput = input<string | null>(null);
  
  // Outputs for modal usage
  saved = output<void>();
  cancelled = output<void>();

  readonly form: FormGroup;
  readonly employees = signal<Employee[]>([]);
  readonly loading = signal<boolean>(false);
  readonly saving = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly isEditMode = signal<boolean>(false);
  readonly projectId = signal<string | null>(null);

  readonly projectStatuses = [
    { value: ProjectStatus.PLANNING, label: 'projects.status.planning' },
    { value: ProjectStatus.ACTIVE, label: 'projects.status.active' },
    { value: ProjectStatus.ON_HOLD, label: 'projects.status.on_hold' },
    { value: ProjectStatus.COMPLETED, label: 'projects.status.completed' },
    { value: ProjectStatus.CANCELLED, label: 'projects.status.cancelled' }
  ];

  constructor() {
    this.form = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
      status: [ProjectStatus.PLANNING],
      startDate: [''],
      deadline: [''],
      budget: [0, [Validators.min(0)]],
      managerId: ['']
    });
  }

  ngOnInit(): void {
    this.loadEmployees();
    
    // Only check route params if not in modal mode
    if (!this.modalMode()) {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.isEditMode.set(true);
        this.projectId.set(id);
        this.loadProject(id);
      }
    } else {
      // Modal mode: check if projectIdInput is provided for edit mode
      const projectId = this.projectIdInput();
      if (projectId) {
        // Edit mode in modal
        this.isEditMode.set(true);
        this.projectId.set(projectId);
        this.loadProject(projectId);
      } else {
        // Create mode in modal
        this.form.reset({
          name: '',
          description: '',
          status: ProjectStatus.PLANNING,
          startDate: '',
          deadline: '',
          budget: 0,
          managerId: ''
        });
        this.error.set('');
        this.isEditMode.set(false);
        this.projectId.set(null);
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

  loadProject(id: string): void {
    this.loading.set(true);
    this.error.set('');

    this.projectService.getProject(id).subscribe({
      next: (project) => {
        this.form.patchValue({
          name: project.name,
          description: project.description || '',
          status: project.status,
          startDate: project.startDate ? project.startDate.split('T')[0] : '',
          deadline: project.deadline ? project.deadline.split('T')[0] : '',
          budget: project.budget || 0,
          managerId: '' // Manager ID not directly available in Project model
        });
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading project:', err);
        let errorMessage = 'Failed to load project';
        
        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
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
    
    if (this.isEditMode() && this.projectId()) {
      // Update existing project
      const dto: ProjectUpdateDTO = {
        name: formValue.name,
        description: formValue.description || undefined,
        status: formValue.status,
        endDate: undefined, // Not in form, can be set separately
        cost: undefined // Not in form, can be set separately
      };

      this.projectService.updateProject(this.projectId()!, dto).subscribe({
        next: () => {
          this.toastService.success('Project updated', 'The project has been successfully updated.');
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/projects']);
          }
        },
        error: (err) => {
          console.error('Error updating project:', err);
          let errorMessage = 'Failed to update project';
          
          if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
            errorMessage = err.details[0];
          } else if (err?.message) {
            errorMessage = err.message;
          }
          
          this.error.set(errorMessage);
          this.toastService.error('Failed to update project', errorMessage);
          this.saving.set(false);
        }
      });
    } else {
      // Create new project
      const dto: ProjectCreateDTO = {
        name: formValue.name,
        description: formValue.description || undefined,
        status: formValue.status,
        startDate: formValue.startDate || undefined,
        deadline: formValue.deadline || undefined,
        budget: formValue.budget ? parseFloat(formValue.budget) : undefined,
        managerId: formValue.managerId || undefined,
        memberIds: [] // Can be added later via addMember endpoint
      };

      this.projectService.createProject(dto).subscribe({
        next: () => {
          this.toastService.success('Project created', 'The project has been successfully created.');
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/projects']);
          }
        },
        error: (err) => {
          console.error('Error creating project:', err);
          let errorMessage = 'Failed to create project';
          
          if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
            errorMessage = err.details[0];
          } else if (err?.message) {
            errorMessage = err.message;
          }
          
          this.error.set(errorMessage);
          this.toastService.error('Failed to create project', errorMessage);
          this.saving.set(false);
        }
      });
    }
  }

  onCancel(): void {
    if (this.modalMode()) {
      this.cancelled.emit();
    } else {
      this.router.navigate(['/projects']);
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
