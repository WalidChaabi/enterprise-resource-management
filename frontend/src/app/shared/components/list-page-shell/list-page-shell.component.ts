import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-list-page-shell',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="space-y-5">
      <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <ng-content select="[shell-heading]" />
        </div>
        <ng-content select="[shell-actions]" />
      </div>

      @if (error()) {
        <div class="flex items-start gap-3 rounded-lg border border-destructive/20 bg-destructive/5 p-4">
          <lucide-icon name="alert-circle" [size]="20" class="mt-0.5 flex-shrink-0 text-destructive" />
          <p class="text-sm font-medium text-foreground">{{ error() }}</p>
        </div>
      }

      <ng-content select="[shell-banner]" />

      @if (loading()) {
        <ng-content select="[shell-loading]" />
      } @else if (empty()) {
        <ng-content select="[shell-empty]" />
      } @else {
        <div class="space-y-5">
          <ng-content select="[shell-stats]" />
          <ng-content select="[shell-filters]" />
          <ng-content />
        </div>
      }
    </div>
  `,
})
export class ListPageShellComponent {
  readonly loading = input(false);
  readonly empty = input(false);
  readonly error = input('');
}
