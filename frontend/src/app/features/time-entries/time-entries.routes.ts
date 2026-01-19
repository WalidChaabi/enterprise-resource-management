import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => 
      import('./time-entry-list/time-entry-list.component').then(m => m.TimeEntryListComponent)
  },
  {
    path: 'new',
    loadComponent: () => 
      import('./time-entry-form/time-entry-form.component').then(m => m.TimeEntryFormComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () => 
      import('./time-entry-form/time-entry-form.component').then(m => m.TimeEntryFormComponent)
  }
];
