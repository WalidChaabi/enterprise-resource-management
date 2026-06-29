import { LucideAngularModule } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TimeEntryService } from '../../../core/services/time-entry.service';
import { AuthService } from '../../../core/services/auth.service';
import { TimeEntry, TimeEntryStatus } from '../../../core/models/time-entry.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { TimeEntryFormComponent } from '../time-entry-form/time-entry-form.component';
import { TimeEntryDataTableComponent } from './time-entry-data-table.component';

@Component({
    selector: 'app-time-entry-list',
    imports: [
        CommonModule,
        FormsModule,
        DecimalPipe,
        TranslatePipe,
        LucideAngularModule,
        ButtonComponent,
        CardComponent,
        CardContentComponent,
        SkeletonTableComponent,
        DialogComponent,
        TimeEntryFormComponent,
        TimeEntryDataTableComponent
    ],
    templateUrl: './time-entry-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TimeEntryListComponent {
  private readonly timeEntryService = inject(TimeEntryService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly timeEntries = signal<TimeEntry[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly processing = signal<string | null>(null);
  readonly rejectingEntry = signal<TimeEntry | null>(null);
  readonly rejectionReason = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);

  readonly canApprove = computed(() => {
    return this.authService.hasAnyRole(ROLES.PROJECT_MANAGER, ROLES.ORG_ADMIN, ROLES.HR_ADMIN);
  });

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

  constructor() {
    this.loadTimeEntries();
  }

  loadTimeEntries(): void {
    this.loading.set(true);
    this.error.set('');

    this.timeEntryService.getTimeEntries().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (timeEntries) => {
        const entries = Array.isArray(timeEntries) ? timeEntries : (timeEntries.content || []);
        this.timeEntries.set(entries);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.error?.details?.[0] || err?.message || 'Failed to load time entries');
        this.loading.set(false);
      }
    });
  }

  submitEntry(entry: TimeEntry): void {
    if (this.processing()) return;

    this.processing.set(entry.id);
    this.error.set('');

    this.timeEntryService.submitTimeEntry(entry.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.loadTimeEntries();
        this.processing.set(null);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to submit time entry');
        this.processing.set(null);
      }
    });
  }

  approveEntry(entry: TimeEntry): void {
    if (this.processing()) return;

    this.processing.set(entry.id);
    this.error.set('');

    this.timeEntryService.approveTimeEntry(entry.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.loadTimeEntries();
        this.processing.set(null);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to approve time entry');
        this.processing.set(null);
      }
    });
  }

  startRejectEntry(entry: TimeEntry): void {
    this.rejectingEntry.set(entry);
    this.rejectionReason.set('');
  }

  closeRejectDialog(): void {
    this.rejectingEntry.set(null);
    this.rejectionReason.set('');
  }

  confirmReject(): void {
    const entry = this.rejectingEntry();
    if (!entry || this.processing()) return;

    this.processing.set(entry.id);
    this.error.set('');

    const reason = this.rejectionReason().trim() || undefined;

    this.timeEntryService.rejectTimeEntry(entry.id, reason).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.closeRejectDialog();
        this.loadTimeEntries();
        this.processing.set(null);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to reject time entry');
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
