import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PrivacyService } from '../../../core/services/privacy.service';
import { AuthService } from '../../../core/services/auth.service';
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
  ],
  templateUrl: './privacy-settings.component.html',
})
export class PrivacySettingsComponent implements OnInit {
  private readonly privacyService = inject(PrivacyService);
  private readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly exporting = signal(false);
  readonly requesting = signal(false);
  readonly adminLoading = signal(false);
  readonly error = signal('');
  readonly success = signal('');
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
    this.success.set('');
    this.privacyService.exportPersonalData().subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = `erm-data-export-${Date.now()}.zip`;
        anchor.click();
        URL.revokeObjectURL(url);
        this.exporting.set(false);
        this.success.set('settings.privacy.exportSuccess');
      },
      error: () => {
        this.error.set('settings.privacy.exportError');
        this.exporting.set(false);
      },
    });
  }

  requestDeletion(): void {
    if (!confirm('Request account deletion? An administrator must approve before your data is anonymized.')) {
      return;
    }
    this.requesting.set(true);
    this.error.set('');
    this.success.set('');
    this.privacyService.requestErasure().subscribe({
      next: (request) => {
        this.erasureRequest.set(request);
        this.requesting.set(false);
        this.success.set('settings.privacy.requestSuccess');
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'settings.privacy.requestError');
        this.requesting.set(false);
      },
    });
  }

  approveRequest(request: ErasureRequest): void {
    this.privacyService.approveErasureRequest(request.id).subscribe({
      next: () => {
        this.loadPendingRequests();
        this.success.set('settings.privacy.approveSuccess');
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
        this.success.set('settings.privacy.rejectSuccess');
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'settings.privacy.adminError');
      },
    });
  }
}
