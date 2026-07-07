import { LucideAngularModule } from 'lucide-angular';
import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { SettingsService } from '../../../core/services/settings.service';
import { BillingSettings } from '../../../core/models/settings.model';
import { TranslatePipe } from '../../../core/pipes/translate.pipe';
import { CardComponent, CardHeaderComponent, CardTitleComponent, CardContentComponent } from '../../../shared/components/ui/card/card.component';
import { AlertComponent } from '../../../shared/components/ui/alert/alert.component';
import { BadgeComponent } from '../../../shared/components/ui/badge/badge.component';
import {
  SettingsPageLayoutComponent,
} from '../components';

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
        BadgeComponent,
        SettingsPageLayoutComponent,
    ],
    template: `
    <app-settings-page-layout>
      @if (checkoutMessage()) {
        <app-alert [variant]="checkoutMessageVariant()">{{ checkoutMessage() }}</app-alert>
      }

      @if (error()) {
        <app-alert variant="destructive">{{ error() }}</app-alert>
      }

      @if (loading()) {
        <div class="flex items-center justify-center py-12">
          <div class="h-8 w-8 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      } @else {
        <div class="space-y-6">
          <div class="rounded-xl border border-subtle bg-card shadow-card">
            <div class="p-6">
              <div class="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
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
                        @if (billing()?.seatCount) {
                          <span> · {{ billing()!.seatCount }} {{ 'settings.billing.seats' | translate }}</span>
                        }
                      </p>
                    }
                    @if (billing()?.trialEndsAt && billing()?.status?.toLowerCase() === 'trialing') {
                      <div class="flex items-center gap-2 text-sm text-muted-foreground mb-1">
                        <lucide-icon name="clock" [size]="16" />
                        <span>{{ 'settings.billing.trialEnds' | translate }} <strong class="text-foreground">{{ billing()!.trialEndsAt | date:'mediumDate' }}</strong></span>
                      </div>
                    }
                    @if (billing()?.nextBillingDate) {
                      <div class="flex items-center gap-2 text-sm text-muted-foreground">
                        <lucide-icon name="calendar" [size]="16" />
                        <span>{{ 'settings.billing.nextBilling' | translate }} <strong class="text-foreground">{{ billing()!.nextBillingDate | date:'mediumDate' }}</strong></span>
                      </div>
                    } @else if (!billing()?.trialEndsAt) {
                      <p class="text-sm text-muted-foreground">{{ 'settings.billing.noBillingDate' | translate }}</p>
                    }
                  </div>
                </div>
                <div class="flex flex-wrap gap-2">
                  @if (billing()?.checkoutAvailable) {
                    <button
                      type="button"
                      [disabled]="actionLoading()"
                      (click)="startCheckout()"
                      class="inline-flex h-9 items-center justify-center rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground hover:bg-primary/90 transition-colors disabled:opacity-50">
                      {{ 'settings.billing.upgrade' | translate }}
                    </button>
                  }
                  @if (billing()?.portalAvailable) {
                    <button
                      type="button"
                      [disabled]="actionLoading()"
                      (click)="openPortal()"
                      class="inline-flex h-9 items-center justify-center rounded-md border border-input bg-background px-4 text-sm font-medium hover:bg-accent hover:text-accent-foreground transition-colors disabled:opacity-50">
                      {{ 'settings.billing.managePlan' | translate }}
                    </button>
                  }
                </div>
              </div>
            </div>
          </div>

          <div class="grid gap-6 lg:grid-cols-2">
            <app-card class="group hover:shadow-md transition-shadow duration-200">
              <app-card-header>
                <div class="flex items-center gap-3">
                  <div class="icon-container h-10 w-10">
                    <lucide-icon name="credit-card" [size]="20" />
                  </div>
                  <app-card-title>{{ 'settings.billing.paymentInfo' | translate }}</app-card-title>
                </div>
              </app-card-header>
              <app-card-content>
                <div class="flex flex-col items-center justify-center py-8 text-center">
                  <div class="rounded-full bg-muted p-4 mb-4">
                    <lucide-icon name="dollar-sign" [size]="24" class="text-muted-foreground" />
                  </div>
                  @if (billing()?.portalAvailable) {
                    <h4 class="text-sm font-medium text-foreground mb-1">{{ 'settings.billing.paymentViaPortal' | translate }}</h4>
                    <p class="text-xs text-muted-foreground">{{ 'settings.billing.paymentViaPortalHint' | translate }}</p>
                  } @else {
                    <h4 class="text-sm font-medium text-foreground mb-1">{{ 'settings.billing.paymentComingSoon' | translate }}</h4>
                    <p class="text-xs text-muted-foreground">{{ 'settings.billing.paymentComingSoonHint' | translate }}</p>
                  }
                </div>
              </app-card-content>
            </app-card>

            <app-card class="group hover:shadow-md transition-shadow duration-200">
              <app-card-header>
                <div class="flex items-center gap-3">
                  <div class="icon-container h-10 w-10">
                    <lucide-icon name="file-text" [size]="20" />
                  </div>
                  <app-card-title>{{ 'settings.billing.history' | translate }}</app-card-title>
                </div>
              </app-card-header>
              <app-card-content>
                <div class="flex flex-col items-center justify-center py-8 text-center">
                  <div class="rounded-full bg-muted p-4 mb-4">
                    <lucide-icon name="file-text" [size]="24" class="text-muted-foreground" />
                  </div>
                  @if (billing()?.portalAvailable) {
                    <h4 class="text-sm font-medium text-foreground mb-1">{{ 'settings.billing.historyViaPortal' | translate }}</h4>
                    <p class="text-xs text-muted-foreground">{{ 'settings.billing.historyViaPortalHint' | translate }}</p>
                  } @else {
                    <h4 class="text-sm font-medium text-foreground mb-1">{{ 'settings.billing.historyComingSoon' | translate }}</h4>
                    <p class="text-xs text-muted-foreground">{{ 'settings.billing.historyComingSoonHint' | translate }}</p>
                  }
                </div>
              </app-card-content>
            </app-card>
          </div>
        </div>
      }
    </app-settings-page-layout>
  `
})
export class BillingSettingsComponent implements OnInit {
  private readonly settingsService = inject(SettingsService);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(false);
  readonly actionLoading = signal(false);
  readonly error = signal('');
  readonly billing = signal<BillingSettings | null>(null);
  readonly checkoutMessage = signal('');
  readonly checkoutMessageVariant = signal<'default' | 'destructive'>('default');

