import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PrivacyService } from '../../../core/services/privacy.service';
import { AuthService } from '../../../core/services/auth.service';
import { TranslationService } from '../../../core/services/translation.service';
import { ErasureRequest } from '../../../core/models/privacy.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { ROLES } from '../../../core/utils/constants';
import {
  CardComponent,
  CardHeaderComponent,
  CardTitleComponent,
  CardDescriptionComponent,
  CardContentComponent,
} from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import { ConfirmDialogService } from '../../../shared/components/ui/alert-dialog';
import { ToastService } from '../../../shared/components/ui/toast/toast.service';
import { SettingsPageLayoutComponent } from '../components';

@Component({
  selector: 'app-privacy-settings',
  imports: [
    CommonModule,
    FormsModule,
    DatePipe,
    TranslatePipe,
    CardComponent,
    CardHeaderComponent,
    CardTitleComponent,
    CardDescriptionComponent,
    CardContentComponent,
    AlertComponent,
    ButtonComponent,
    BadgeComponent,
    SettingsPageLayoutComponent,
  ],
  templateUrl: './privacy-settings.component.html',
})
export class PrivacySettingsComponent implements OnInit {
  private readonly privacyService = inject(PrivacyService);
  private readonly authService = inject(AuthService);
  private readonly translation = inject(TranslationService);
  private readonly confirmDialog = inject(ConfirmDialogService);
  private readonly toastService = inject(ToastService);

  readonly loading = signal(false);
  readonly exporting = signal(false);
  readonly requesting = signal(false);
  readonly adminLoading = signal(false);
  readonly error = signal('');
  readonly erasureRequest = signal<ErasureRequest | null>(null);
  readonly pendingRequests = signal<ErasureRequest[]>([]);

  readonly isOrgAdmin = computed(() => this.authService.hasRole(ROLES.ORG_ADMIN));

  ngOnInit(): void {
    this.loadErasureStatus();
    if (this.isOrgAdmin()) {
      this.loadPendingRequests();
    }
  }

  loadErasureStatus(): void {
    this.loading.set(true);
    this.privacyService.getErasureRequest().subscribe({
      next: (request) => {
        this.erasureRequest.set(request);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  loadPendingRequests(): void {
    this.adminLoading.set(true);
    this.privacyService.listPendingErasureRequests().subscribe({
      next: (requests) => {
        this.pendingRequests.set(requests);
        this.adminLoading.set(false);
      },
      error: () => {
        this.adminLoading.set(false);
      },
    });
  }

  exportData(): void {
    this.exporting.set(true);
    this.error.set('');
    this.privacyService.exportPersonalData().subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `erm-data-export-${Date.now()}.zip`;
        anchor.click();
        URL.revokeObjectURL(url);
        this.exporting.set(false);
        this.toastService.successI18n('settings.privacy.exportSuccess');
      },
      error: () => {
        this.error.set('settings.privacy.exportError');
        this.exporting.set(false);
      },
    });
  }

  async requestDeletion(): Promise<void> {
    const confirmed = await this.confirmDialog.open({
      title: this.translation.translate('settings.privacy.confirmDeletionTitle'),
      description: this.translation.translate('settings.privacy.confirmDeletionDescription'),
      confirmLabel: this.translation.translate('settings.privacy.requestDeletion'),
      cancelLabel: this.translation.translate('common.cancel'),
      variant: 'destructive',
    });
    if (!confirmed) {
      return;
    }

    this.requesting.set(true);
    this.error.set('');
    this.privacyService.requestErasure().subscribe({
      next: (request) => {
        this.erasureRequest.set(request);
        this.requesting.set(false);
        this.toastService.successI18n('settings.privacy.requestSuccess');
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'settings.privacy.requestError');
        this.requesting.set(false);
      },
    });
  }

  async approveRequest(request: ErasureRequest): Promise<void> {
    const confirmed = await this.confirmDialog.open({
      title: this.translation.translate('settings.privacy.confirmApproveTitle'),
      description: this.translation.translate('settings.privacy.confirmApproveDescription'),
      confirmLabel: this.translation.translate('settings.privacy.approve'),
      cancelLabel: this.translation.translate('common.cancel'),
      variant: 'destructive',
    });
    if (!confirmed) {
      return;
    }

    this.privacyService.approveErasureRequest(request.id).subscribe({
      next: () => {
        this.loadPendingRequests();
        this.toastService.successI18n('settings.privacy.approveSuccess');
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'settings.privacy.adminError');
      },
    });
  }

  rejectRequest(request: ErasureRequest): void {
    this.privacyService.rejectErasureRequest(request.id).subscribe({
      next: () => {
        this.loadPendingRequests();
        this.toastService.successI18n('settings.privacy.rejectSuccess');
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'settings.privacy.adminError');
      },
    });
  }
}
