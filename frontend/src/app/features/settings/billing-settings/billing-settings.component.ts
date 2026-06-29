import { LucideAngularModule } from 'lucide-angular';
import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { SettingsService } from '../../../core/services/settings.service';
import { BillingSettings } from '../../../core/models/settings.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';

@Component({
    selector: 'app-billing-settings',
    imports: [
        CommonModule,
        DatePipe,
        DecimalPipe,
        TranslatePipe,
        LucideAngularModule,
        CardComponent,
        CardHeaderComponent,
        CardTitleComponent,
        CardContentComponent,
        AlertComponent,
        BadgeComponent
    ],
    template: `
    <div class="mt-6 space-y-6">
      <div>
        <h2 class="text-xl font-semibold text-foreground">Billing</h2>
        <p class="text-sm text-muted-foreground">Manage your subscription plan, invoices, and payment methods.</p>
      </div>

      @if (error()) {
        <app-alert variant="destructive">{{ error() }}</app-alert>
      }

      @if (loading()) {
        <div class="flex items-center justify-center py-12">
          <div class="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      } @else {
        <div class="space-y-6">
          <!-- Current Plan Card - Prominent -->
          <div class="rounded-xl border border-subtle bg-card shadow-card">
            <div class="p-6">
              <div class="flex items-start justify-between">
                <div class="flex items-start gap-4 flex-1">
                  <div class="icon-container h-12 w-12">
                    <lucide-icon name="shield-check" [size]="24" />
                  </div>
                  <div class="flex-1">
                    <div class="flex items-center gap-3 mb-2">
                      <h3 class="text-2xl font-bold text-foreground">{{ billing()?.planName || ('settings.billing.noPlan' | translate) }}</h3>
                      <app-badge [variant]="getStatusVariant()" class="text-xs font-medium">
                        {{ billing()?.status || ('settings.billing.unknown' | translate) }}
                      </app-badge>
                    </div>
                    @if (billing()?.amount != null && billing()?.currency) {
                      <p class="text-sm text-muted-foreground mb-1">
                        {{ billing()!.amount | number:'1.2-2' }} {{ billing()!.currency }}
                      </p>
                    }
                    @if (billing()?.nextBillingDate) {
                      <div class="flex items-center gap-2 text-sm text-muted-foreground">
                        <lucide-icon name="calendar" [size]="16" />
                        <span>Next billing date: <strong class="text-foreground">{{ billing()!.nextBillingDate | date:'mediumDate' }}</strong></span>
                      </div>
                    } @else {
                      <p class="text-sm text-muted-foreground">No billing date scheduled</p>
                    }
                  </div>
                </div>
                <button
                  type="button"
                  class="inline-flex h-9 items-center justify-center rounded-md border border-input bg-background px-4 text-sm font-medium hover:bg-accent hover:text-accent-foreground transition-colors">
                  Manage Plan
                </button>
              </div>
            </div>
          </div>

          <!-- Payment Information and Billing History Grid -->
          <div class="grid gap-6 lg:grid-cols-2">
            <!-- Payment Information Card -->
            <app-card class="group hover:shadow-md transition-shadow duration-200">
              <app-card-header>
                <div class="flex items-center gap-3">
                  <div class="icon-container h-10 w-10">
                    <lucide-icon name="credit-card" [size]="20" />
                  </div>
                  <app-card-title>Payment Information</app-card-title>
                </div>
              </app-card-header>
              <app-card-content>
                <div class="flex flex-col items-center justify-center py-8 text-center">
                  <div class="rounded-full bg-muted p-4 mb-4">
                    <lucide-icon name="dollar-sign" [size]="24" class="text-muted-foreground" />
                  </div>
                  <h4 class="text-sm font-medium text-foreground mb-1">Payment management coming soon</h4>
                  <p class="text-xs text-muted-foreground">Add and manage your payment methods here</p>
                </div>
              </app-card-content>
            </app-card>

            <!-- Billing History Card -->
            <app-card class="group hover:shadow-md transition-shadow duration-200">
              <app-card-header>
                <div class="flex items-center gap-3">
                  <div class="icon-container h-10 w-10">
                    <lucide-icon name="file-text" [size]="20" />
                  </div>
                  <app-card-title>Billing History</app-card-title>
                </div>
              </app-card-header>
              <app-card-content>
                <div class="flex flex-col items-center justify-center py-8 text-center">
                  <div class="rounded-full bg-muted p-4 mb-4">
                    <lucide-icon name="file-text" [size]="24" class="text-muted-foreground" />
                  </div>
                  <h4 class="text-sm font-medium text-foreground mb-1">Invoice history coming soon</h4>
                  <p class="text-xs text-muted-foreground">View and download your past invoices</p>
                </div>
              </app-card-content>
            </app-card>
          </div>
        </div>
      }
    </div>
  `
})
export class BillingSettingsComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);

  readonly loading = signal(false);
  readonly error = signal('');
  readonly billing = signal<BillingSettings | null>(null);

  ngOnInit(): void {
    this.loadBilling();
  }

  loadBilling(): void {
    this.loading.set(true);
    this.error.set('');

    this.settingsService.getBilling().subscribe({
      next: (billing) => {
        this.billing.set(billing);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Failed to load billing information');
        this.loading.set(false);
      }
    });
  }

  getStatusVariant(): 'default' | 'secondary' | 'destructive' | 'outline' {
    const status = this.billing()?.status?.toLowerCase();
    if (status === 'active') return 'default';
    if (status === 'cancelled' || status === 'expired') return 'destructive';
    return 'secondary';
  }
}
