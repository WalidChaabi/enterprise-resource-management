import { Component, OnInit, signal, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { DepartmentService } from '../../../core/services/department.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { Department, DepartmentCreateDTO, DepartmentUpdateDTO } from '../../../core/models/department.model';
import { Employee } from '../../../core/models/employee.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
  selector: 'app-department-form',
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
    AlertComponent
  ],
  templateUrl: './department-form.component.html'
})
export class DepartmentFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly departmentService = inject(DepartmentService);
  private readonly employeeService = inject(EmployeeService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // Inputs for modal usage
  modalMode = input<boolean>(false);
  departmentIdInput = input<string | null>(null);
  
  // Outputs for modal usage
  saved = output<void>();
  cancelled = output<void>();

  readonly form: FormGroup;
  readonly employees = signal<Employee[]>([]);
  readonly loading = signal<boolean>(false);
  readonly saving = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly isEditMode = signal<boolean>(false);
  readonly departmentId = signal<string | null>(null);

  constructor() {
    this.form = this.fb.group({
      name: ['', [Validators.required]],
      description: [''],
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
        this.departmentId.set(id);
        this.loadDepartment(id);
      }
    } else {
      // Modal mode: check if departmentIdInput is provided for edit mode
      const departmentId = this.departmentIdInput();
      if (departmentId) {
        this.isEditMode.set(true);
        this.departmentId.set(departmentId);
        this.loadDepartment(departmentId);
      } else {
        // Reset form when opening in modal mode
        this.form.reset({
          name: '',
          description: '',
          managerId: ''
        });
        this.error.set('');
        this.isEditMode.set(false);
        this.departmentId.set(null);
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

  loadDepartment(id: string): void {
    this.loading.set(true);
    this.error.set('');

    this.departmentService.getDepartment(id).subscribe({
      next: (department) => {
        this.form.patchValue({
          name: department.name,
          description: department.description || '',
          managerId: department.managerId || ''
        });
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading department:', err);
        let errorMessage = 'Failed to load department';
        
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
    
    if (this.isEditMode() && this.departmentId()) {
      // Update existing department
      const dto: DepartmentUpdateDTO = {
        name: formValue.name,
        description: formValue.description || undefined,
        managerId: formValue.managerId || undefined
      };

      this.departmentService.updateDepartment(this.departmentId()!, dto).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/departments']);
          }
        },
        error: (err) => {
          console.error('Error updating department:', err);
          let errorMessage = 'Failed to update department';
          
          if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
            errorMessage = err.details[0];
          } else if (err?.message) {
            errorMessage = err.message;
          }
          
          this.error.set(errorMessage);
          this.saving.set(false);
        }
      });
    } else {
      // Create new department
      const dto: DepartmentCreateDTO = {
        name: formValue.name,
        description: formValue.description || undefined,
        managerId: formValue.managerId || undefined
      };

      this.departmentService.createDepartment(dto).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/departments']);
          }
        },
        error: (err) => {
          console.error('Error creating department:', err);
          let errorMessage = 'Failed to create department';
          
          if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
            errorMessage = err.details[0];
          } else if (err?.message) {
            errorMessage = err.message;
          }
          
          this.error.set(errorMessage);
          this.saving.set(false);
        }
      });
    }
  }

  onCancel(): void {
    if (this.modalMode()) {
      this.cancelled.emit();
    } else {
      this.router.navigate(['/departments']);
    }
  }

  getFieldError(fieldName: string): string {
    const field = this.form.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName} is required`;
      }
    }
    return '';
  }
}
