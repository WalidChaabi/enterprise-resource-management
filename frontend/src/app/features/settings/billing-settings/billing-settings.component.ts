import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { SettingsService } from '../../../core/services/settings.service';
import { BillingSettings } from '../../../core/models/settings.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';

@Component({
  selector: 'app-billing-settings',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    TranslatePipe,
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
          <div class="rounded-lg border border-primary/20 bg-gradient-to-br from-primary/5 via-primary/3 to-background shadow-md">
            <div class="p-6">
              <div class="flex items-start justify-between">
                <div class="flex items-start gap-4 flex-1">
                  <div class="flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10 text-primary">
                    <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                    </svg>
                  </div>
                  <div class="flex-1">
                    <div class="flex items-center gap-3 mb-2">
                      <h3 class="text-2xl font-bold text-foreground">{{ billing()?.plan || 'No Plan' }}</h3>
                      <app-badge [variant]="getStatusVariant()" class="text-xs font-medium">
                        {{ billing()?.status || 'Unknown' }}
                      </app-badge>
                    </div>
                    @if (billing()?.nextBillingDate) {
                      <div class="flex items-center gap-2 text-sm text-muted-foreground">
                        <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
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
                  <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-500/10 text-blue-500">
                    <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                    </svg>
                  </div>
                  <app-card-title>Payment Information</app-card-title>
                </div>
              </app-card-header>
              <app-card-content>
                <div class="flex flex-col items-center justify-center py-8 text-center">
                  <div class="rounded-full bg-muted p-4 mb-4">
                    <svg class="h-6 w-6 text-muted-foreground" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
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
                  <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-green-500/10 text-green-500">
                    <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                  </div>
                  <app-card-title>Billing History</app-card-title>
                </div>
              </app-card-header>
              <app-card-content>
                <div class="flex flex-col items-center justify-center py-8 text-center">
                  <div class="rounded-full bg-muted p-4 mb-4">
                    <svg class="h-6 w-6 text-muted-foreground" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
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
