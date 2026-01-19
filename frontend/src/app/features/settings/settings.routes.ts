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
  }
];
