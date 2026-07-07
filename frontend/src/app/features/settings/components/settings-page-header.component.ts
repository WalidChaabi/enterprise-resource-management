import { Component, input } from '@angular/core';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';

@Component({
  selector: 'app-settings-page-header',
  imports: [TranslatePipe],
  template: `
    <div class="space-y-1.5 pb-1">
      <h2 class="text-xl font-semibold tracking-tight text-foreground">
        {{ titleKey() | translate }}
      </h2>
      @if (subtitleKey()) {
        <p class="text-sm leading-relaxed text-muted-foreground">
          {{ subtitleKey() | translate }}
        </p>
      }
    </div>
  `,
})
export class SettingsPageHeaderComponent {
  readonly titleKey = input.required<string>();
  readonly subtitleKey = input<string>('');
}