  ngOnInit(): void {
    const checkout = this.route.snapshot.queryParamMap.get('checkout');
    if (checkout === 'success') {
      this.checkoutMessage.set('Subscription updated successfully. It may take a moment to reflect here.');
      this.checkoutMessageVariant.set('default');
    } else if (checkout === 'cancel') {
      this.checkoutMessage.set('Checkout was cancelled. No changes were made.');
      this.checkoutMessageVariant.set('default');
    }
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

  openPortal(): void {
    this.actionLoading.set(true);
    this.error.set('');
    this.settingsService.createBillingPortalSession().subscribe({
      next: (response) => {
        this.actionLoading.set(false);
        window.location.href = response.url;
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Unable to open billing portal');
        this.actionLoading.set(false);
      }
    });
  }

  startCheckout(): void {
    this.actionLoading.set(true);
    this.error.set('');
    this.settingsService.createBillingCheckoutSession().subscribe({
      next: (response) => {
        this.actionLoading.set(false);
        window.location.href = response.url;
      },
      error: (err) => {
        this.error.set(err.details?.[0] || 'Unable to start checkout');
        this.actionLoading.set(false);
      }
    });
  }

  getStatusVariant(): 'default' | 'secondary' | 'destructive' | 'outline' {
    const status = this.billing()?.status?.toLowerCase();
    if (status === 'active') return 'default';
    if (status === 'trialing') return 'secondary';
    if (status === 'cancelled' || status === 'canceled' || status === 'expired' || status === 'unpaid') {
      return 'destructive';
    }
    return 'secondary';
  }
}
