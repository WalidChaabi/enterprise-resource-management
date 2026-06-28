import { Component, OnInit, signal, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EmployeeService } from '../../../core/services/employee.service';
import { DepartmentService } from '../../../core/services/department.service';
import { EmployeeCreateDTO, EmployeeUpdateDTO } from '../../../core/models/employee.model';
import { Department } from '../../../core/models/department.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { InputComponent } from '../../../shared/components/ui/input/input.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
    selector: 'app-employee-form',
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
    templateUrl: './employee-form.component.html'
})
export class EmployeeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly employeeService = inject(EmployeeService);
  private readonly departmentService = inject(DepartmentService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // Inputs for modal usage
  modalMode = input<boolean>(false);
  employeeIdInput = input<string | null>(null);
  
  // Outputs for modal usage
  saved = output<void>();
  cancelled = output<void>();

  readonly form: FormGroup;
  readonly departments = signal<Department[]>([]);
  readonly loading = signal<boolean>(false);
  readonly saving = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly isEditMode = signal<boolean>(false);
  readonly employeeId = signal<string | null>(null);

  constructor() {
    this.form = this.fb.group({
      userId: ['', [Validators.required]],
      employeeCode: [''],
      jobTitle: [''],
      departmentId: [''],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.loadDepartments();
    
    // Only check route params if not in modal mode
    if (!this.modalMode()) {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.isEditMode.set(true);
        this.employeeId.set(id);
        this.loadEmployee(id);
      }
    } else {
      const employeeId = this.employeeIdInput();
      if (employeeId) {
        this.isEditMode.set(true);
        this.employeeId.set(employeeId);
        this.loadEmployee(employeeId);
      } else {
        // Reset form when opening in modal mode
        this.form.reset({
          userId: '',
          employeeCode: '',
          jobTitle: '',
          departmentId: '',
          active: true
        });
        this.error.set('');
        this.isEditMode.set(false);
        this.employeeId.set(null);
      }
    }
  }

  loadDepartments(): void {
    this.departmentService.getDepartments().subscribe({
      next: (departments) => {
        const departmentList = Array.isArray(departments)
          ? departments
          : ((departments as { content?: Department[]; items?: Department[]; data?: Department[] }).content ||
              (departments as { items?: Department[] }).items ||
              (departments as { data?: Department[] }).data ||
              []);
        this.departments.set(departmentList);
      },
      error: (err) => {
        console.error('Error loading departments:', err);
        // Don't block form if departments fail to load
      }
    });
  }

  loadEmployee(id: string): void {
    this.loading.set(true);
    this.error.set('');

    this.employeeService.getEmployee(id).subscribe({
      next: (employee) => {
        this.form.patchValue({
          userId: employee.userId || '',
          employeeCode: employee.employeeCode || '',
          jobTitle: employee.jobTitle || '',
          departmentId: employee.departmentId || '',
          active: employee.active
        });
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading employee:', err);
        let errorMessage = 'Failed to load employee';
        
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
    
    if (this.isEditMode() && this.employeeId()) {
      // Update existing employee
      const dto: EmployeeUpdateDTO = {
        employeeCode: formValue.employeeCode || undefined,
        jobTitle: formValue.jobTitle || undefined,
        departmentId: formValue.departmentId || undefined,
        active: formValue.active
      };

      this.employeeService.updateEmployee(this.employeeId()!, dto).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/employees']);
          }
        },
        error: (err) => {
          console.error('Error updating employee:', err);
          let errorMessage = 'Failed to update employee';
          
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
      // Create new employee
      const dto: EmployeeCreateDTO = {
        userId: formValue.userId,
        employeeCode: formValue.employeeCode || undefined,
        jobTitle: formValue.jobTitle || undefined,
        departmentId: formValue.departmentId || undefined
      };

      this.employeeService.createEmployee(dto).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/employees']);
          }
        },
        error: (err) => {
          console.error('Error creating employee:', err);
          let errorMessage = 'Failed to create employee';
          
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
      this.router.navigate(['/employees']);
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
