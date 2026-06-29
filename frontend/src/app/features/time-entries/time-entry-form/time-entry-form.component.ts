import { LucideAngularModule } from 'lucide-angular';
import { Component, OnInit, signal, inject, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TimeEntryService } from '../../../core/services/time-entry.service';
import { ProjectService } from '../../../core/services/project.service';
import { TimeEntryCreateDTO, TimeEntryUpdateDTO } from '../../../core/models/time-entry.model';
import { Project } from '../../../core/models/project.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { InputComponent } from '../../../shared/components/ui/input/input.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
    selector: 'app-time-entry-form',
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
        InputComponent,
        AlertComponent
    ],
    templateUrl: './time-entry-form.component.html'
})
export class TimeEntryFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly timeEntryService = inject(TimeEntryService);
  private readonly projectService = inject(ProjectService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  // Inputs for modal usage
  modalMode = input<boolean>(false);
  
  // Outputs for modal usage
  saved = output<void>();
  cancelled = output<void>();

  readonly form: FormGroup;
  readonly projects = signal<Project[]>([]);
  readonly loading = signal<boolean>(false);
  readonly saving = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly isEditMode = signal<boolean>(false);
  readonly timeEntryId = signal<string | null>(null);

  constructor() {
    const today = new Date().toISOString().split('T')[0];
    const defaultStartTime = '09:00';
    const defaultEndTime = '17:00';
    
    this.form = this.fb.group({
      projectId: ['', [Validators.required]],
      entryDate: [today, [Validators.required]],
      startTime: [defaultStartTime, [Validators.required]],
      endTime: [defaultEndTime, [Validators.required]],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadProjects();
    
    // Only check route params if not in modal mode
    if (!this.modalMode()) {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.isEditMode.set(true);
        this.timeEntryId.set(id);
        this.loadTimeEntry(id);
      }
    } else {
      // Reset form when opening in modal mode
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
  }

  loadProjects(): void {
    this.loading.set(true);
    this.projectService.getProjects().subscribe({
      next: (projects) => {
        this.projects.set(projects);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading projects:', err);
        this.error.set('Failed to load projects');
        this.loading.set(false);
      }
    });
  }

  loadTimeEntry(id: string): void {
    this.loading.set(true);
    this.timeEntryService.getTimeEntry(id).subscribe({
      next: (timeEntry) => {
        // Parse date and calculate start/end times from hours
        // If backend provides startTime/endTime, use those; otherwise calculate from hours
        const entryDate = timeEntry.date.split('T')[0];
        const startTime = '09:00'; // Default, should come from backend
        const endTime = this.calculateEndTime(startTime, timeEntry.hours);
        
        this.form.patchValue({
          projectId: timeEntry.projectId,
          entryDate: entryDate,
          startTime: startTime,
          endTime: endTime,
          description: timeEntry.description || ''
        });
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading time entry:', err);
        let errorMessage = 'Failed to load time entry';
        
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
    
    // Validate projectId
    if (!formValue.projectId || formValue.projectId.trim() === '') {
      this.error.set('Project is required');
      this.form.get('projectId')?.markAsTouched();
      this.saving.set(false);
      return;
    }
    
    // Validate entryDate
    if (!formValue.entryDate || formValue.entryDate.trim() === '') {
      this.error.set('Date is required');
      this.form.get('entryDate')?.markAsTouched();
      this.saving.set(false);
      return;
    }
    
    // Validate startTime
    if (!formValue.startTime || formValue.startTime.trim() === '') {
      this.error.set('Start time is required');
      this.form.get('startTime')?.markAsTouched();
      this.saving.set(false);
      return;
    }
    
    // Validate endTime
    if (!formValue.endTime || formValue.endTime.trim() === '') {
      this.error.set('End time is required');
      this.form.get('endTime')?.markAsTouched();
      this.saving.set(false);
      return;
    }
    
    // Validate endTime is after startTime
    if (formValue.endTime <= formValue.startTime) {
      this.error.set('End time must be after start time');
      this.form.get('endTime')?.markAsTouched();
      this.saving.set(false);
      return;
    }
    
    // Combine entryDate with startTime and endTime to create full datetime strings
    const entryDate = formValue.entryDate.trim();
    const startTimeStr = formValue.startTime.trim();
    const endTimeStr = formValue.endTime.trim();
    
    // Create ISO datetime strings: "YYYY-MM-DDTHH:mm:ss"
    const startDateTime = `${entryDate}T${startTimeStr}:00`;
    const endDateTime = `${entryDate}T${endTimeStr}:00`;
    
    const dto: TimeEntryCreateDTO | TimeEntryUpdateDTO = {
      projectId: formValue.projectId.trim(),
      entryDate: entryDate,
      startTime: startDateTime,
      endTime: endDateTime,
      description: formValue.description?.trim() || undefined
    };

    if (this.isEditMode() && this.timeEntryId()) {
      // Update existing time entry
      this.timeEntryService.updateTimeEntry(this.timeEntryId()!, dto as TimeEntryUpdateDTO).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/time-entries']);
          }
        },
        error: (err) => {
          console.error('Error updating time entry:', err);
          let errorMessage = 'Failed to update time entry';
          
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
      // Create new time entry
      this.timeEntryService.createTimeEntry(dto as TimeEntryCreateDTO).subscribe({
        next: () => {
          if (this.modalMode()) {
            this.saved.emit();
          } else {
            this.router.navigate(['/time-entries']);
          }
        },
        error: (err) => {
          console.error('Error creating time entry:', err);
          let errorMessage = 'Failed to create time entry';
          
          // Handle validation errors from backend
          if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
            // Join all validation error messages
            errorMessage = err.details.join('. ');
          } else if (err?.error?.details && Array.isArray(err.error.details) && err.error.details.length > 0) {
            errorMessage = err.error.details.join('. ');
          } else if (err?.message) {
            errorMessage = err.message;
          } else if (err?.error?.message) {
            errorMessage = err.error.message;
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
      this.router.navigate(['/time-entries']);
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
      if (field.errors['max']) {
        return `${fieldName} must be at most ${field.errors['max'].max}`;
      }
    }
    return '';
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
    
    const startTotalMinutes = startHours * 60 + startMinutes;
    const endTotalMinutes = endHours * 60 + endMinutes;
    
    // Handle case where endTime is next day
    let diffMinutes = endTotalMinutes - startTotalMinutes;
    if (diffMinutes < 0) {
      diffMinutes += 24 * 60; // Add 24 hours
    }
    
    return diffMinutes / 60;
  }
}
