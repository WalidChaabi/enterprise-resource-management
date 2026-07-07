import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  Check,
  LucideAngularModule,
  MoreHorizontal,
  Pencil,
  Send,
  X,
} from 'lucide-angular';
import { TimeEntry } from '@/core/models/time-entry.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { ButtonDirective } from '@/shared/components/ui/button/button.directive';
import {
  DropdownMenuComponent,
  DropdownMenuItemComponent,
  DropdownMenuTriggerDirective,
} from '@/shared/components/ui/dropdown-menu/dropdown-menu.component';

@Component({
  selector: 'app-time-entry-row-actions',
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
          <app-dropdown-menu-item [routerLink]="['/time-entries', entry().id, 'edit']">
            <lucide-icon [img]="Pencil" [size]="16" />
            {{ 'common.edit' | translate }}
          </app-dropdown-menu-item>
        }
        @if (showSubmit()) {
          <app-dropdown-menu-item (activated)="submitEntry.emit(entry())">
            <lucide-icon [img]="Send" [size]="16" />
            {{ 'timeEntries.submit' | translate }}
          </app-dropdown-menu-item>
        }
        @if (showApprove()) {
          <app-dropdown-menu-item (activated)="approve.emit(entry())">
            <lucide-icon [img]="Check" [size]="16" />
            {{ 'timeEntries.approve' | translate }}
          </app-dropdown-menu-item>
          <app-dropdown-menu-item class="text-destructive focus:text-destructive" (activated)="reject.emit(entry())">
            <lucide-icon [img]="X" [size]="16" />
            {{ 'timeEntries.reject' | translate }}
          </app-dropdown-menu-item>
        }
      </div>
    </app-dropdown-menu>
  `,
})
export class TimeEntryRowActionsComponent {
  readonly entry = input.required<TimeEntry>();
  readonly showEdit = input(false);
  readonly showSubmit = input(false);
  readonly showApprove = input(false);
  readonly processing = input(false);

  readonly submitEntry = output<TimeEntry>();
  readonly approve = output<TimeEntry>();
  readonly reject = output<TimeEntry>();

  readonly MoreHorizontal = MoreHorizontal;
  readonly Pencil = Pencil;
  readonly Send = Send;
  readonly Check = Check;
  readonly X = X;
}
