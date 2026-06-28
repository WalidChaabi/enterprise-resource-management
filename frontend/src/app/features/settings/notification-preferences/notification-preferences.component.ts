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
        <h2 class="text-xl font-semibold text-foreground">Notification Preferences</h2>
        <p class="text-sm text-muted-foreground">Choose which updates and notifications you want to receive.</p>
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
          <div class="space-y-6">
            <!-- Email Notifications Card -->
            <app-card class="group hover:shadow-md transition-shadow duration-200">
              <app-card-header>
                <div class="flex items-center gap-3">
                  <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-500/10 text-blue-500">
                    <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <app-card-title>Email Notifications</app-card-title>
                </div>
              </app-card-header>
              <app-card-content class="space-y-4">
                <div class="flex items-center justify-between rounded-lg border border-border bg-muted/30 p-4 transition-colors hover:bg-muted/50">
                  <div class="flex items-center gap-4 flex-1">
                    <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-500/10 text-blue-500">
                      <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <div class="flex-1">
                      <label class="text-sm font-medium text-foreground cursor-pointer">Email Notifications</label>
                      <p class="text-xs text-muted-foreground mt-0.5">Receive notifications via email</p>
                    </div>
                  </div>
                  <label class="relative inline-flex cursor-pointer items-center">
                    <input
                      type="checkbox"
                      formControlName="emailNotifications"
                      class="peer sr-only">
                    <div class="peer h-6 w-11 rounded-full bg-muted transition-colors peer-checked:bg-primary peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary peer-focus:ring-offset-2">
                      <div class="absolute left-[2px] top-[2px] h-5 w-5 rounded-full bg-background shadow-sm transition-transform peer-checked:translate-x-5"></div>
                    </div>
                  </label>
                </div>

                <div class="flex items-center justify-between rounded-lg border border-border bg-muted/30 p-4 transition-colors hover:bg-muted/50">
                  <div class="flex items-center gap-4 flex-1">
                    <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-purple-500/10 text-purple-500">
                      <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                      </svg>
                    </div>
                    <div class="flex-1">
                      <label class="text-sm font-medium text-foreground cursor-pointer">Project Updates</label>
                      <p class="text-xs text-muted-foreground mt-0.5">Get notified about project changes</p>
                    </div>
                  </div>
                  <label class="relative inline-flex cursor-pointer items-center">
                    <input
                      type="checkbox"
                      formControlName="projectUpdates"
                      class="peer sr-only">
                    <div class="peer h-6 w-11 rounded-full bg-muted transition-colors peer-checked:bg-primary peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary peer-focus:ring-offset-2">
                      <div class="absolute left-[2px] top-[2px] h-5 w-5 rounded-full bg-background shadow-sm transition-transform peer-checked:translate-x-5"></div>
                    </div>
                  </label>
                </div>

                <div class="flex items-center justify-between rounded-lg border border-border bg-muted/30 p-4 transition-colors hover:bg-muted/50">
                  <div class="flex items-center gap-4 flex-1">
                    <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-orange-500/10 text-orange-500">
                      <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                      </svg>
                    </div>
                    <div class="flex-1">
                      <label class="text-sm font-medium text-foreground cursor-pointer">Task Assignments</label>
                      <p class="text-xs text-muted-foreground mt-0.5">Receive notifications when tasks are assigned to you</p>
                    </div>
                  </div>
                  <label class="relative inline-flex cursor-pointer items-center">
                    <input
                      type="checkbox"
                      formControlName="taskAssignments"
                      class="peer sr-only">
                    <div class="peer h-6 w-11 rounded-full bg-muted transition-colors peer-checked:bg-primary peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary peer-focus:ring-offset-2">
                      <div class="absolute left-[2px] top-[2px] h-5 w-5 rounded-full bg-background shadow-sm transition-transform peer-checked:translate-x-5"></div>
                    </div>
                  </label>
                </div>
              </app-card-content>
            </app-card>

            <!-- Push Notifications Card -->
            <app-card class="group hover:shadow-md transition-shadow duration-200">
              <app-card-header>
                <div class="flex items-center gap-3">
                  <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-green-500/10 text-green-500">
                    <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                    </svg>
                  </div>
                  <app-card-title>Push Notifications</app-card-title>
                </div>
              </app-card-header>
              <app-card-content>
                <div class="flex items-center justify-between rounded-lg border border-border bg-muted/30 p-4 transition-colors hover:bg-muted/50">
                  <div class="flex items-center gap-4 flex-1">
                    <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-green-500/10 text-green-500">
                      <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                      </svg>
                    </div>
                    <div class="flex-1">
                      <label class="text-sm font-medium text-foreground cursor-pointer">Push Notifications</label>
                      <p class="text-xs text-muted-foreground mt-0.5">Receive browser push notifications</p>
                    </div>
                  </div>
                  <label class="relative inline-flex cursor-pointer items-center">
                    <input
                      type="checkbox"
                      formControlName="pushNotifications"
                      class="peer sr-only">
                    <div class="peer h-6 w-11 rounded-full bg-muted transition-colors peer-checked:bg-primary peer-focus:outline-none peer-focus:ring-2 peer-focus:ring-primary peer-focus:ring-offset-2">
                      <div class="absolute left-[2px] top-[2px] h-5 w-5 rounded-full bg-background shadow-sm transition-transform peer-checked:translate-x-5"></div>
                    </div>
                  </label>
                </div>
              </app-card-content>
            </app-card>

            <!-- Action Buttons -->
            <div class="flex justify-end gap-3 pt-2">
              <button
                type="button"
                class="inline-flex h-10 items-center justify-center rounded-md border border-input bg-background px-6 text-sm font-medium hover:bg-accent hover:text-accent-foreground transition-colors"
                (click)="resetForm()">
                Cancel
              </button>
              <button
                type="submit"
                [disabled]="saving()"
                class="inline-flex h-10 items-center justify-center rounded-md bg-primary px-6 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:pointer-events-none disabled:opacity-50 transition-colors">
                @if (saving()) {
                  <span class="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"></span>
                }
                Save Preferences
              </button>
            </div>
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
    emailNotifications: [false],
    pushNotifications: [false],
    projectUpdates: [false],
    taskAssignments: [false]
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
          emailNotifications: prefs.emailNotifications,
          pushNotifications: prefs.pushNotifications,
          projectUpdates: prefs.projectUpdates,
          taskAssignments: prefs.taskAssignments
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
      emailNotifications: this.preferencesForm.value.emailNotifications,
      pushNotifications: this.preferencesForm.value.pushNotifications,
      projectUpdates: this.preferencesForm.value.projectUpdates,
      taskAssignments: this.preferencesForm.value.taskAssignments
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
        emailNotifications: prefs.emailNotifications,
        pushNotifications: prefs.pushNotifications,
        projectUpdates: prefs.projectUpdates,
        taskAssignments: prefs.taskAssignments
      });
    }
  }
}
