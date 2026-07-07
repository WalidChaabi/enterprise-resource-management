import { LucideAngularModule } from 'lucide-angular';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LeaveRequestService } from '../../../core/services/leave-request.service';
import { ReportingService } from '../../../core/services/reporting.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  LeaveRequest,
  LeaveRequestStatus,
  LeaveRequestStatusFilter,
} from '../../../core/models/leave-request.model';
import { LeaveBalance } from '../../../core/models/report.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { TranslationService } from '../../../core/services/translation.service';
import { ConfirmDialogService } from '../../../shared/components/ui/alert-dialog';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { EmptyStateComponent } from '../../../shared/components/ui/empty-state/empty-state.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { ListPageShellComponent } from '../../../shared/components/list-page-shell/list-page-shell.component';
import { LeaveRequestFormComponent } from '../leave-request-form/leave-request-form.component';
import { LeaveRequestDataTableComponent } from './leave-request-data-table.component';
import {
  TabsComponent,
  TabsContentComponent,
  TabsListComponent,
  TabsTriggerComponent,
} from '../../../shared/components/ui/tabs';

@Component({
  selector: 'app-leave-request-list',
  imports: [
    CommonModule,
    FormsModule,
    DatePipe,
    TranslatePipe,
    LucideAngularModule,
    ButtonComponent,
    CardComponent,
    CardContentComponent,
    SkeletonTableComponent,
    EmptyStateComponent,
    DialogComponent,
    ListPageShellComponent,
    LeaveRequestFormComponent,
    LeaveRequestDataTableComponent,
    TabsComponent,
    TabsListComponent,
    TabsTriggerComponent,
    TabsContentComponent,
  ],
  templateUrl: './leave-request-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LeaveRequestListComponent {
  private readonly leaveRequestService = inject(LeaveRequestService);
  private readonly reportingService = inject(ReportingService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly toastService = inject(ToastService);
  private readonly translation = inject(TranslationService);

  readonly leaveRequests = signal<LeaveRequest[]>([]);
  readonly myLeaveBalance = signal<LeaveBalance | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly processing = signal<string | null>(null);
  readonly rejectingRequest = signal<LeaveRequest | null>(null);
  readonly rejectionReason = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);
  readonly statusFilter = signal<LeaveRequestStatusFilter>('ALL');
  protected readonly LeaveRequestStatus = LeaveRequestStatus;

  readonly statusFilters: { value: LeaveRequestStatusFilter; labelKey: string }[] = [
    { value: 'ALL', labelKey: 'leaveRequests.filters.all' },
    { value: LeaveRequestStatus.PENDING, labelKey: 'leaveRequests.statuses.pending' },
    { value: LeaveRequestStatus.APPROVED, labelKey: 'leaveRequests.statuses.approved' },
    { value: LeaveRequestStatus.REJECTED, labelKey: 'leaveRequests.statuses.rejected' },
    { value: LeaveRequestStatus.CANCELLED, labelKey: 'leaveRequests.statuses.cancelled' },
  ];

  readonly canApprove = computed(() =>
    this.authService.hasAnyRole(ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER),
  );

  readonly requestsByStatus = computed(() => {
    const requests = this.leaveRequests();
    return {
      pending: requests.filter((r) => r.status === LeaveRequestStatus.PENDING),
      approved: requests.filter((r) => r.status === LeaveRequestStatus.APPROVED),
      rejected: requests.filter((r) => r.status === LeaveRequestStatus.REJECTED),
      cancelled: requests.filter((r) => r.status === LeaveRequestStatus.CANCELLED),
    };
  });

  readonly daysByStatus = computed(() => ({
    pending: this.requestsByStatus().pending.reduce((sum, r) => sum + r.days, 0),
    approved: this.requestsByStatus().approved.reduce((sum, r) => sum + r.days, 0),
    rejected: this.requestsByStatus().rejected.reduce((sum, r) => sum + r.days, 0),
  }));

  readonly pendingApprovalCount = computed(() => this.requestsByStatus().pending.length);

  readonly filteredCount = computed(() => {
    const filter = this.statusFilter();
    if (filter === 'ALL') return this.leaveRequests().length;
    return this.leaveRequests().filter((request) => request.status === filter).length;
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
      },
    });
  }

  loadLeaveRequests(): void {
    this.loading.set(true);
    this.error.set('');

    this.leaveRequestService.getLeaveRequests().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (leaveRequests) => {
        this.leaveRequests.set(leaveRequests);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.error?.details?.[0] || err?.message || 'Failed to load leave requests');
        this.loading.set(false);
      },
    });
  }

  setStatusFilter(filter: LeaveRequestStatusFilter): void {
    this.statusFilter.set(filter);
  }

  onStatusTabChange(value: string): void {
    this.setStatusFilter(value as LeaveRequestStatusFilter);
  }

  isStatusTabActive(filter: LeaveRequestStatusFilter): boolean {
    return this.statusFilter() === filter;
  }

  statusCount(filter: LeaveRequestStatusFilter): number {
    if (filter === 'ALL') {
      return this.leaveRequests().length;
    }
    return this.leaveRequests().filter((request) => request.status === filter).length;
  }

  approveRequest(request: LeaveRequest): void {
    if (this.processing()) return;

    this.processing.set(request.id);
    this.error.set('');

    this.leaveRequestService.approveLeaveRequest(request.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.toastService.successI18n('leaveRequests.toast.approvedTitle', 'leaveRequests.toast.approvedDescription');
        this.loadLeaveRequests();
        this.loadLeaveBalance();
        this.processing.set(null);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to approve leave request');
        this.processing.set(null);
      },
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
        this.toastService.successI18n('leaveRequests.toast.rejectedTitle', 'leaveRequests.toast.rejectedDescription');
        this.closeRejectDialog();
        this.loadLeaveRequests();
        this.processing.set(null);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to reject leave request');
        this.processing.set(null);
      },
    });
  }

  async startCancelRequest(request: LeaveRequest): Promise<void> {
    const confirmed = await this.confirmDialog.open({
      title: this.translation.translate('leaveRequests.cancel'),
      description: this.translation.translate('leaveRequests.cancelConfirm'),
      confirmLabel: this.translation.translate('leaveRequests.confirmCancel'),
      cancelLabel: this.translation.translate('common.cancel'),
      variant: 'destructive',
    });
    if (!confirmed || this.processing()) {
      return;
    }

    this.processing.set(request.id);
    this.error.set('');

    this.leaveRequestService.cancelLeaveRequest(request.id).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.toastService.successI18n('leaveRequests.toast.cancelledTitle', 'leaveRequests.toast.cancelledDescription');
        this.loadLeaveRequests();
        this.loadLeaveBalance();
        this.processing.set(null);
      },
      error: (err) => {
        this.error.set(err?.details?.[0] || err?.message || 'Failed to cancel leave request');
        this.processing.set(null);
      },
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
    this.toastService.successI18n('leaveRequests.toast.createdTitle', 'leaveRequests.toast.createdDescription');
    this.loadLeaveRequests();
    this.loadLeaveBalance();
  }
}
