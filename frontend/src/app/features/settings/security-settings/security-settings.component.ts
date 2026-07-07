import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SecurityService } from '../../../core/services/security.service';
import { AuthService } from '../../../core/services/auth.service';
import { TranslationService } from '../../../core/services/translation.service';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ROLES } from '../../../core/utils/constants';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import { InputComponent } from '../../../shared/components/ui/input/input.component';
import { FormSelectComponent } from '../../../shared/components/ui/form-select/form-select.component';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';
import { SwitchComponent } from '../../../shared/components/ui/switch/switch.component';
import {
  SettingsPageLayoutComponent,
  SettingsCardComponent,
  SettingsItemComponent,
  SettingsFieldComponent,
  SettingsInfoCalloutComponent,
  SettingsActionBarComponent,
} from '../components';

interface OrgSettingsSnapshot {
  ssoEnabled: boolean;
  requireMfa: boolean;
  ssoProvider: string;
}

@Component({
  selector: 'app-security-settings',
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TranslatePipe,
    ButtonComponent,
    AlertComponent,
    BadgeComponent,
    InputComponent,
    FormSelectComponent,
    SwitchComponent,
    SettingsPageLayoutComponent,
    SettingsCardComponent,
    SettingsItemComponent,
    SettingsFieldComponent,
    SettingsInfoCalloutComponent,
    SettingsActionBarComponent,
  ],
  templateUrl: './security-settings.component.html',
})
export class SecuritySettingsComponent implements OnInit {
  private readonly securityService = inject(SecurityService);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  private readonly translation = inject(TranslationService);
  private readonly fb = inject(FormBuilder);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly setupSecret = signal('');
  readonly qrUri = signal('');
  readonly backupCodes = signal<string[]>([]);
  readonly verificationCode = signal('');

  readonly userMfaEnabled = signal(false);
  readonly unusedBackupCodes = signal(0);
  readonly availableProviders = signal<string[]>([]);
  readonly savedOrgSettings = signal<OrgSettingsSnapshot | null>(null);
  readonly orgFormRevision = signal(0);

  readonly isOrgAdmin = computed(() => this.authService.hasRole(ROLES.ORG_ADMIN));
  readonly hasConfiguredSsoProviders = computed(() => this.availableProviders().length > 0);

  readonly orgSettingsDirty = computed(() => {
    this.orgFormRevision();
    const saved = this.savedOrgSettings();
    if (!saved) return false;
    const current = this.orgSettingsForm.getRawValue() as OrgSettingsSnapshot;
    return (
      current.ssoEnabled !== saved.ssoEnabled
      || current.requireMfa !== saved.requireMfa
      || current.ssoProvider !== saved.ssoProvider
    );
  });

  readonly ssoProviderOptions = computed(() => {
    const providers = this.availableProviders();
    const options: { value: string; label: string }[] = [];

    if (providers.length === 0 || providers.length > 1) {
      options.push({
        value: 'ALL',
        label: this.translation.translate('settings.security.providerAll'),
      });
    }
    if (providers.length === 0 || providers.includes('google')) {
      options.push({ value: 'google', label: 'Google' });
    }
    if (providers.length === 0 || providers.includes('azure')) {
      options.push({ value: 'azure', label: 'Microsoft' });
    }

    return options;
  });

  orgSettingsForm: FormGroup = this.fb.group({
    ssoEnabled: [false],
    requireMfa: [false],
    ssoProvider: ['ALL'],
  });

  ngOnInit(): void {
    this.orgSettingsForm.get('ssoEnabled')?.valueChanges.subscribe((enabled: boolean) => {
      const providerControl = this.orgSettingsForm.get('ssoProvider');
      if (enabled) {
        providerControl?.enable({ emitEvent: false });
      } else {
        providerControl?.disable({ emitEvent: false });
      }
    });

    this.orgSettingsForm.valueChanges.subscribe(() => {
      this.orgFormRevision.update((value) => value + 1);
    });

    this.load();
  }

  load(): void {
    this.loading.set(true);

    this.securityService.getSecuritySettings().subscribe({
      next: (settings) => {
        this.applyOrgSettings({
          ssoEnabled: settings.ssoEnabled,
          requireMfa: settings.requireMfa,
          ssoProvider: settings.ssoProvider || 'ALL',
        });

        this.userMfaEnabled.set(settings.userMfaEnabled);
        this.unusedBackupCodes.set(settings.unusedBackupCodes);
        this.availableProviders.set(settings.availableSsoProviders || []);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('settings.security.loadError');
        this.loading.set(false);
      },
    });
  }

  saveOrgSettings(): void {
    if (!this.isOrgAdmin() || !this.orgSettingsDirty()) return;

    this.saving.set(true);
    this.error.set('');

    const { ssoEnabled, requireMfa, ssoProvider } = this.orgSettingsForm.getRawValue();

    this.securityService.updateSecuritySettings({
      ssoEnabled,
      requireMfa,
      ssoProvider,
    }).subscribe({
      next: () => {
        this.toastService.successI18n('settings.security.saved');
        this.saving.set(false);
        this.load();
      },
      error: () => {
        this.error.set('settings.security.saveError');
        this.saving.set(false);
      },
    });
  }

  resetOrgSettings(): void {
    const saved = this.savedOrgSettings();
    if (saved) {
      this.applyOrgSettings(saved);
    }
  }

  beginMfaSetup(): void {
    this.error.set('');
    this.backupCodes.set([]);

    this.securityService.beginMfaSetup().subscribe({
      next: (setup) => {
        this.setupSecret.set(setup.secret);
        this.qrUri.set(setup.qrCodeUri);
      },
      error: (err) => this.error.set(err?.details?.[0] || 'settings.security.mfaSetupError'),
    });
  }

  confirmMfaSetup(): void {
    if (!this.setupSecret() || !this.verificationCode()) return;

    this.securityService.confirmMfaSetup({
      secret: this.setupSecret(),
      code: this.verificationCode(),
    }).subscribe({
      next: (response) => {
        this.backupCodes.set(response.codes);
        this.setupSecret.set('');
        this.qrUri.set('');
        this.verificationCode.set('');
        this.toastService.successI18n('settings.security.mfaEnabled');
        this.load();
      },
      error: () => this.error.set('settings.security.mfaConfirmError'),
    });
  }

  disableMfa(): void {
    this.securityService.disableMfa().subscribe({
      next: () => {
        this.toastService.successI18n('settings.security.mfaDisabled');
        this.load();
      },
      error: () => this.error.set('settings.security.mfaDisableError'),
    });
  }

  regenerateCodes(): void {
    this.securityService.regenerateBackupCodes().subscribe({
      next: (response) => {
        this.backupCodes.set(response.codes);
        this.toastService.successI18n('settings.security.backupCodesGenerated');
        this.load();
      },
      error: () => this.error.set('settings.security.backupCodesError'),
    });
  }

  private applyOrgSettings(settings: OrgSettingsSnapshot): void {
    this.orgSettingsForm.reset(settings, { emitEvent: false });

    if (!settings.ssoEnabled) {
      this.orgSettingsForm.get('ssoProvider')?.disable({ emitEvent: false });
    } else {
      this.orgSettingsForm.get('ssoProvider')?.enable({ emitEvent: false });
    }

    this.savedOrgSettings.set({ ...settings });
    this.orgFormRevision.update((value) => value + 1);
  }
}
