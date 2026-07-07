import { LucideAngularModule } from 'lucide-angular';
import { Component, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TimeEntryService } from '../../../core/services/time-entry.service';
import { ProjectService } from '../../../core/services/project.service';
import { TimeEntryCreateDTO, TimeEntryUpdateDTO } from '../../../core/models/time-entry.model';
import { Project } from '../../../core/models/project.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { TranslationService } from '../../../core/services/translation.service';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { DatePickerComponent } from '../../../shared/components/ui/date-picker/date-picker.component';
import { FormSelectComponent, FormSelectOption } from '../../../shared/components/ui/form-select/form-select.component';
import { getFieldErrorKey } from '../../../shared/utils/form-errors';

@Component({
    selector: 'app-time-entry-form',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterModule,
        DecimalPipe,
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
    templateUrl: './time-entry-form.component.html'
})
export class TimeEntryFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly timeEntryService = inject(TimeEntryService);
  private readonly projectService = inject(ProjectService);
  private readonly translationService = inject(TranslationService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  modalMode = input<boolean>(false);
  saved = output<void>();
  cancelled = output<void>();

  readonly form: FormGroup;
  readonly projects = signal<Project[]>([]);
  readonly loading = signal<boolean>(false);
  readonly saving = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly isEditMode = signal<boolean>(false);
  readonly timeEntryId = signal<string | null>(null);

  readonly durationPresets = [
    { hours: 4, labelKey: 'timeEntries.form.presets.halfDay' },
    { hours: 7.5, labelKey: 'timeEntries.form.presets.standard' },
    { hours: 8, labelKey: 'timeEntries.form.presets.fullDay' },
  ];

  readonly projectSelectOptions = computed<FormSelectOption[]>(() =>
    this.projects().map((project) => ({ value: project.id, label: project.name }))
  );

  constructor() {
    const today = new Date().toISOString().split('T')[0];
    this.form = this.fb.group({
      projectId: ['', [Validators.required]],
      entryDate: [today, [Validators.required]],
      startTime: ['09:00', [Validators.required]],
      endTime: ['17:00', [Validators.required]],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadProjects();

    if (!this.modalMode()) {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.isEditMode.set(true);
        this.timeEntryId.set(id);
        this.loadTimeEntry(id);
      }
    } else {
      this.resetForm();
    }
  }

  resetForm(): void {
    const today = new Date().toISOString().split('T')[0];
    this.form.reset({
      projectId: '',
      entryDate: today,
      startTime: '09:00',
      endTime: '17:00',
      description: ''
    });
    this.error.set('');
    this.isEditMode.set(false);
    this.timeEntryId.set(null);
  }

  loadProjects(): void {
    this.loading.set(true);
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.projects.set(projects);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(this.translationService.translate('timeEntries.form.loadProjectsError'));
        this.loading.set(false);
      }
    });
  }

  loadTimeEntry(id: string): void {
    this.loading.set(true);
    this.timeEntryService.getTimeEntry(id).subscribe({
      next: (timeEntry) => {
        const entryDate = (timeEntry.entryDate ?? timeEntry.date).split('T')[0];
        const startTime = timeEntry.startTime ?? '09:00';
        const endTime = timeEntry.endTime ?? this.calculateEndTime(startTime, timeEntry.hours);

        this.form.patchValue({
          projectId: timeEntry.projectId,
          entryDate,
          startTime,
          endTime,
          description: timeEntry.description || ''
        });
        this.loading.set(false);
      },
      error: (err) => {
        const message = err?.details?.[0] || err?.message || this.translationService.translate('timeEntries.form.loadError');
        this.error.set(message);
        this.loading.set(false);
      }
    });
  }

  applyDurationPreset(hours: number): void {
    const startTime = this.form.get('startTime')?.value || '09:00';
    this.form.patchValue({ endTime: this.calculateEndTime(startTime, hours) });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set('');

    const formValue = this.form.value;
    const entryDate = String(formValue.entryDate).trim();
    const startTimeStr = String(formValue.startTime).trim();
    const endTimeStr = String(formValue.endTime).trim();

    if (!formValue.projectId?.trim()) {
      this.error.set(this.translationService.translate('timeEntries.form.projectRequired'));
      this.form.get('projectId')?.markAsTouched();
      this.saving.set(false);
      return;
    }

    if (endTimeStr <= startTimeStr) {
      this.error.set(this.translationService.translate('timeEntries.form.endAfterStart'));
      this.form.get('endTime')?.markAsTouched();
      this.saving.set(false);
      return;
    }

    const dto: TimeEntryCreateDTO | TimeEntryUpdateDTO = {
      projectId: formValue.projectId.trim(),
      entryDate,
      startTime: `${entryDate}T${startTimeStr}:00`,
      endTime: `${entryDate}T${endTimeStr}:00`,
      description: formValue.description?.trim() || undefined
    };

    const request$ = this.isEditMode() && this.timeEntryId()
      ? this.timeEntryService.updateTimeEntry(this.timeEntryId()!, dto as TimeEntryUpdateDTO)
      : this.timeEntryService.createTimeEntry(dto as TimeEntryCreateDTO);

    request$.subscribe({
      next: () => {
        if (this.modalMode()) {
          this.saved.emit();
        } else {
          this.router.navigate(['/time-entries']);
        }
      },
      error: (err) => {
        const message = err?.details?.join?.('. ')
          || err?.details?.[0]
          || err?.message
          || this.translationService.translate('timeEntries.form.saveError');
        this.error.set(message);
        this.saving.set(false);
      }
    });
  }

  onCancel(): void {
    if (this.modalMode()) {
      this.cancelled.emit();
    } else {
      this.router.navigate(['/time-entries']);
    }
  }

  getFieldError(fieldName: string): string {
    return getFieldErrorKey(this.form.get(fieldName), {
      required: `timeEntries.form.errors.${fieldName}Required`,
    });
  }

  calculateEndTime(startTime: string, hours: number): string {
    const [startHours, startMinutes] = startTime.split(':').map(Number);
    const totalMinutes = startHours * 60 + startMinutes + (hours * 60);
    const endHours = Math.floor(totalMinutes / 60) % 24;
    const endMinutes = totalMinutes % 60;
    return `${endHours.toString().padStart(2, '0')}:${endMinutes.toString().padStart(2, '0')}`;
  }

  calculateHours(): number {
    const startTime = this.form.get('startTime')?.value;
    const endTime = this.form.get('endTime')?.value;

    if (!startTime || !endTime) return 0;

    const [startHours, startMinutes] = startTime.split(':').map(Number);
    const [endHours, endMinutes] = endTime.split(':').map(Number);

    let diffMinutes = (endHours * 60 + endMinutes) - (startHours * 60 + startMinutes);
    if (diffMinutes < 0) diffMinutes += 24 * 60;

    return diffMinutes / 60;
  }
}
