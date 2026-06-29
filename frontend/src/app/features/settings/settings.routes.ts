import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'profile',
    pathMatch: 'full'
  },
  {
    path: 'profile',
    loadComponent: () =>
      import('./profile-settings/profile-settings.component').then(m => m.ProfileSettingsComponent)
  },
  {
    path: 'organization',
    loadComponent: () =>
      import('./organization-settings/organization-settings.component').then(m => m.OrganizationSettingsComponent)
  },
  {
    path: 'roles',
    loadComponent: () =>
      import('./roles-settings/roles-settings.component').then(m => m.RolesSettingsComponent)
  },
  {
    path: 'billing',
    loadComponent: () =>
      import('./billing-settings/billing-settings.component').then(m => m.BillingSettingsComponent)
  },
  {
    path: 'notifications',
    loadComponent: () =>
      import('./notification-preferences/notification-preferences.component').then(m => m.NotificationPreferencesComponent)
  },
  {
    path: 'audit',
    loadComponent: () =>
      import('./audit-settings/audit-settings.component').then(m => m.AuditSettingsComponent)
  },
  {
    path: 'privacy',
    loadComponent: () =>
      import('./privacy-settings/privacy-settings.component').then(m => m.PrivacySettingsComponent)
  },
  {
    path: 'webhooks',
    loadComponent: () =>
      import('./webhook-settings/webhook-settings.component').then(m => m.WebhookSettingsComponent)
  }
];
