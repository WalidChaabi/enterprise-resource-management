import { LucideAngularModule } from 'lucide-angular';
import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { LeaveRequestService } from '../../../core/services/leave-request.service';
import {
  LeaveRequestCreateDTO,
  LeaveRequestUpdateDTO,
  LeaveType,
} from '../../../core/models/leave-request.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { TranslationService } from '../../../core/services/translation.service';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import {
  CardComponent,
  CardHeaderComponent,
  CardTitleComponent,
  CardContentComponent,
} from '../../../shared/components/ui/card/card.component';
import { DatePickerComponent } from '../../../shared/components/ui/date-picker/date-picker.component';
import { FormSelectComponent, FormSelectOption } from '../../../shared/components/ui/form-select/form-select.component';
import { getFieldErrorKey } from '../../../shared/utils/form-errors';

@Component({
  selector: 'app-leave-request-form',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    TranslatePipe,
    LucideAngularModule,
    ButtonComponent,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardContentComponent,
    DatePickerComponent,
    FormSelectComponent,
  ],
  templateUrl: './leave-request-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LeaveRequestFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly leaveRequestService = inject(LeaveRequestService);
  private readonly translationService = inject(TranslationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly modalMode = input<boolean>(false);
  readonly saved = output<void>();
  readonly cancelled = output<void>();

  readonly form: FormGroup;
  readonly loading = signal<boolean>(false);
  readonly saving = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly isEditMode = signal<boolean>(false);
  readonly leaveRequestId = signal<string | null>(null);

  readonly leaveTypes = [
    { value: LeaveType.VACATION, labelKey: 'leaveRequests.types.vacation' },
    { value: LeaveType.SICK, labelKey: 'leaveRequests.types.sick' },
    { value: LeaveType.PERSONAL, labelKey: 'leaveRequests.types.personal' },
    { value: LeaveType.UNPAID, labelKey: 'leaveRequests.types.unpaid' },
    { value: LeaveType.OTHER, labelKey: 'leaveRequests.types.other' },
  ];

  readonly leaveTypeSelectOptions = computed<FormSelectOption[]>(() =>
    this.leaveTypes.map((type) => ({
      value: type.value,
      label: this.translationService.translate(type.labelKey),
    })),
  );

  constructor() {
    this.form = this.fb.group(
      {
        leaveType: ['', [Validators.required]],
        startDate: ['', [Validators.required]],
        endDate: ['', [Validators.required]],
        reason: [''],
      },
      {
        validators: [this.dateRangeValidator],
      },
    );
  }

  ngOnInit(): void {
    if (!this.modalMode()) {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.isEditMode.set(true);
        this.leaveRequestId.set(id);
        this.loadLeaveRequest(id);
      }
    } else {
      this.resetForm();
    }
  }

  resetForm(): void {
    this.form.reset({
      leaveType: '',
      startDate: '',
      endDate: '',
      reason: '',
    });
    this.error.set('');
    this.isEditMode.set(false);
    this.leaveRequestId.set(null);
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
          reason: leaveRequest.reason || '',
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(this.extractError(err, 'Failed to load leave request'));
        this.loading.set(false);
      },
    });
  }

  calculateDays(): number {
    const startDate = this.form.get('startDate')?.value;
    const endDate = this.form.get('endDate')?.value;

    if (startDate && endDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);
      const diffTime = Math.abs(end.getTime() - start.getTime());
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
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
      reason: formValue.reason || undefined,
    };

    if (this.isEditMode() && this.leaveRequestId()) {
      this.leaveRequestService.updateLeaveRequest(this.leaveRequestId()!, dto as LeaveRequestUpdateDTO).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/leave-requests']);
          }
        },
        error: (err) => {
          this.error.set(this.extractError(err, 'Failed to update leave request'));
          this.saving.set(false);
        },
      });
    } else {
      this.leaveRequestService.createLeaveRequest(dto as LeaveRequestCreateDTO).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/leave-requests']);
          }
        },
        error: (err) => {
          this.error.set(this.extractError(err, 'Failed to create leave request'));
          this.saving.set(false);
        },
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
    return getFieldErrorKey(this.form.get(fieldName), {
      required: `leaveRequests.form.errors.${fieldName}Required`,
      dateRange: 'leaveRequests.form.errors.endAfterStart',
    });
  }

  private extractError(err: { details?: string[]; message?: string }, fallback: string): string {
    if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
      return err.details[0];
    }
    return err?.message || fallback;
  }
}
