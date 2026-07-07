import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  Check,
  CirclePause,
  LucideAngularModule,
  MoreHorizontal,
  Pencil,
  X,
} from 'lucide-angular';
import { LeaveRequest } from '@/core/models/leave-request.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { ButtonDirective } from '@/shared/components/ui/button/button.directive';
import {
  DropdownMenuComponent,
  DropdownMenuItemComponent,
  DropdownMenuTriggerDirective,
} from '@/shared/components/ui/dropdown-menu/dropdown-menu.component';

@Component({
  selector: 'app-leave-request-row-actions',
  imports: [
    CommonModule,
    RouterModule,
    TranslatePipe,
    LucideAngularModule,
    ButtonDirective,
    DropdownMenuComponent,
    DropdownMenuTriggerDirective,
    DropdownMenuItemComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-dropdown-menu>
      <button
        dropdownTrigger
        app-button
        variant="ghost"
        size="icon"
        type="button"
        class="h-8 w-8"
        [disabled]="processing()"
        [attr.aria-label]="'common.actions' | translate">
        @if (processing()) {
          <div class="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"></div>
        } @else {
          <lucide-icon [img]="MoreHorizontal" [size]="16" />
        }
        <span class="sr-only">{{ 'common.actions' | translate }}</span>
      </button>
      <div dropdownContent class="w-48">
        @if (showEdit()) {
          <app-dropdown-menu-item [routerLink]="['/leave-requests', request().id, 'edit']">
            <lucide-icon [img]="Pencil" [size]="16" />
            {{ 'common.edit' | translate }}
          </app-dropdown-menu-item>
        }
        @if (showApprove()) {
          <app-dropdown-menu-item (activated)="approve.emit(request())">
            <lucide-icon [img]="Check" [size]="16" />
            {{ 'leaveRequests.approve' | translate }}
          </app-dropdown-menu-item>
          <app-dropdown-menu-item class="text-destructive focus:text-destructive" (activated)="reject.emit(request())">
            <lucide-icon [img]="X" [size]="16" />
            {{ 'leaveRequests.reject' | translate }}
          </app-dropdown-menu-item>
        }
        @if (showCancel()) {
          <app-dropdown-menu-item (activated)="cancelRequest.emit(request())">
            <lucide-icon [img]="CirclePause" [size]="16" />
            {{ 'leaveRequests.cancel' | translate }}
          </app-dropdown-menu-item>
        }
      </div>
    </app-dropdown-menu>
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
  readonly cancelRequest = output<LeaveRequest>();

  readonly MoreHorizontal = MoreHorizontal;
  readonly Pencil = Pencil;
  readonly Check = Check;
  readonly X = X;
  readonly CirclePause = CirclePause;
}
