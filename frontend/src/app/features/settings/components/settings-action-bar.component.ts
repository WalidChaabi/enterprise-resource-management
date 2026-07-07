import { Component, input, output } from '@angular/core';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-settings-action-bar',
  imports: [TranslatePipe, ButtonComponent, LucideAngularModule],
  template: `
    @if (visible()) {
      <div
        class="settings-action-bar animate-in slide-in-from-bottom-2 fade-in duration-200"
        role="region"
        [attr.aria-label]="'settings.actionBar.label' | translate">
        <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div class="min-w-0">
            <p class="text-sm font-medium text-foreground">{{ 'settings.actionBar.unsavedTitle' | translate }}</p>
            <p class="settings-description">{{ 'settings.actionBar.unsavedDescription' | translate }}</p>
          </div>
          <div class="flex shrink-0 justify-end gap-2">
            <app-button
              type="button"
              variant="outline"
              [disabled]="saving()"
              (click)="cancelled.emit()">
              {{ 'common.cancel' | translate }}
            </app-button>
            <app-button
              type="button"
              [disabled]="disabled() || saving()"
              [attr.aria-busy]="saving()"
              (click)="save.emit()">
              @if (saving()) {
                <lucide-icon name="loader-2" [size]="16" class="animate-spin" />
              }
              {{ 'settings.actionBar.saveChanges' | translate }}
            </app-button>
          </div>
        </div>
      </div>
    }
  `,
})
export class SettingsActionBarComponent {
  readonly visible = input(false);
  readonly saving = input(false);
  readonly disabled = input(false);
  readonly cancelled = output<void>();
  readonly save = output<void>();
}
