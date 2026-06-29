import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./reporting-dashboard/reporting-dashboard.component').then(m => m.ReportingDashboardComponent)
  },
  {
    path: 'capacity',
    loadComponent: () =>
      import('./capacity-dashboard/capacity-dashboard.component').then(m => m.CapacityDashboardComponent)
  }
];
