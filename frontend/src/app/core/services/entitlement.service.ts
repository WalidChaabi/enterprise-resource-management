import { Injectable, inject, signal } from '@angular/core';
import { Entitlements } from '../models/settings.model';
import { SettingsService } from './settings.service';

@Injectable({
  providedIn: 'root'
})
export class EntitlementService {
  private readonly settingsService = inject(SettingsService);

  readonly entitlements = signal<Entitlements | null>(null);

  load(): void {
    this.settingsService.getEntitlements().subscribe({
      next: (data) => this.entitlements.set(data),
      error: () => this.entitlements.set(null),
    });
  }

  clear(): void {
    this.entitlements.set(null);
  }

  has(entitlement: string): boolean {
    return this.entitlements()?.entitlements.includes(entitlement) ?? false;
  }
}
