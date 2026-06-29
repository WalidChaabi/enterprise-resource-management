import { LucideAngularModule } from 'lucide-angular';
import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { SettingsService } from '../../../core/services/settings.service';
import { NotificationPreferences, NotificationPreferencesUpdate } from '../../../core/models/settings.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
    selector: 'app-notification-preferences',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        TranslatePipe,
        LucideAngularModule,
        ButtonComponent,
        CardComponent,
        CardHeaderComponent,
        CardTitleComponent,
        CardContentComponent,
        AlertComponent
    ],
    template: `
    <div class="mt-6 space-y-6">
      <div>
        <h2 class="text-xl font-semibold text-foreground">{{ 'settings.notifications.title' | translate }}</h2>
        <p class="text-sm text-muted-foreground">{{ 'settings.notifications.subtitle' | translate }}</p>
      </div>

      @if (error()) {
        <app-alert variant="destructive">{{ error() }}</app-alert>
      }

      @if (loading()) {
        <div class="flex items-center justify-center py-12">
          <div class="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      } @else {
        <form [formGroup]="preferencesForm" (ngSubmit)="onSubmit()">
          <app-card>
            <app-card-header>
              <app-card-title>{{ 'settings.notifications.channels' | translate }}</app-card-title>
            </app-card-header>
            <app-card-content class="space-y-4">
              <div class="flex items-center justify-between rounded-lg border border-border bg-muted/30 p-4">
                <div class="flex-1 pr-4">
                  <label class="text-sm font-medium text-foreground">{{ 'settings.notifications.emailEnabled' | translate }}</label>
                  <p class="text-xs text-muted-foreground mt-0.5">{{ 'settings.notifications.emailEnabledHint' | translate }}</p>
                </div>
                <label class="relative inline-flex cursor-pointer items-center">
                  <input type="checkbox" formControlName="emailEnabled" class="peer sr-only">
                  <div class="peer h-6 w-11 rounded-full bg-muted transition-colors peer-checked:bg-primary">
                    <div class="absolute left-[2px] top-[2px] h-5 w-5 rounded-full bg-background shadow-sm transition-transform peer-checked:translate-x-5"></div>
                  </div>
                </label>
              </div>

              <div class="flex items-center justify-between rounded-lg border border-border bg-muted/30 p-4">
                <div class="flex-1 pr-4">
                  <label class="text-sm font-medium text-foreground">{{ 'settings.notifications.inAppEnabled' | translate }}</label>
                  <p class="text-xs text-muted-foreground mt-0.5">{{ 'settings.notifications.inAppEnabledHint' | translate }}</p>
                </div>
                <label class="relative inline-flex cursor-pointer items-center">
                  <input type="checkbox" formControlName="inAppEnabled" class="peer sr-only">
                  <div class="peer h-6 w-11 rounded-full bg-muted transition-colors peer-checked:bg-primary">
                    <div class="absolute left-[2px] top-[2px] h-5 w-5 rounded-full bg-background shadow-sm transition-transform peer-checked:translate-x-5"></div>
                  </div>
                </label>
              </div>
            </app-card-content>
          </app-card>

          <div class="flex justify-end gap-3 pt-4">
            <button
              type="button"
              class="inline-flex h-10 items-center justify-center rounded-md border border-input bg-background px-6 text-sm font-medium hover:bg-accent hover:text-accent-foreground transition-colors"
              (click)="resetForm()">
              {{ 'common.cancel' | translate }}
            </button>
            <app-button type="submit" [disabled]="saving()">
              @if (saving()) {
                <lucide-icon name="loader-2" [size]="16" class="animate-spin mr-2" />
              }
              {{ 'common.save' | translate }}
            </app-button>
          </div>
        </form>
      }
    </div>
  `
})
export class NotificationPreferencesComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly preferences = signal<NotificationPreferences | null>(null);

  preferencesForm: FormGroup = this.fb.group({
    emailEnabled: [true],
    inAppEnabled: [true]
  });

  ngOnInit(): void {
    this.loadPreferences();
  }

  loadPreferences(): void {
    this.loading.set(true);
    this.error.set('');

    this.settingsService.getNotificationPreferences().subscribe({
      next: (prefs) => {
        this.preferences.set(prefs);
        this.preferencesForm.patchValue({
          emailEnabled: prefs.emailEnabled,
          inAppEnabled: prefs.inAppEnabled
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to load notification preferences');
        this.loading.set(false);
      }
    });
  }

  onSubmit(): void {
    this.saving.set(true);
    this.error.set('');

    const update: NotificationPreferencesUpdate = {
      emailEnabled: this.preferencesForm.value.emailEnabled,
      inAppEnabled: this.preferencesForm.value.inAppEnabled
    };

    this.settingsService.updateNotificationPreferences(update).subscribe({
      next: (updated) => {
        this.preferences.set(updated);
        this.saving.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to update notification preferences');
        this.saving.set(false);
      }
    });
  }

  resetForm(): void {
    const prefs = this.preferences();
    if (prefs) {
      this.preferencesForm.patchValue({
        emailEnabled: prefs.emailEnabled,
        inAppEnabled: prefs.inAppEnabled
      });
    }
  }
}
