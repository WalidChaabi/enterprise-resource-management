import { LucideAngularModule } from 'lucide-angular';
import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { SettingsService } from '../../../core/services/settings.service';
import { NotificationPreferences, NotificationPreferencesUpdate } from '../../../core/models/settings.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent, CardFooterComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';
import {
  SettingsPageLayoutComponent,
  SettingsRowComponent,
  SettingsFormActionsComponent,
} from '../components';
import { SwitchComponent } from '../../../shared/components/ui/switch/switch.component';

@Component({
    selector: 'app-notification-preferences',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        TranslatePipe,
        LucideAngularModule,
        CardComponent,
        CardHeaderComponent,
        CardTitleComponent,
        CardContentComponent,
        CardFooterComponent,
        AlertComponent,
        SettingsPageLayoutComponent,
        SettingsRowComponent,
        SettingsFormActionsComponent,
        SwitchComponent,
    ],
    template: `
    <app-settings-page-layout>
      @if (error()) {
        <app-alert variant="destructive" role="alert">{{ error() }}</app-alert>
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
              <app-settings-row
                labelKey="settings.notifications.emailEnabled"
                descriptionKey="settings.notifications.emailEnabledHint">
                <app-switch formControlName="emailEnabled" />
              </app-settings-row>

              <app-settings-row
                labelKey="settings.notifications.inAppEnabled"
                descriptionKey="settings.notifications.inAppEnabledHint">
                <app-switch formControlName="inAppEnabled" />
              </app-settings-row>
            </app-card-content>
            <app-card-footer class="justify-end">
              <app-settings-form-actions
                [saving]="saving()"
                (cancelled)="resetForm()" />
            </app-card-footer>
          </app-card>
        </form>
      }
    </app-settings-page-layout>
  `
})
export class NotificationPreferencesComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);
  private readonly fb = inject(FormBuilder);
  private readonly toastService = inject(ToastService);

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
        this.toastService.successI18n('settings.toast.notificationsSaved', 'settings.toast.notificationsSavedDescription');
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
