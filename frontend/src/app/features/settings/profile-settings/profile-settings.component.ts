import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { SettingsService } from '../../../core/services/settings.service';
import { ProfileSettings, ProfileSettingsUpdate } from '../../../core/models/settings.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent, CardFooterComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { InputComponent } from '../../../shared/components/ui/input/input.component';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';
import {
  SettingsPageLayoutComponent,
  SettingsFieldComponent,
  SettingsFormActionsComponent,
} from '../components';

@Component({
    selector: 'app-profile-settings',
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
        TranslatePipe,
        CardComponent,
        CardHeaderComponent,
        CardTitleComponent,
        CardContentComponent,
        CardFooterComponent,
        AlertComponent,
        InputComponent,
        SettingsPageLayoutComponent,
        SettingsFieldComponent,
        SettingsFormActionsComponent,
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
        <form [formGroup]="profileForm" (ngSubmit)="onSubmit()">
          <app-card>
            <app-card-header>
              <app-card-title>{{ 'settings.profile.personalInfoTitle' | translate }}</app-card-title>
            </app-card-header>
            <app-card-content class="space-y-4">
              <app-settings-field
                labelKey="settings.profile.email"
                hintKey="settings.profile.emailHint">
                <app-input
                  type="email"
                  [disabled]="true"
                  [ngModel]="profile()?.email ?? ''"
                  [ngModelOptions]="{ standalone: true }"
                  class="bg-muted text-muted-foreground" />
              </app-settings-field>

              <div class="grid gap-4 sm:grid-cols-2">
                <app-settings-field
                  labelKey="settings.profile.firstName"
                  forId="firstName">
                  <app-input id="firstName" formControlName="firstName" />
                  @if (profileForm.get('firstName')?.hasError('required') && profileForm.get('firstName')?.touched) {
                    <p class="text-xs text-destructive">{{ 'settings.profile.firstNameRequired' | translate }}</p>
                  }
                </app-settings-field>

                <app-settings-field
                  labelKey="settings.profile.lastName"
                  forId="lastName">
                  <app-input id="lastName" formControlName="lastName" />
                  @if (profileForm.get('lastName')?.hasError('required') && profileForm.get('lastName')?.touched) {
                    <p class="text-xs text-destructive">{{ 'settings.profile.lastNameRequired' | translate }}</p>
                  }
                </app-settings-field>
              </div>
            </app-card-content>
            <app-card-footer class="justify-end">
              <app-settings-form-actions
                [saving]="saving()"
                [disabled]="profileForm.invalid"
                (cancelled)="resetForm()" />
            </app-card-footer>
          </app-card>
        </form>
      }
    </app-settings-page-layout>
  `
})
export class ProfileSettingsComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);
  private readonly fb = inject(FormBuilder);
  private readonly toastService = inject(ToastService);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly profile = signal<ProfileSettings | null>(null);

  profileForm: FormGroup = this.fb.group({
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]]
  });

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading.set(true);
    this.error.set('');

    this.settingsService.getProfile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.profileForm.patchValue({
          firstName: profile.firstName,
          lastName: profile.lastName
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to load profile');
        this.loading.set(false);
      }
    });
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      return;
    }

    this.saving.set(true);
    this.error.set('');

    const update: ProfileSettingsUpdate = {
      firstName: this.profileForm.value.firstName,
      lastName: this.profileForm.value.lastName
    };

    this.settingsService.updateProfile(update).subscribe({
      next: (updated) => {
        this.profile.set(updated);
        this.saving.set(false);
        this.toastService.successI18n('settings.toast.profileSaved', 'settings.toast.profileSavedDescription');
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to update profile');
        this.saving.set(false);
      }
    });
  }

  resetForm(): void {
    const profile = this.profile();
    if (profile) {
      this.profileForm.patchValue({
        firstName: profile.firstName,
        lastName: profile.lastName
      });
    }
  }
}
