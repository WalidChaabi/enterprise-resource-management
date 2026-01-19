import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => 
      import('./leave-request-list/leave-request-list.component').then(m => m.LeaveRequestListComponent)
  },
  {
    path: 'new',
    loadComponent: () => 
      import('./leave-request-form/leave-request-form.component').then(m => m.LeaveRequestFormComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () => 
      import('./leave-request-form/leave-request-form.component').then(m => m.LeaveRequestFormComponent)
  }
];
