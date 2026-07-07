import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SettingsService } from '../../../core/services/settings.service';
import { OrganizationSettings, OrganizationSettingsUpdate } from '../../../core/models/settings.model';
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
    selector: 'app-organization-settings',
    imports: [
        CommonModule,
        ReactiveFormsModule,
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
        <form [formGroup]="orgForm" (ngSubmit)="onSubmit()">
          <app-card>
            <app-card-header>
              <app-card-title>{{ 'settings.organization.infoTitle' | translate }}</app-card-title>
            </app-card-header>
            <app-card-content class="space-y-4">
              <app-settings-field
                labelKey="settings.organization.name"
                forId="name">
                <app-input id="name" formControlName="name" />
                @if (orgForm.get('name')?.hasError('required') && orgForm.get('name')?.touched) {
                  <p class="text-xs text-destructive">{{ 'settings.organization.nameRequired' | translate }}</p>
                }
              </app-settings-field>

              <app-settings-field
                labelKey="settings.organization.description"
                forId="description">
                <textarea
                  id="description"
                  formControlName="description"
                  rows="4"
                  class="flex min-h-[80px] w-full rounded-[10px] border border-subtle bg-transparent px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:cursor-not-allowed disabled:opacity-50">
                </textarea>
              </app-settings-field>
            </app-card-content>
            <app-card-footer class="justify-end">
              <app-settings-form-actions
                [saving]="saving()"
                [disabled]="orgForm.invalid"
                (cancelled)="resetForm()" />
            </app-card-footer>
          </app-card>
        </form>
      }
    </app-settings-page-layout>
  `
})
export class OrganizationSettingsComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);
  private readonly fb = inject(FormBuilder);
  private readonly toastService = inject(ToastService);

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
        this.toastService.successI18n('settings.toast.organizationSaved', 'settings.toast.organizationSavedDescription');
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
