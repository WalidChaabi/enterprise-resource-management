import { Component, input } from '@angular/core';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';

@Component({
  selector: 'app-settings-item',
  imports: [TranslatePipe],
  host: { class: 'block' },
  template: `
    <div class="flex items-center justify-between gap-4 py-3.5 first:pt-0 last:pb-0">
      <div class="min-w-0 flex-1 space-y-1">
        <p class="settings-item-title">{{ labelKey() | translate }}</p>
        @if (descriptionKey()) {
          <p class="settings-description">{{ descriptionKey() | translate }}</p>
        }
      </div>
      <div class="flex shrink-0 items-center self-center">
        <ng-content />
      </div>
    </div>
  `,
})
export class SettingsItemComponent {
  readonly labelKey = input.required<string>();
  readonly descriptionKey = input<string>('');
}
