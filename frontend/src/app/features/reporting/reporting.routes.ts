import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => 
      import('./reporting-dashboard/reporting-dashboard.component').then(m => m.ReportingDashboardComponent)
  }
];
