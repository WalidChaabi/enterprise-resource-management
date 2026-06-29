import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TimeEntry } from '@/core/models/time-entry.model';
import { TranslatePipe } from '@/core/pipes/translate.pipe';
import { ButtonComponent } from '@/shared/components/ui/button/button.component';

@Component({
  selector: 'app-time-entry-row-actions',
  imports: [CommonModule, RouterModule, TranslatePipe, ButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="flex flex-wrap items-center justify-end gap-2">
      @if (showEdit()) {
        <app-button variant="ghost" size="sm" [routerLink]="['/time-entries', entry().id, 'edit']">
          {{ 'common.edit' | translate }}
        </app-button>
      }
      @if (showSubmit()) {
        <app-button variant="default" size="sm" [disabled]="processing()" (click)="submit.emit(entry())">
          @if (processing()) {
            <div class="mr-2 h-3 w-3 animate-spin rounded-full border-2 border-current border-t-transparent"></div>
          }
          {{ 'timeEntries.submit' | translate }}
        </app-button>
      }
      @if (showApprove()) {
        <app-button variant="default" size="sm" [disabled]="processing()" (click)="approve.emit(entry())">
          @if (processing()) {
            <div class="mr-2 h-3 w-3 animate-spin rounded-full border-2 border-current border-t-transparent"></div>
          }
          {{ 'timeEntries.approve' | translate }}
        </app-button>
        <app-button variant="destructive" size="sm" [disabled]="processing()" (click)="reject.emit(entry())">
          {{ 'timeEntries.reject' | translate }}
        </app-button>
      }
    </div>
  `,
})
export class TimeEntryRowActionsComponent {
  readonly entry = input.required<TimeEntry>();
  readonly showEdit = input(false);
  readonly showSubmit = input(false);
  readonly showApprove = input(false);
  readonly processing = input(false);

  readonly submit = output<TimeEntry>();
  readonly approve = output<TimeEntry>();
  readonly reject = output<TimeEntry>();
}
