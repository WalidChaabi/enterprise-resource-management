import { LucideAngularModule } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LeaveRequestService } from '../../../core/services/leave-request.service';
import { ReportingService } from '../../../core/services/reporting.service';
import { AuthService } from '../../../core/services/auth.service';
import { LeaveRequest } from '../../../core/models/leave-request.model';
import { LeaveBalance } from '../../../core/models/report.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { LeaveRequestFormComponent } from '../leave-request-form/leave-request-form.component';
import { LeaveRequestDataTableComponent } from './leave-request-data-table.component';

@Component({
    selector: 'app-leave-request-list',
    imports: [
        CommonModule,
        FormsModule,
        TranslatePipe,
        LucideAngularModule,
        ButtonComponent,
        CardComponent,
        CardContentComponent,
        SkeletonTableComponent,
        DialogComponent,
        LeaveRequestFormComponent,
        LeaveRequestDataTableComponent
    ],
    templateUrl: './leave-request-list.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LeaveRequestListComponent {
  private readonly leaveRequestService = inject(LeaveRequestService);
  private readonly reportingService = inject(ReportingService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly leaveRequests = signal<LeaveRequest[]>([]);
  readonly myLeaveBalance = signal<LeaveBalance | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly processing = signal<string | null>(null);
  readonly rejectingRequest = signal<LeaveRequest | null>(null);
  readonly cancellingRequest = signal<LeaveRequest | null>(null);
  readonly rejectionReason = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);

  readonly canApprove = computed(() => {
    return this.authService.hasAnyRole(ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER);
  });

  constructor() {
    this.loadLeaveRequests();
    this.loadLeaveBalance();
  }

  loadLeaveBalance(): void {
    this.reportingService.getLeaveBalance().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (balances) => {
        const email = this.authService.getCurrentUser()?.email;
        const mine = balances.find((b) => b.employeeEmail === email) ?? null;
        this.myLeaveBalance.set(mine);
      },
      error: () => {
        this.myLeaveBalance.set(null);
      }
    });
  }

  loadLeaveRequests(): void {
    this.loading.set(true);
    this.error.set('');

    this.leaveRequestService.getLeaveRequests().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (leaveRequests) => {
        const requests = Array.isArray(leaveRequests) ? leaveRequests : (leaveRequests.content || []);
        this.leaveRequests.set(requests);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.error?.details?.[0] || err?.message || 'Failed to load leave requests');
        this.loading.set(false);
      }
    });
  }

  approveRequest(request: LeaveRequest): void {
    if (this.processing()) return;

    this.processing.set(request.id);
    this.error.set('');

    this.leaveRequestService.approveLeaveRequest(request.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.loadLeaveRequests();
        this.processing.set(null);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to approve leave request');
        this.processing.set(null);
      }
    });
  }

  startRejectRequest(request: LeaveRequest): void {
    this.rejectingRequest.set(request);
    this.rejectionReason.set('');
  }

  closeRejectDialog(): void {
    this.rejectingRequest.set(null);
    this.rejectionReason.set('');
  }

  confirmReject(): void {
    const request = this.rejectingRequest();
    if (!request || this.processing()) return;

    this.processing.set(request.id);
    this.error.set('');

    const reason = this.rejectionReason().trim() || undefined;

    this.leaveRequestService.rejectLeaveRequest(request.id, reason).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.closeRejectDialog();
        this.loadLeaveRequests();
        this.processing.set(null);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to reject leave request');
        this.processing.set(null);
      }
    });
  }

  startCancelRequest(request: LeaveRequest): void {
    this.cancellingRequest.set(request);
  }

  closeCancelDialog(): void {
    this.cancellingRequest.set(null);
  }

  confirmCancel(): void {
    const request = this.cancellingRequest();
    if (!request || this.processing()) return;

    this.processing.set(request.id);
    this.error.set('');

    this.leaveRequestService.cancelLeaveRequest(request.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.closeCancelDialog();
        this.loadLeaveRequests();
        this.processing.set(null);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to cancel leave request');
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

  onLeaveRequestCreated(): void {
    this.closeCreateModal();
    this.loadLeaveRequests();
  }
}
