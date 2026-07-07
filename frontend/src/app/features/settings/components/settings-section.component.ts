import { Component, input } from '@angular/core';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';

/** Optional in-card section label when a card contains multiple logical groups. */
@Component({
  selector: 'app-settings-section',
  imports: [TranslatePipe],
  host: { class: 'block border-t border-border/60 pt-4 first:border-t-0 first:pt-0' },
  template: `
    @if (titleKey()) {
      <div class="mb-3 space-y-0.5">
        <p class="text-sm font-semibold text-foreground">{{ titleKey() | translate }}</p>
        @if (descriptionKey()) {
          <p class="settings-description">{{ descriptionKey() | translate }}</p>
        }
      </div>
    }
    <ng-content />
  `,
})
export class SettingsSectionComponent {
  readonly titleKey = input<string>('');
  readonly descriptionKey = input<string>('');
}
