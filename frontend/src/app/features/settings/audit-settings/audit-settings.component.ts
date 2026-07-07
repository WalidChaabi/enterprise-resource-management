import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuditService } from '../../../core/services/audit.service';
import { AuditEvent } from '../../../core/models/audit.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardDescriptionComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { ButtonComponent } from '../../../shared/components/ui/button/button.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import { SettingsPageLayoutComponent } from '../components';

@Component({
  selector: 'app-audit-settings',
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
  templateUrl: './audit-settings.component.html',
})
export class AuditSettingsComponent implements OnInit {
  private readonly auditService = inject(AuditService);

  readonly loading = signal(false);
  readonly exporting = signal(false);
  readonly error = signal('');
  readonly events = signal<AuditEvent[]>([]);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);

  actionFilter = '';
  entityTypeFilter = '';

  ngOnInit(): void {
    this.load();
  }

  load(page = 0): void {
    this.loading.set(true);
    this.error.set('');
    this.page.set(page);

    this.auditService
      .listEvents(page, 20, this.actionFilter || undefined, this.entityTypeFilter || undefined)
      .subscribe({
        next: (result) => {
          this.events.set(result.content);
          this.totalPages.set(result.totalPages);
          this.totalElements.set(result.totalElements);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(err.details?.[0] || 'Failed to load audit log');
          this.loading.set(false);
        },
      });
  }

  applyFilters(): void {
    this.load(0);
  }

  exportCsv(): void {
    this.exporting.set(true);
    this.auditService.exportCsv(this.actionFilter || undefined, this.entityTypeFilter || undefined).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = 'audit-events.csv';
        anchor.click();
        URL.revokeObjectURL(url);
        this.exporting.set(false);
      },
      error: () => {
        this.error.set('Failed to export audit log');
        this.exporting.set(false);
      },
    });
  }

  previousPage(): void {
    if (this.page() > 0) {
      this.load(this.page() - 1);
    }
  }

  nextPage(): void {
    if (this.page() < this.totalPages() - 1) {
      this.load(this.page() + 1);
    }
  }
}
