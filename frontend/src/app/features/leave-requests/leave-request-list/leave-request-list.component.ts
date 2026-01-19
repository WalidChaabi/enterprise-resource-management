import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LeaveRequestService } from '../../../core/services/leave-request.service';
import { AuthService } from '../../../core/services/auth.service';
import { LeaveRequest, LeaveRequestStatus } from '../../../core/models/leave-request.model';
import { ROLES } from '../../../core/utils/constants';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { BadgeComponent, BadgeVariant } from '../../../shared/components/ui/badge/badge.component';
import { SkeletonTableComponent } from '../../../shared/components/ui/skeleton/skeleton.component';
import { InputComponent } from '../../../shared/components/ui/input/input.component';
import { DialogComponent } from '../../../shared/components/ui/dialog/dialog.component';
import { LeaveRequestFormComponent } from '../leave-request-form/leave-request-form.component';

@Component({
  selector: 'app-leave-request-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, TranslatePipe, ButtonComponent, CardComponent, CardContentComponent, BadgeComponent, SkeletonTableComponent, DatePipe, InputComponent, DialogComponent, LeaveRequestFormComponent],
  templateUrl: './leave-request-list.component.html'
})
export class LeaveRequestListComponent implements OnInit {
  private readonly leaveRequestService = inject(LeaveRequestService);
  private readonly authService = inject(AuthService);

  readonly leaveRequests = signal<LeaveRequest[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string>('');
  readonly processing = signal<string | null>(null);
  readonly rejectingRequestId = signal<string | null>(null);
  readonly rejectionReason = signal<string>('');
  readonly showCreateModal = signal<boolean>(false);

  readonly canApprove = computed(() => {
    return this.authService.hasAnyRole(ROLES.HR_ADMIN, ROLES.ORG_ADMIN, ROLES.DEPARTMENT_MANAGER);
  });

  ngOnInit(): void {
    this.loadLeaveRequests();
  }

  loadLeaveRequests(): void {
    this.loading.set(true);
    this.error.set('');

    this.leaveRequestService.getLeaveRequests().subscribe({
      next: (leaveRequests) => {
        // Handle both array and paginated response
        const requests = Array.isArray(leaveRequests) ? leaveRequests : (leaveRequests.content || []);
        this.leaveRequests.set(requests);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading leave requests:', err);
        let errorMessage = 'Failed to load leave requests';
        
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

  getStatusVariant(status: LeaveRequestStatus): BadgeVariant {
    switch (status) {
      case LeaveRequestStatus.APPROVED:
        return 'default';
      case LeaveRequestStatus.PENDING:
        return 'secondary';
      case LeaveRequestStatus.REJECTED:
      case LeaveRequestStatus.CANCELLED:
        return 'destructive';
      default:
        return 'outline';
    }
  }

  canApproveRequest(request: LeaveRequest): boolean {
    return this.canApprove() && request.status === LeaveRequestStatus.PENDING;
  }

  canCancelRequest(request: LeaveRequest): boolean {
    return request.status === LeaveRequestStatus.PENDING;
  }

  approveRequest(request: LeaveRequest): void {
    if (this.processing()) return;

    this.processing.set(request.id);
    this.error.set('');

    this.leaveRequestService.approveLeaveRequest(request.id).subscribe({
      next: () => {
        this.loadLeaveRequests();
        this.processing.set(null);
      },
      error: (err) => {
        console.error('Error approving leave request:', err);
        let errorMessage = 'Failed to approve leave request';
        
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

  startRejectRequest(request: LeaveRequest): void {
    this.rejectingRequestId.set(request.id);
    this.rejectionReason.set('');
  }

  cancelReject(): void {
    this.rejectingRequestId.set(null);
    this.rejectionReason.set('');
  }

  confirmReject(request: LeaveRequest): void {
    if (this.processing()) return;

    this.processing.set(request.id);
    this.error.set('');

    const reason = this.rejectionReason().trim() || undefined;

    this.leaveRequestService.rejectLeaveRequest(request.id, reason).subscribe({
      next: () => {
        this.rejectingRequestId.set(null);
        this.rejectionReason.set('');
        this.loadLeaveRequests();
        this.processing.set(null);
      },
      error: (err) => {
        console.error('Error rejecting leave request:', err);
        let errorMessage = 'Failed to reject leave request';
        
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

  cancelRequest(request: LeaveRequest): void {
    if (this.processing()) return;

    if (!confirm('Are you sure you want to cancel this leave request?')) {
      return;
    }

    this.processing.set(request.id);
    this.error.set('');

    this.leaveRequestService.cancelLeaveRequest(request.id).subscribe({
      next: () => {
        this.loadLeaveRequests();
        this.processing.set(null);
      },
      error: (err) => {
        console.error('Error cancelling leave request:', err);
        let errorMessage = 'Failed to cancel leave request';
        
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

  onLeaveRequestCreated(): void {
    this.closeCreateModal();
    this.loadLeaveRequests();
  }
}
