import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '../../core/pipes/translate.pipe';

@Component({
    selector: 'app-settings',
    imports: [CommonModule, RouterModule, RouterOutlet, TranslatePipe],
    template: `
    <div class="space-y-6">
      <div>
        <h1 class="text-2xl font-semibold text-foreground">Settings</h1>
        <p class="text-sm text-muted-foreground">Manage account, organization, and security preferences.</p>
      </div>

      <div class="flex flex-wrap gap-2 border-b">
        <a
          routerLink="/settings/profile"
          routerLinkActive="border-b-2 border-primary text-primary"
          class="rounded-t-md border-b-2 border-transparent px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">
          Profile
        </a>
        <a
          routerLink="/settings/organization"
          routerLinkActive="border-b-2 border-primary text-primary"
          class="rounded-t-md border-b-2 border-transparent px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">
          Organization
        </a>
        <a
          routerLink="/settings/roles"
          routerLinkActive="border-b-2 border-primary text-primary"
          class="rounded-t-md border-b-2 border-transparent px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">
          Roles & Permissions
        </a>
        <a
          routerLink="/settings/billing"
          routerLinkActive="border-b-2 border-primary text-primary"
          class="rounded-t-md border-b-2 border-transparent px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">
          Billing
        </a>
        <a
          routerLink="/settings/notifications"
          routerLinkActive="border-b-2 border-primary text-primary"
          class="rounded-t-md border-b-2 border-transparent px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">
          Notifications
        </a>
        <a
          routerLink="/settings/audit"
          routerLinkActive="border-b-2 border-primary text-primary"
          class="rounded-t-md border-b-2 border-transparent px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:text-foreground">
          {{ 'settings.audit.tab' | translate }}
        </a>
      </div>

      <router-outlet></router-outlet>
    </div>
  `
})
export class SettingsComponent {}
