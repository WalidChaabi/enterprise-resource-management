import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => 
      import('./project-list/project-list.component').then(m => m.ProjectListComponent)
  },
  {
    path: 'new',
    loadComponent: () => 
      import('./project-form/project-form.component').then(m => m.ProjectFormComponent)
  },
  {
    path: ':id',
    loadComponent: () => 
      import('./project-detail/project-detail.component').then(m => m.ProjectDetailComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () => 
      import('./project-form/project-form.component').then(m => m.ProjectFormComponent)
  },
  {
    path: ':projectId/tasks/:id',
    loadComponent: () => 
      import('./task-detail/task-detail.component').then(m => m.TaskDetailComponent)
  },
  {
    path: ':projectId/tasks/new',
    loadComponent: () => 
      import('./task-form/task-form.component').then(m => m.TaskFormComponent)
  },
  {
    path: ':projectId/tasks/:id/edit',
    loadComponent: () => 
      import('./task-form/task-form.component').then(m => m.TaskFormComponent)
  }
];
