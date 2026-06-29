import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule } from 'lucide-angular';
import { LeaveRequest } from '@/core/models/leave-request.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { ButtonComponent } from '@/shared/components/ui/button/button.component';

@Component({
  selector: 'app-leave-request-row-actions',
  imports: [CommonModule, RouterModule, LucideAngularModule, TranslatePipe, ButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="flex flex-wrap items-center justify-end gap-2">
      @if (showEdit()) {
        <app-button variant="ghost" size="sm" [routerLink]="['/leave-requests', request().id, 'edit']">
          {{ 'common.edit' | translate }}
        </app-button>
      }
      @if (showApprove()) {
        <app-button
          variant="default"
          size="sm"
          [disabled]="processing()"
          (click)="approve.emit(request())">
          @if (processing()) {
            <div class="mr-2 h-3 w-3 animate-spin rounded-full border-2 border-current border-t-transparent"></div>
          }
          {{ 'leaveRequests.approve' | translate }}
        </app-button>
        <app-button variant="destructive" size="sm" [disabled]="processing()" (click)="reject.emit(request())">
          {{ 'leaveRequests.reject' | translate }}
        </app-button>
      }
      @if (showCancel()) {
        <app-button variant="outline" size="sm" [disabled]="processing()" (click)="cancel.emit(request())">
          {{ 'leaveRequests.cancel' | translate }}
        </app-button>
      }
    </div>
  `,
})
export class LeaveRequestRowActionsComponent {
  readonly request = input.required<LeaveRequest>();
  readonly showEdit = input(false);
  readonly showApprove = input(false);
  readonly showCancel = input(false);
  readonly processing = input(false);

  readonly approve = output<LeaveRequest>();
  readonly reject = output<LeaveRequest>();
  readonly cancel = output<LeaveRequest>();
}
