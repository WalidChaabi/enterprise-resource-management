import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TimeEntryService } from '../../../core/services/time-entry.service';
import { AuthService } from '../../../core/services/auth.service';
import { TimeEntry, TimeEntryStatus } from '../../../core/models/time-entry.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { BadgeComponent, BadgeVariant } from '../../../shared/components/ui/badge/badge.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { InputComponent } from '../../../shared/components/ui/input/input.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { TimeEntryFormComponent } from '../time-entry-form/time-entry-form.component';

@Component({
  selector: 'app-time-entry-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslatePipe, ButtonComponent, CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent, BadgeComponent, SkeletonTableComponent, DatePipe, InputComponent, DialogComponent, TimeEntryFormComponent],
  templateUrl: './time-entry-list.component.html'
})
export class TimeEntryListComponent implements OnInit {
  private readonly timeEntryService = inject(TimeEntryService);
  private readonly authService = inject(AuthService);

  readonly timeEntries = signal<TimeEntry[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly processing = signal<string | null>(null);
  readonly rejectingEntryId = signal<string | null>(null);
  readonly rejectionReason = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);

  readonly canApprove = computed(() => {
    return this.authService.hasAnyRole(ROLES.PROJECT_MANAGER, ROLES.ORG_ADMIN, ROLES.HR_ADMIN);
  });

  // Summary statistics
  readonly totalHours = computed(() => {
    return this.timeEntries().reduce((sum, entry) => sum + entry.hours, 0);
  });

  readonly hoursByStatus = computed(() => {
    const entries = this.timeEntries();
    return {
      draft: entries.filter(e => e.status === TimeEntryStatus.DRAFT).reduce((sum, e) => sum + e.hours, 0),
      submitted: entries.filter(e => e.status === TimeEntryStatus.SUBMITTED).reduce((sum, e) => sum + e.hours, 0),
      approved: entries.filter(e => e.status === TimeEntryStatus.APPROVED).reduce((sum, e) => sum + e.hours, 0),
      rejected: entries.filter(e => e.status === TimeEntryStatus.REJECTED).reduce((sum, e) => sum + e.hours, 0)
    };
  });

  readonly entriesByStatus = computed(() => {
    const entries = this.timeEntries();
    return {
      draft: entries.filter(e => e.status === TimeEntryStatus.DRAFT).length,
      submitted: entries.filter(e => e.status === TimeEntryStatus.SUBMITTED).length,
      approved: entries.filter(e => e.status === TimeEntryStatus.APPROVED).length,
      rejected: entries.filter(e => e.status === TimeEntryStatus.REJECTED).length
    };
  });

  ngOnInit(): void {
    this.loadTimeEntries();
  }

  loadTimeEntries(): void {
    this.loading.set(true);
    this.error.set('');

    this.timeEntryService.getTimeEntries().subscribe({
      next: (timeEntries) => {
        // Handle both array and paginated response
        const entries = Array.isArray(timeEntries) ? timeEntries : (timeEntries.content || []);
        this.timeEntries.set(entries);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading time entries:', err);
        let errorMessage = 'Failed to load time entries';
        
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

  getStatusVariant(status: TimeEntryStatus): BadgeVariant {
    switch (status) {
      case TimeEntryStatus.APPROVED:
        return 'default';
      case TimeEntryStatus.SUBMITTED:
        return 'secondary';
      case TimeEntryStatus.REJECTED:
        return 'destructive';
      default:
        return 'outline';
    }
  }

  canSubmitEntry(entry: TimeEntry): boolean {
    return entry.status === TimeEntryStatus.DRAFT;
  }

  canApproveEntry(entry: TimeEntry): boolean {
    return this.canApprove() && entry.status === TimeEntryStatus.SUBMITTED;
  }

  canEditEntry(entry: TimeEntry): boolean {
    return entry.status === TimeEntryStatus.DRAFT || entry.status === TimeEntryStatus.REJECTED;
  }

  submitEntry(entry: TimeEntry): void {
    if (this.processing()) return;

    this.processing.set(entry.id);
    this.error.set('');

    this.timeEntryService.submitTimeEntry(entry.id).subscribe({
      next: () => {
        this.loadTimeEntries();
        this.processing.set(null);
      },
      error: (err) => {
        console.error('Error submitting time entry:', err);
        let errorMessage = 'Failed to submit time entry';
        
        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
        }
        
        this.error.set(errorMessage);
        this.processing.set(null);
      }
    });
  }

  approveEntry(entry: TimeEntry): void {
    if (this.processing()) return;

    this.processing.set(entry.id);
    this.error.set('');

    this.timeEntryService.approveTimeEntry(entry.id).subscribe({
      next: () => {
        this.loadTimeEntries();
        this.processing.set(null);
      },
      error: (err) => {
        console.error('Error approving time entry:', err);
        let errorMessage = 'Failed to approve time entry';
        
        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
        }
        
        this.error.set(errorMessage);
        this.processing.set(null);
      }
    });
  }

  startRejectEntry(entry: TimeEntry): void {
    this.rejectingEntryId.set(entry.id);
    this.rejectionReason.set('');
  }

  cancelReject(): void {
    this.rejectingEntryId.set(null);
    this.rejectionReason.set('');
  }

  confirmReject(entry: TimeEntry): void {
    if (this.processing()) return;

    this.processing.set(entry.id);
    this.error.set('');

    const reason = this.rejectionReason().trim() || undefined;

    this.timeEntryService.rejectTimeEntry(entry.id, reason).subscribe({
      next: () => {
        this.rejectingEntryId.set(null);
        this.rejectionReason.set('');
        this.loadTimeEntries();
        this.processing.set(null);
      },
      error: (err) => {
        console.error('Error rejecting time entry:', err);
        let errorMessage = 'Failed to reject time entry';
        
        if (err?.details && Array.isArray(err.details) && err.details.length > 0) {
          errorMessage = err.details[0];
        } else if (err?.message) {
          errorMessage = err.message;
        }
        
        this.error.set(errorMessage);
        this.processing.set(null);
      }
    });
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  onTimeEntryCreated(): void {
    this.closeCreateModal();
    this.loadTimeEntries();
  }
}
