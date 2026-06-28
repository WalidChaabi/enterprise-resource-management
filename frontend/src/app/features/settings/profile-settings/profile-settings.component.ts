import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SettingsService } from '../../../core/services/settings.service';
import { ProfileSettings, ProfileSettingsUpdate } from '../../../core/models/settings.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
    selector: 'app-profile-settings',
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
        <h2 class="text-xl font-semibold text-foreground">Profile Settings</h2>
        <p class="text-sm text-muted-foreground">Update your personal information and preferences.</p>
      </div>

      @if (error()) {
        <app-alert variant="destructive">{{ error() }}</app-alert>
      }

      @if (loading()) {
        <div class="flex items-center justify-center py-12">
          <div class="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      } @else {
        <form [formGroup]="profileForm" (ngSubmit)="onSubmit()">
          <app-card>
            <app-card-header>
              <app-card-title>Personal Information</app-card-title>
            </app-card-header>
            <app-card-content class="space-y-4">
              <div>
                <label class="text-sm font-medium text-foreground">Email</label>
                <input
                  type="email"
                  [value]="profile()?.email"
                  disabled
                  class="mt-1 flex h-10 w-full rounded-md border border-input bg-muted px-3 py-2 text-sm text-muted-foreground">
                <p class="mt-1 text-xs text-muted-foreground">Email cannot be changed. Contact support if needed.</p>
              </div>

              <div class="grid gap-4 sm:grid-cols-2">
                <div>
                  <label for="firstName" class="text-sm font-medium text-foreground">First Name</label>
                  <input
                    id="firstName"
                    type="text"
                    formControlName="firstName"
                    class="mt-1 flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm">
                  @if (profileForm.get('firstName')?.hasError('required') && profileForm.get('firstName')?.touched) {
                    <p class="mt-1 text-xs text-destructive">First name is required.</p>
                  }
                </div>

                <div>
                  <label for="lastName" class="text-sm font-medium text-foreground">Last Name</label>
                  <input
                    id="lastName"
                    type="text"
                    formControlName="lastName"
                    class="mt-1 flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm">
                  @if (profileForm.get('lastName')?.hasError('required') && profileForm.get('lastName')?.touched) {
                    <p class="mt-1 text-xs text-destructive">Last name is required.</p>
                  }
                </div>
              </div>

              <div class="flex justify-end gap-2 pt-4">
                <button
                  type="button"
                  class="inline-flex h-10 items-center justify-center rounded-md border border-input bg-background px-4 py-2 text-sm font-medium hover:bg-accent"
                  (click)="resetForm()">
                  Cancel
                </button>
                <button
                  type="submit"
                  [disabled]="profileForm.invalid || saving()"
                  class="inline-flex h-10 items-center justify-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:pointer-events-none disabled:opacity-50">
                  @if (saving()) {
                    <span class="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent"></span>
                  }
                  Save Changes
                </button>
              </div>
            </app-card-content>
          </app-card>
        </form>
      }
    </div>
  `
})
export class ProfileSettingsComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);
  private readonly fb = inject(FormBuilder);

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
