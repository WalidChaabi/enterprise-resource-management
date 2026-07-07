import { Component, input } from '@angular/core';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';

@Component({
  selector: 'app-settings-field',
  imports: [TranslatePipe],
  template: `
    <div class="flex flex-col gap-3">
      @if (labelKey()) {
        <label [attr.for]="forId() || null" class="text-sm font-medium leading-snug text-foreground">
          {{ labelKey() | translate }}
        </label>
      }
      <div class="flex flex-col gap-1.5">
        <ng-content />
        @if (hintKey()) {
          <p class="text-xs leading-relaxed text-muted-foreground">
            {{ hintKey() | translate }}
          </p>
        }
      </div>
    </div>
  `,
})
export class SettingsFieldComponent {
  readonly labelKey = input<string>('');
  readonly hintKey = input<string>('');
  readonly forId = input<string>('');
}
