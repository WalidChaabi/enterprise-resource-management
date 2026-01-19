import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SettingsService } from '../../../core/services/settings.service';
import { OrganizationSettings, OrganizationSettingsUpdate } from '../../../core/models/settings.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';

@Component({
  selector: 'app-organization-settings',
  standalone: true,
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
        <h2 class="text-xl font-semibold text-foreground">Organization Settings</h2>
        <p class="text-sm text-muted-foreground">Manage your organization details and workspace settings.</p>
      </div>

      @if (error()) {
        <app-alert variant="destructive">{{ error() }}</app-alert>
      }

      @if (loading()) {
        <div class="flex items-center justify-center py-12">
          <div class="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      } @else {
        <form [formGroup]="orgForm" (ngSubmit)="onSubmit()">
          <app-card>
            <app-card-header>
              <app-card-title>Organization Information</app-card-title>
            </app-card-header>
            <app-card-content class="space-y-4">
              <div>
                <label for="name" class="text-sm font-medium text-foreground">Organization Name</label>
                <input
                  id="name"
                  type="text"
                  formControlName="name"
                  class="mt-1 flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm">
                @if (orgForm.get('name')?.hasError('required') && orgForm.get('name')?.touched) {
                  <p class="mt-1 text-xs text-destructive">Organization name is required.</p>
                }
              </div>

              <div>
                <label for="description" class="text-sm font-medium text-foreground">Description</label>
                <textarea
                  id="description"
                  formControlName="description"
                  rows="4"
                  class="mt-1 flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm">
                </textarea>
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
                  [disabled]="orgForm.invalid || saving()"
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
export class OrganizationSettingsComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly organization = signal<OrganizationSettings | null>(null);

  orgForm: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    description: ['']
  });

  ngOnInit(): void {
    this.loadOrganization();
  }

  loadOrganization(): void {
    this.loading.set(true);
    this.error.set('');

    this.settingsService.getOrganization().subscribe({
      next: (org) => {
        this.organization.set(org);
        this.orgForm.patchValue({
          name: org.name,
          description: org.description || ''
        });
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to load organization');
        this.loading.set(false);
      }
    });
  }

  onSubmit(): void {
    if (this.orgForm.invalid) {
      return;
    }

    this.saving.set(true);
    this.error.set('');

    const update: OrganizationSettingsUpdate = {
      name: this.orgForm.value.name,
      description: this.orgForm.value.description || undefined
    };

    this.settingsService.updateOrganization(update).subscribe({
      next: (updated) => {
        this.organization.set(updated);
        this.saving.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to update organization');
        this.saving.set(false);
      }
    });
  }

  resetForm(): void {
    const org = this.organization();
    if (org) {
      this.orgForm.patchValue({
        name: org.name,
        description: org.description || ''
      });
    }
  }
}
