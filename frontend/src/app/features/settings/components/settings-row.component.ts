import { Component, input } from '@angular/core';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';

@Component({
  selector: 'app-settings-row',
  imports: [TranslatePipe],
  template: `
    <div class="flex items-center justify-between gap-4 rounded-lg border border-border/60 bg-muted/20 px-4 py-3">
      <div class="min-w-0 flex-1 space-y-0.5">
        <p class="text-sm font-medium leading-none text-foreground">{{ labelKey() | translate }}</p>
        @if (descriptionKey()) {
          <p class="text-xs leading-relaxed text-muted-foreground">
            {{ descriptionKey() | translate }}
          </p>
        }
      </div>
      <div class="shrink-0">
        <ng-content />
      </div>
    </div>
  `,
})
export class SettingsRowComponent {
  readonly labelKey = input.required<string>();
  readonly descriptionKey = input<string>('');
}
