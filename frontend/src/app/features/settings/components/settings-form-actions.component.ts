import { Component, input, output } from '@angular/core';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-settings-form-actions',
  imports: [TranslatePipe, ButtonComponent, LucideAngularModule],
  template: `
    <div class="flex justify-end gap-2">
      <app-button
        type="button"
        variant="outline"
        [disabled]="saving()"
        (click)="cancelled.emit()">
        {{ 'common.cancel' | translate }}
      </app-button>
      <app-button
        type="submit"
        [disabled]="disabled() || saving()"
        [attr.aria-busy]="saving()">
        @if (saving()) {
          <lucide-icon name="loader-2" [size]="16" class="animate-spin" />
        }
        {{ 'common.save' | translate }}
      </app-button>
    </div>
  `,
})
export class SettingsFormActionsComponent {
  readonly saving = input(false);
  readonly disabled = input(false);
  readonly cancelled = output<void>();
}
