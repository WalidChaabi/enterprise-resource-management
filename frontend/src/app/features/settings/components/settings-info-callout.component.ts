import { Component, input } from '@angular/core';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-settings-info-callout',
  imports: [TranslatePipe, LucideAngularModule],
  host: { class: 'block' },
  template: `
    <div
      class="mb-1 flex gap-3 rounded-lg border border-primary/20 bg-primary/5 px-4 py-3.5"
      role="status">
      <lucide-icon name="info" [size]="18" class="mt-0.5 shrink-0 text-primary" aria-hidden="true" />
      <div class="min-w-0 space-y-1">
        <p class="text-sm font-medium leading-snug text-foreground">{{ titleKey() | translate }}</p>
        @if (descriptionKey()) {
          <p class="settings-description leading-relaxed">{{ descriptionKey() | translate }}</p>
        }
      </div>
    </div>
  `,
})
export class SettingsInfoCalloutComponent {
  readonly titleKey = input.required<string>();
  readonly descriptionKey = input<string>('');
}
