import { Component, OnInit, signal, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LeaveRequestService } from '../../../core/services/leave-request.service';
import { LeaveRequest, LeaveRequestCreateDTO, LeaveRequestUpdateDTO, LeaveType } from '../../../core/models/leave-request.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { InputComponent } from '../../../shared/components/ui/input/input.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
  selector: 'app-leave-request-form',
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
  templateUrl: './leave-request-form.component.html'
})
export class LeaveRequestFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly leaveRequestService = inject(LeaveRequestService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // Inputs for modal usage
  modalMode = input<boolean>(false);
  
  // Outputs for modal usage
  saved = output<void>();
  cancelled = output<void>();

  readonly form: FormGroup;
  readonly loading = signal<boolean>(false);
  readonly saving = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly isEditMode = signal<boolean>(false);
  readonly leaveRequestId = signal<string | null>(null);

  readonly leaveTypes = [
    { value: LeaveType.VACATION, label: 'leaveRequests.types.vacation' },
    { value: LeaveType.SICK, label: 'leaveRequests.types.sick' },
    { value: LeaveType.PERSONAL, label: 'leaveRequests.types.personal' },
    { value: LeaveType.UNPAID, label: 'leaveRequests.types.unpaid' },
    { value: LeaveType.OTHER, label: 'leaveRequests.types.other' }
  ];

  constructor() {
    this.form = this.fb.group({
      leaveType: ['', [Validators.required]],
      startDate: ['', [Validators.required]],
      endDate: ['', [Validators.required]],
      reason: ['']
    }, {
      validators: [this.dateRangeValidator]
    });
  }

  ngOnInit(): void {
    // Only check route params if not in modal mode
    if (!this.modalMode()) {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.isEditMode.set(true);
        this.leaveRequestId.set(id);
        this.loadLeaveRequest(id);
      }
    } else {
      // Reset form when opening in modal mode
      this.form.reset({
        leaveType: '',
        startDate: '',
        endDate: '',
        reason: ''
      });
      this.error.set('');
      this.isEditMode.set(false);
      this.leaveRequestId.set(null);
    }
  }

  dateRangeValidator(form: FormGroup) {
    const startDate = form.get('startDate')?.value;
    const endDate = form.get('endDate')?.value;
    
    if (startDate && endDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);
      
      if (end < start) {
        form.get('endDate')?.setErrors({ dateRange: true });
        return { dateRange: true };
      }
    }
    
    return null;
  }

  loadLeaveRequest(id: string): void {
    this.loading.set(true);
    this.leaveRequestService.getLeaveRequest(id).subscribe({
      next: (leaveRequest) => {
        this.form.patchValue({
          leaveType: leaveRequest.leaveType,
          startDate: leaveRequest.startDate.split('T')[0],
          endDate: leaveRequest.endDate.split('T')[0],
          reason: leaveRequest.reason || ''
        });
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading leave request:', err);
        let errorMessage = 'Failed to load leave request';
        
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

  calculateDays(): number {
    const startDate = this.form.get('startDate')?.value;
    const endDate = this.form.get('endDate')?.value;
    
    if (startDate && endDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);
      const diffTime = Math.abs(end.getTime() - start.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1; // +1 to include both start and end days
      return diffDays;
    }
    
    return 0;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set('');

    const formValue = this.form.value;
    const dto: LeaveRequestCreateDTO | LeaveRequestUpdateDTO = {
      leaveType: formValue.leaveType,
      startDate: formValue.startDate,
      endDate: formValue.endDate,
      reason: formValue.reason || undefined
    };

    if (this.isEditMode() && this.leaveRequestId()) {
      // Update existing leave request
      this.leaveRequestService.updateLeaveRequest(this.leaveRequestId()!, dto as LeaveRequestUpdateDTO).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/leave-requests']);
          }
        },
        error: (err) => {
          console.error('Error updating leave request:', err);
          let errorMessage = 'Failed to update leave request';
          
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
      // Create new leave request
      this.leaveRequestService.createLeaveRequest(dto as LeaveRequestCreateDTO).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/leave-requests']);
          }
        },
        error: (err) => {
          console.error('Error creating leave request:', err);
          let errorMessage = 'Failed to create leave request';
          
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
      this.router.navigate(['/leave-requests']);
    }
  }

  getFieldError(fieldName: string): string {
    const field = this.form.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName} is required`;
      }
      if (field.errors['dateRange']) {
        return 'End date must be after start date';
      }
    }
    return '';
  }
}
